package devxplorers.heart_rate_monitor.Twilio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @Autowired
    private TwilioService twilioService;

    @PostMapping("/send-sms")
    public String sendSms(@RequestParam String phoneNumber, @RequestParam String message) {
        twilioService.sendSms(phoneNumber, message);
        return "SMS sent successfully!";
    }
}