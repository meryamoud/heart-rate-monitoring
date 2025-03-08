package devxplorers.heart_rate_monitor.LogReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogReaderScheduler {
    private final LogReader logReader;

    @Value("${log.file.path}")
    private String filePath;

    public LogReaderScheduler(LogReader logReader) {
        this.logReader = logReader;
    }

    @Scheduled(fixedRate = 10000)
    public void scheduleLogReading() {
        System.out.println("Lecture des logs en cours...");
        logReader.readAndSendLogs(filePath);
    }
}

