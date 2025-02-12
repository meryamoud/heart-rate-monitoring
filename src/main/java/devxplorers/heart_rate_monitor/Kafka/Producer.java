package devxplorers.heart_rate_monitor.Kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final String TOPIC = "heart_rate";

    @Autowired
    private KafkaTemplate<String, Integer> kafkaTemplate;

    public void sendHeartRate(int heartRate) {
        long timestamp = System.currentTimeMillis(); // Génération du timestamp
        ProducerRecord<String, Integer> record = new ProducerRecord<>(TOPIC, null, timestamp, null, heartRate);
        kafkaTemplate.send(record); // Envoi du message avec le timestamp

        System.out.println("Sent: HeartRate = " + heartRate + " at " + timestamp);
    }
}
