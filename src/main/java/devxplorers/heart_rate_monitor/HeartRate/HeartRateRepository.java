package devxplorers.heart_rate_monitor.HeartRate;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRateRepository  extends ElasticsearchRepository<HeartRateData, String> {

}
