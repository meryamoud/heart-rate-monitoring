package devxplorers.heart_rate_monitor.Coospo;

import be.glever.ant.AntException;
import be.glever.ant.message.AntMessage;

public interface CustomAntMessageRegistryHRM {
    AntMessage from(byte[] messageBytes) throws AntException;
}
