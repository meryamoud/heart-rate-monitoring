package devxplorers.heart_rate_monitor.Coospo;


import be.glever.ant.AntException;
import be.glever.ant.usb.AntUsbDevice;
import be.glever.ant.usb.AntUsbDeviceFactory;
import java.util.List;

public class SensorScanner {

    public static void main(String[] args) {
        try {
            // Retrieve connected ANT+ USB dongles
            List<AntUsbDevice> devices = AntUsbDeviceFactory.getAvailableAntDevices();

            if (devices.isEmpty()) {
                System.out.println("No ANT+ USB devices found.");
                return;
            }

            // Print available devices
            System.out.println("Found " + devices.size() + " ANT+ USB device(s):");
            for (AntUsbDevice device : devices) {
                System.out.println(" - " + device);
            }

        } catch (AntException e) {
            System.err.println("Error while detecting ANT+ USB devices: " + e.getMessage());
        }
    }


}
