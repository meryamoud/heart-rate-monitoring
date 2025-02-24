package devxplorers.heart_rate_monitor.Coospo;

import be.glever.ant.message.AntMessage;
import be.glever.ant.message.AntMessageRegistry;
import be.glever.ant.message.requestedresponse.CapabilitiesResponseMessage;
import be.glever.ant.AntException;

public class CustomAntMessageRegistry {

    public AntMessage from(byte[] messageBytes) throws AntException {
        if (messageBytes.length < 1) {
            throw new AntException("Message is too short to determine type.");
        }

        byte messageId = messageBytes[0];

        // Intercept CapabilitiesResponseMessage
        if (messageId == CapabilitiesResponseMessage.MSG_ID) {
            if (messageBytes.length < 6) {
                System.out.println("Warning: Received CapabilitiesResponseMessage with length " + messageBytes.length + " (expected at least 6 bytes).");
                return null; // Ignore the message
            }
        }

        // Call the static method from AntMessageRegistry instead of using super
        return AntMessageRegistry.from(messageBytes);
    }
}
