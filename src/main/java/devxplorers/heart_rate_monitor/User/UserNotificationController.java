package devxplorers.heart_rate_monitor.User;

import devxplorers.heart_rate_monitor.Twilio.TwilioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserNotificationController {

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private UserService userService;

    @PostMapping("/send-sms-to-user")
    public String sendSmsToUser(@RequestParam String phoneNumber, @RequestParam String message) {
        User user = userService.getUserByPhoneNumber(phoneNumber);
        if (user != null) {
            twilioService.sendSms(phoneNumber, message);
            return "SMS sent successfully to " + user.getName();
        } else {
            return "User not found!";
        }
    }
}