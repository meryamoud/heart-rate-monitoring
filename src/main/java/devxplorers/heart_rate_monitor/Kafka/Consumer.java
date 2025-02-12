package devxplorers.heart_rate_monitor.Kafka;


import devxplorers.heart_rate_monitor.HeartRate.HeartRateData;
import devxplorers.heart_rate_monitor.HeartRate.HeartRateRepository;
import devxplorers.heart_rate_monitor.Twilio.TwilioService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class Consumer {

    @Autowired
    private TwilioService twilioService; // Injection du service Twilio

    @Autowired
    private HeartRateRepository heartRateRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Changer selon ton besoin
    }

    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(ConsumerRecord<String, Integer> record) {
        try {
            int heartRate = record.value();
            long kafkaTimestamp = record.timestamp();

            // Convertir le timestamp en format lisible
            String readableTimestamp = dateFormat.format(new Date(kafkaTimestamp));

            // Stocker la donnée formatée dans Elasticsearch
            HeartRateData data = new HeartRateData(heartRate, readableTimestamp);
            heartRateRepository.save(data);

            System.out.println("Saved heart rate: " + data.getHeartRate() + " at " + readableTimestamp);
        } catch (Exception e) {
            System.err.println("Error consuming Kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}