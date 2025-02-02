package devxplorers.heart_rate_monitor.Controller;

import devxplorers.heart_rate_monitor.Kafka.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {
    @Autowired
    private Producer producer;

    @GetMapping("/send")
    public String sendMessage(@RequestParam("message") int bpm) {
        producer.sendHeartRate(bpm);
        return "📤 Message envoyé à Kafka avec succès : " + bpm + " BPM";
    }
}
