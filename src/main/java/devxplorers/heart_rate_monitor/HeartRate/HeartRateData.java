package devxplorers.heart_rate_monitor.HeartRate;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;


@Document(indexName = "heart_rate")
public class HeartRateData {

    @Id
    private String id;

    private int heartRate;
    @Field(type = FieldType.Date)
    private Date timestamp;

    public HeartRateData(int heartRate,Date timestamp) {
        this.heartRate = heartRate;
        this.timestamp = timestamp;
    }

}
