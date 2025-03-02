package devxplorers.heart_rate_monitor.LogReader;

import devxplorers.heart_rate_monitor.Kafka.KafkaProducer;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class LogReader {
    private final KafkaProducer kafkaProducer;

    public LogReader(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void readAndSendLogs(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Integer lastHeartRate = null;
            int messageCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Rx:")) {
                    messageCount++;
                    if (messageCount == 22) {
                        messageCount = 1;
                        continue;
                    }

                    String heartRateData = extractHeartRateData(line);
                    if (heartRateData != null) {
                        String decryptedHeartRate = decryptHeartRate(heartRateData);
                        if (decryptedHeartRate != null) {
                            int currentHeartRate = Integer.parseInt(decryptedHeartRate);
                            if (currentHeartRate >= 30 && currentHeartRate <= 200) {
                                if (lastHeartRate == null || currentHeartRate != lastHeartRate) {
                                    kafkaProducer.sendMessage("Heart Rate: " + currentHeartRate);
                                }
                                lastHeartRate = currentHeartRate;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractHeartRateData(String line) {
        String prefix = "Rx: ";
        int startIndex = line.indexOf(prefix);
        if (startIndex != -1) {
            String heartRateData = line.substring(startIndex + prefix.length()).trim();
            return heartRateData.replace("[", "").replace("]", "").replace(" ", "");
        }
        return null;
    }

    private String decryptHeartRate(String heartRateData) {
        try {
            byte[] byteData = hexStringToByteArray(heartRateData);
            if (byteData.length >= 8) {
                return String.valueOf(byteData[7] & 0xFF);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
