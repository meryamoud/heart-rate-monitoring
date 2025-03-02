package devxplorers.heart_rate_monitor.Twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class TwilioService {

    @Value("${twilio.enabled:false}") // Valeur par défaut : false
    private boolean isTwilioEnabled;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    public void sendSms(String toPhoneNumber, String messageBody) {
        if (!isTwilioEnabled) {
            System.out.println("Twilio est désactivé. Aucun SMS envoyé.");
            return;
        }
        Twilio.init(accountSid, authToken);  // Initialize Twilio with the instance variables

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber),  // Destination phone number
                new com.twilio.type.PhoneNumber(twilioPhoneNumber),  // Your Twilio phone number
                messageBody  // Message body
        ).create();

        System.out.println("SMS envoyé avec succès à " + toPhoneNumber + " avec SID: " + message.getSid());
    }
}
