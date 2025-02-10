package devxplorers.heart_rate_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HeartRateMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartRateMonitorApplication.class, args);
    }

}
