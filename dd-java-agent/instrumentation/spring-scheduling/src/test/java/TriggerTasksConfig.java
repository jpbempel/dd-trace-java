import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class TriggerTasksConfig {
  @Bean
  public TriggerTasks triggerTasks() {
    return new TriggerTasks();
  }
}
