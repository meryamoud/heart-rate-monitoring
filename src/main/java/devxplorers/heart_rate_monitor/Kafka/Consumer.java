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
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;

@Service
public class Consumer {

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private HeartRateRepository heartRateRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final int HIGH_HEART_RATE = 100;
    private static final int LOW_HEART_RATE = 60;
    private static final String TEST_PHONE_NUMBER = "+33784828928";
    private static final int WINDOW_SIZE = 10;

    private Queue<Integer> recentHeartRates = new LinkedList<>();
    private int sum = 0;

    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(ConsumerRecord<String, Integer> record) {
        try {
            int heartRate = record.value();
            long kafkaTimestamp = record.timestamp();
            String readableTimestamp = dateFormat.format(new Date(kafkaTimestamp));

            // Stocker la donnée dans Elasticsearch
            HeartRateData data = new HeartRateData(heartRate, readableTimestamp);
            heartRateRepository.save(data);
            System.out.println("Saved heart rate: " + data.getHeartRate() + " at " + readableTimestamp);

            // Ajouter la valeur actuelle à la fenêtre glissante
            recentHeartRates.add(heartRate);
            sum += heartRate;

            if (recentHeartRates.size() > WINDOW_SIZE) {
                sum -= recentHeartRates.poll();
            }

            double average = sum / (double) recentHeartRates.size();
            double deviation = calculateStandardDeviation(recentHeartRates, average);

            System.out.println("Average heart rate: " + average + ", Standard deviation: " + deviation);

            // Vérification des anomalies
            if (heartRate >= HIGH_HEART_RATE || heartRate <= LOW_HEART_RATE) {
                String message = "Alerte : Fréquence cardiaque anormale détectée ! (" + heartRate + " BPM)";
                twilioService.sendSms(TEST_PHONE_NUMBER, message);
                System.out.println("Notification envoyée via Twilio !");
            }

            if (deviation > 15) {
                String message = "Alerte : Fréquence cardiaque instable détectée ! (Déviation élevée)";
                twilioService.sendSms(TEST_PHONE_NUMBER, message);
                System.out.println("Notification envoyée pour instabilité cardiaque !");
            }
        } catch (Exception e) {
            System.err.println("Error consuming Kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateStandardDeviation(Queue<Integer> heartRates, double average) {
        double sumOfSquares = 0.0;
        for (int rate : heartRates) {
            sumOfSquares += Math.pow(rate - average, 2);
        }
        return Math.sqrt(sumOfSquares / heartRates.size());
    }
}
