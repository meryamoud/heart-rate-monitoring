package devxplorers.heart_rate_monitor.HeartRate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;


@Document(indexName = "heart_rate_index")
public class HeartRateData {

    @Id
    private String id; // L'ID généré par Elasticsearch

    private int heartRate;
    @Field(type = FieldType.Date)  // Stocke en tant que type `date` dans Elasticsearch
    private Date timestamp;

    public HeartRateData(int heartRate) {
        this.heartRate = heartRate;
        this.timestamp = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)); // Timestamp du moment où on stocke la donnée
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

}
