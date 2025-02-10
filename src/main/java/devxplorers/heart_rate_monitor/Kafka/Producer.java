package devxplorers.heart_rate_monitor.Kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {
    private static final String TOPIC = "heart_rate";

    @Autowired
    private KafkaTemplate<String, Integer> kafkaTemplate;

    public void sendHeartRate(int heartRate) {
        kafkaTemplate.send(TOPIC, heartRate);
    }
}
