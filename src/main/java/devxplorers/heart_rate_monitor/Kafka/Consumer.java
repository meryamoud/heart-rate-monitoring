package devxplorers.heart_rate_monitor.Kafka;


import devxplorers.heart_rate_monitor.Twilio.TwilioService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class Consumer {

    @Autowired // cette annotation permet l'injection de dépendances
    private TwilioService twilioService; // Injection du service Twilio

    private static final int HIGH_HEART_RATE = 100;  // Seuil critique haut
    private static final int LOW_HEART_RATE = 60;   // Seuil critique bas
    private static final String TEST_PHONE_NUMBER = "+33784828928";  // mon numéro pour le test
    private static final int WINDOW_SIZE = 10;  // Taille de la fenêtre glissante pour l'analyse des tendances

    private Queue<Integer> recentHeartRates = new LinkedList<>();
    private int sum = 0;

    @KafkaListener(topics = "heart_rate", groupId = "heart_rate_group")
    public void consume(ConsumerRecord<String, Integer> record) {
        Integer heartRate = record.value();
        System.out.println("Consumed heart rate: " + heartRate);

        // Ajouter la valeur actuelle à la fenêtre glissante
        recentHeartRates.add(heartRate);
        sum += heartRate;

        if (recentHeartRates.size() > WINDOW_SIZE) {
            sum -= recentHeartRates.poll();
        }

        double average = sum / (double) recentHeartRates.size();
        double deviation = calculateStandardDeviation(recentHeartRates, average);

        System.out.println("Average heart rate: " + average + ", Standard deviation: " + deviation);

        // Vérification des anomalies basées sur les seuils fixes
        if (heartRate >= HIGH_HEART_RATE || heartRate <= LOW_HEART_RATE) {
            String message = "Alerte : Fréquence cardiaque anormale détectée ! (" + heartRate + " BPM)";
            twilioService.sendSms(TEST_PHONE_NUMBER, message);
            System.out.println("Notification envoyée via Twilio !");
        }

        // Vérification des anomalies basées sur la déviation par rapport à la moyenne
        if (deviation > 15) {  // Seuil arbitraire pour la déviation
            String message = "Alerte : Fréquence cardiaque instable détectée ! (Déviation élevée)";
            twilioService.sendSms(TEST_PHONE_NUMBER, message);
            System.out.println("Notification envoyée pour instabilité cardiaque !");
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