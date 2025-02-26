package devxplorers.heart_rate_monitor.HeartRate;

import devxplorers.heart_rate_monitor.Kafka.Producer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

public class LogReader {

    private static final String FILE_PATH = "C:/Users/pavilion/Desktop/logreader/SimulANT+ Logs - 808S 0043102/Heart Rate Display ANT Messages.txt";

    @Autowired
    private static Producer kafkaProducer;

    public static void main(String[] args) {
        // Initialize Kafka producer
        kafkaProducer = new Producer();

        // Read and decrypt the logs
        readAndDecryptLogs(FILE_PATH);
    }

    public static void readAndDecryptLogs(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Integer lastHeartRate = null;  // Store the last heart rate value to compare for uniqueness
            int messageCount = 0; // Counter to keep track of the number of data messages

            while ((line = reader.readLine()) != null) {
                // Check if the line contains the pattern "Rx:" indicating received data
                if (line.contains("Rx:")) {
                    messageCount++; // Increment the counter for each data message

                    // Process the first 21 messages
                    if (messageCount == 22) {
                        messageCount = 1;  // Reset the counter after reading 21 messages
                        continue; // Skip the 22nd message
                    }

                    // Extract the heart rate data from the log line
                    String heartRateData = extractHeartRateData(line);

                    if (heartRateData != null) {
                        // Decrypt the heart rate value
                        String decryptedHeartRate = decryptHeartRate(heartRateData);
                        if (decryptedHeartRate != null) {
                            int currentHeartRate = Integer.parseInt(decryptedHeartRate);

                            // Apply a filter to discard unrealistic heart rate values
                            if (currentHeartRate < 30 || currentHeartRate > 200) {
                                continue;  // Skip unrealistic heart rates
                            }

                            // Skip if the current heart rate is the same as the last heart rate (consecutive)
                            if (lastHeartRate == null || currentHeartRate != lastHeartRate) {
                                // Generate the current timestamp for the heart rate
                                long timestamp = System.currentTimeMillis();

                                // Send valid heart rate to Kafka with the generated timestamp
                                kafkaProducer.sendHeartRateWithTimestamp(currentHeartRate, timestamp);
                            }
                            lastHeartRate = currentHeartRate;  // Update the last heart rate value
                        } else {
                            System.out.println("Failed to decrypt heart rate data.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to extract the heart rate data from the log line
    public static String extractHeartRateData(String line) {
        String prefix = "Rx: ";
        int startIndex = line.indexOf(prefix);

        if (startIndex != -1) {
            // Extract the byte sequence after "Rx: "
            String heartRateData = line.substring(startIndex + prefix.length()).trim();

            // Remove the brackets, spaces, and any non-hex characters to get only the hex values
            heartRateData = heartRateData.replace("[", "").replace("]", "").replace(" ", "");

            return heartRateData;
        }

        return null;
    }

    // Method to decrypt the heart rate data (based on ANT+ protocol assumptions)
    public static String decryptHeartRate(String heartRateData) {
        try {
            // Convert the hex string to byte array
            byte[] byteData = hexStringToByteArray(heartRateData);

            // Check if we have enough bytes (at least 8 bytes)
            if (byteData.length >= 8) {
                // Heart rate data is typically in Bytes 4-7, extract those bytes
                byte[] heartRateBytes = new byte[4];
                System.arraycopy(byteData, 4, heartRateBytes, 0, 4);

                // For this example, we assume the heart rate is stored in the first byte of Bytes 4-7
                int heartRate = heartRateBytes[0] & 0xFF;  // Convert to unsigned byte value

                // Return the heart rate as a string
                return String.valueOf(heartRate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Utility method to convert hex string to byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        // Convert hex string to byte array
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
}
