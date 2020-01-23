import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduledTasksConfig {
  @Bean
  public ScheduledTasks scheduledTasks() {
    return new ScheduledTasks();
  }
}
