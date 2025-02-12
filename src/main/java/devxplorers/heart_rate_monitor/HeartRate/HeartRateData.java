package devxplorers.heart_rate_monitor.HeartRate;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "heart_rate")
public class HeartRateData {

    @Id
    private String id;
    private int heartRate;
    private String timestamp; // Stocke la date lisible

    public HeartRateData(int heartRate, String timestamp) {
        this.heartRate = heartRate;
        this.timestamp = timestamp;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    @Override
    public String toString() {
        return "HeartRateData{" +
                "heartRate=" + heartRate +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
