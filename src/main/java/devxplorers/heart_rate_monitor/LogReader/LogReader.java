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
    private static final String SESSION_DATE_PATTERN = "(\\d{2}/\\d{2}/\\d{4})\\s(\\d{2}:\\d{2}:\\d{2})";  // Pattern to extract session date
    private static final String TIMESTAMP_PATTERN = "(\\d+)\\s:\\sRx:\\s\\[[^\\]]+\\]";  // Pattern to extract timestamp
    private static final String POSITION_FILE = "processed_position.txt"; // Fichier pour sauvegarder la position lue

    public LogReader(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void readAndSendLogs(String filePath) {
        long lastReadPosition = getLastReadPosition();  // Récupérer la dernière position lue
        try (RandomAccessFile logFile = new RandomAccessFile(filePath, "r")) {
            logFile.seek(lastReadPosition); // Se positionner à la dernière position lue
            String line;
            Integer lastHeartRate = null;
            int messageCount = 0;
            long previousTimestamp = -1;  // Pour stocker le timestamp précédent
            String sessionDate = "";
            LocalDateTime sessionDateTime = null;

            // DateTimeFormatter pour le format de la date de session
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            while ((line = logFile.readLine()) != null) {
                // Vérifier la date de session (la première ligne contient la date de session)
                Pattern datePattern = Pattern.compile(SESSION_DATE_PATTERN);
                Matcher dateMatcher = datePattern.matcher(line);

                if (dateMatcher.find()) {
                    // Capturer la date de session et la convertir en LocalDateTime
                    sessionDate = dateMatcher.group(1) + " " + dateMatcher.group(2);
                    sessionDateTime = LocalDateTime.parse(sessionDate, dateFormatter);
                    System.out.println("New Session Date: " + sessionDateTime);
                    continue;  // Passer la ligne contenant la date de session
                }

                // Vérifier les lignes de log avec les timestamps
                Pattern timestampPatternCompiled = Pattern.compile(TIMESTAMP_PATTERN);
                Matcher timestampMatcher = timestampPatternCompiled.matcher(line);

                if (timestampMatcher.find()) {
                    // Capturer le timestamp actuel (en millisecondes)
                    long currentTimestamp = Long.parseLong(timestampMatcher.group(1));

                    // Si nous avons un timestamp précédent, calculer la différence
                    if (previousTimestamp != -1) {
                        // Calculer la différence en millisecondes entre le timestamp actuel et le précédent
                        long diffInMillis = currentTimestamp - previousTimestamp;

                        // Ajouter la différence au timestamp de session
                        if (sessionDateTime != null) {
                            sessionDateTime = sessionDateTime.plus(diffInMillis, ChronoUnit.MILLIS);
                        }
                    }

                    // Mettre à jour le timestamp précédent pour la prochaine itération
                    previousTimestamp = currentTimestamp;

                    // Extraire et traiter les données de fréquence cardiaque
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
                                    // Préparer le message combiné avec l'heure de session et la fréquence cardiaque
                                    String message = "Time: " + sessionDateTime + ", Heart Rate: " + currentHeartRate;
                                    kafkaProducer.sendMessage(message);  // Envoyer le message à Kafka
                                }
                                lastHeartRate = currentHeartRate;
                            }
                        }
                    }
                }
                // Sauvegarder la position lue
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

    // Récupérer la dernière position lue dans le fichier
    private long getLastReadPosition() {
        File positionFile = new File(POSITION_FILE);
        if (positionFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(positionFile))) {
                return Long.parseLong(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0; // Si le fichier n'existe pas, commencer à partir du début
    }

    // Mettre à jour la position lue dans le fichier
    private void updateLastReadPosition(long newPosition) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(POSITION_FILE))) {
            writer.write(String.valueOf(newPosition));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
