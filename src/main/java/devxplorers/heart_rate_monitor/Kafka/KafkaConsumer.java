package devxplorers.heart_rate_monitor.Kafka;

import devxplorers.heart_rate_monitor.HeartRate.HeartRateData;
import devxplorers.heart_rate_monitor.HeartRate.HeartRateRepository;
import devxplorers.heart_rate_monitor.Twilio.TwilioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


import java.util.LinkedList;
import java.util.Queue;

@Service
public class KafkaConsumer {

    @Autowired
    private HeartRateRepository heartRateRepository;


    @Autowired
    private TwilioService twilioService;

    private static final int HIGH_HEART_RATE = 100;
    private static final int LOW_HEART_RATE = 60;
    private static final String TEST_PHONE_NUMBER = "+33784828928";
    private static final int WINDOW_SIZE = 10;

    private Queue<Integer> recentHeartRates = new LinkedList<>();
    private int sum = 0;



    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(String message) {
        try {
            // Extraire le cœur du message (le nombre après "Heart Rate: ")
            String[] parts = message.split(": ");
            if (parts.length > 1) {
                int heartRate = Integer.parseInt(parts[1].trim()); // Convertir la partie après ": " en entier

                // Créer l'objet HeartRateData avec un timestamp
                HeartRateData data = new HeartRateData(heartRate);

                // Sauvegarder dans Elasticsearch
                heartRateRepository.save(data);

                // Afficher dans la console
                System.out.println("📥 Stocké dans Elasticsearch : " + heartRate + " à " + data.getTimestamp());
                recentHeartRates.add(heartRate);
                sum += heartRate;

                if (recentHeartRates.size() > WINDOW_SIZE) {
                    sum -= recentHeartRates.poll();
                }

                double average = sum / (double) recentHeartRates.size();
                double deviation = calculateStandardDeviation(recentHeartRates, average);

                System.out.println("Average heart rate: " + average + ", Standard deviation: " + deviation);

                // Vérification des anomalies
                if (heartRate >= HIGH_HEART_RATE || heartRate <= LOW_HEART_RATE) {
                    String message1 = "Alerte : Fréquence cardiaque anormale détectée ! (" + heartRate + " BPM)";
                    twilioService.sendSms(TEST_PHONE_NUMBER, message1);
                    //System.out.println("Notification envoyée via Twilio !");
                }

                if (deviation > 15) {
                    String message2 = "Alerte : Fréquence cardiaque instable détectée ! (Déviation élevée)";
                    twilioService.sendSms(TEST_PHONE_NUMBER, message2);
                    System.out.println("Notification envoyée pour instabilité cardiaque !");
            }
        }
        }catch (NumberFormatException e) {
            System.err.println("❌ Erreur de parsing du heart rate : " + message);
        }
    }
    private double calculateStandardDeviation(Queue<Integer> heartRates, double average) {
        double sumOfSquares = 0.0;
        for (int rate : heartRates) {
            sumOfSquares += Math.pow(rate - average, 2);
        }
        return Math.sqrt(sumOfSquares / heartRates.size());
    }
}




