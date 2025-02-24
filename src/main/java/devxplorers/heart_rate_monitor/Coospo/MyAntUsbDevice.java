package devxplorers.heart_rate_monitor.Coospo;

import be.glever.ant.usb.AntUsbDevice;
import javax.usb.UsbDevice;
import java.lang.reflect.Method;

public class MyAntUsbDevice extends AntUsbDevice {

    public MyAntUsbDevice(UsbDevice device) {
        super(device);
    }

    // Public method that uses reflection to call the private getAvailableChannelNumber() method
    public byte fetchAvailableChannelNumber() {
        try {
            Method method = AntUsbDevice.class.getDeclaredMethod("getAvailableChannelNumber");
            method.setAccessible(true);  // Allow access to the private method
            return (byte) method.invoke(this);
        } catch (Exception e) {
            // Replace printStackTrace with robust logging as needed.
            System.err.println("Error fetching available channel number: " + e.getMessage());
        }
        return -1;  // Return an error value if something goes wrong.
    }
}
