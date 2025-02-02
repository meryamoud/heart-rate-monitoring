package devxplorers.heart_rate_monitor.Kafka;

import org.springframework.kafka.annotation.KafkaListener;

public class Consumer {
    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(String message) {
        System.out.println(" Message reçu de Kafka : " + message);
    }
}
