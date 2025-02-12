package devxplorers.heart_rate_monitor.Kafka;


import devxplorers.heart_rate_monitor.Twilio.TwilioService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    @Autowired
    private TwilioService twilioService; // Injection du service Twilio

    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(ConsumerRecord<String, Integer> record) {
        Integer heartRate = record.value();
        System.out.println("Consumed heart rate: " + heartRate);

    }
}