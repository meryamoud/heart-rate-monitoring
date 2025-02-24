package devxplorers.heart_rate_monitor.Coospo;

import devxplorers.heart_rate_monitor.Kafka.Producer;
import be.glever.ant.AntException;
import be.glever.ant.message.data.BroadcastDataMessage;
import be.glever.ant.usb.AntUsbDevice;
import be.glever.ant.usb.AntUsbDeviceFactory;
import be.glever.antplus.hrm.HRMChannel;
import be.glever.antplus.hrm.HRMSlave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class SensorScanner {

    @Autowired
    private Producer producer;  // Inject the Kafka producer

    // Periodically scan the sensor every 5 seconds (5000 milliseconds)
    @Scheduled(fixedRate = 5000)
    public void scanSensor() {
        try {
            // 1. Get available ANT+ USB devices
            List<AntUsbDevice> devices = AntUsbDeviceFactory.getAvailableAntDevices();

            if (devices.isEmpty()) {
                System.out.println("No ANT+ USB devices found.");
                return;
            }

            // 2. Use the first available ANT+ device
            AntUsbDevice usbDevice = devices.get(0);

            // Initialize the device
            usbDevice.initialize();
            System.out.println("Using ANT+ USB device: " + usbDevice);

            // 3. Set up the HRM channel using HRMSlave for configuration
            HRMSlave hrmSlave = new HRMSlave(usbDevice);

            // 4. Catch ExecutionException when calling listDevices()
            try {
                hrmSlave.listDevices(); // Configure the device
                System.out.println("HRM device(s) detected successfully.");

                // Check if the listDevices method didn't return null (implying a valid setup)
                if (hrmSlave != null) {
                    System.out.println("Heart Rate Monitor is connected successfully.");
                } else {
                    System.out.println("No Heart Rate Monitor detected.");
                }
            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error during device configuration: " + e.getMessage());
                return; // Exit or handle the exception as needed
            }

            // 6. Create the HRMChannel to handle the heart rate data
            HRMChannel hrmChannel = new HRMChannel(usbDevice);

            // 7. Subscribe to HRM events and process them
            hrmChannel.subscribeTo(hrmChannel.getEvents());

            // Integration of your event handling logic
            hrmChannel.getEvents()
                    .doOnNext(message -> {
                        if (message instanceof BroadcastDataMessage) {
                            BroadcastDataMessage dataMessage = (BroadcastDataMessage) message;
                            byte[] payload = dataMessage.getPayLoad();
                            if (payload.length == 8) {  // Check if the payload length is 8
                                int heartRate = payload[0] & 0xFF; // Extract heart rate
                                System.out.println("Heart Rate: " + heartRate + " BPM");

                                // Send the heart rate data to Kafka
                                producer.sendHeartRate(heartRate);
                            } else {
                                System.err.println("Incorrect message length: " + payload.length);
                            }
                        }
                    })
                    .doOnError(error -> System.err.println("Error in event handling: " + error))
                    .subscribe();

        } catch (AntException e) {
            System.err.println("Error while communicating with ANT+ USB device: " + e.getMessage());
        }
    }
}
