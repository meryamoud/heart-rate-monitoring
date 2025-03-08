package devxplorers.heart_rate_monitor.LogReader;

import devxplorers.heart_rate_monitor.Kafka.KafkaProducer;
import org.springframework.stereotype.Component;
import java.time.temporal.ChronoUnit;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.regex.*;

@Component
public class LogReader {
    private final KafkaProducer kafkaProducer;
    private static final String SESSION_DATE_PATTERN = "(\\d{2}/\\d{2}/\\d{4})\\s(\\d{2}:\\d{2}:\\d{2})";
    private static final String TIMESTAMP_PATTERN = "(\\d+)\\s:\\sRx:\\s\\[[^\\]]+\\]";
    private static final String POSITION_FILE = "processed_position.txt";

    public LogReader(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void readAndSendLogs(String filePath) {
        long lastReadPosition = getLastReadPosition();
        try (RandomAccessFile logFile = new RandomAccessFile(filePath, "r")) {
            logFile.seek(lastReadPosition);
            String line;
            Integer lastHeartRate = null;
            int messageCount = 0;
            long previousTimestamp = -1;
            String sessionDate = "";
            LocalDateTime sessionDateTime = null;

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            while ((line = logFile.readLine()) != null) {

                Pattern datePattern = Pattern.compile(SESSION_DATE_PATTERN);
                Matcher dateMatcher = datePattern.matcher(line);

                if (dateMatcher.find()) {
                    sessionDate = dateMatcher.group(1) + " " + dateMatcher.group(2);
                    sessionDateTime = LocalDateTime.parse(sessionDate, dateFormatter);
                    System.out.println("New Session Date: " + sessionDateTime);
                    continue;
                }

                Pattern timestampPatternCompiled = Pattern.compile(TIMESTAMP_PATTERN);
                Matcher timestampMatcher = timestampPatternCompiled.matcher(line);

                if (timestampMatcher.find()) {

                    long currentTimestamp = Long.parseLong(timestampMatcher.group(1));
                    if (previousTimestamp != -1) {
                        long diffInMillis = currentTimestamp - previousTimestamp;
                        if (sessionDateTime != null) {
                            sessionDateTime = sessionDateTime.plus(diffInMillis, ChronoUnit.MILLIS);
                        }
                    }

                    previousTimestamp = currentTimestamp;

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
                                    String message = "Time: " + sessionDateTime + ", Heart Rate: " + currentHeartRate;
                                    kafkaProducer.sendMessage(message);
                                }
                                lastHeartRate = currentHeartRate;
                            }
                        }
                    }
                }
                updateLastReadPosition(logFile.getFilePointer());
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

    private long getLastReadPosition() {
        File positionFile = new File(POSITION_FILE);
        if (positionFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(positionFile))) {
                return Long.parseLong(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void updateLastReadPosition(long newPosition) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(POSITION_FILE))) {
            writer.write(String.valueOf(newPosition));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
