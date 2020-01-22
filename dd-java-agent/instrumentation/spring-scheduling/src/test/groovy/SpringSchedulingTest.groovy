import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.agent.test.utils.ConfigUtils
import datadog.trace.instrumentation.api.Tags
import datadog.trace.instrumentation.springscheduling.SpringSchedulingInstrumentation.RunnableWrapper
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class SpringSchedulingTest extends AgentTestRunner {

  static {
    ConfigUtils.updateConfig {
      System.clearProperty("dd.trace.annotations")
    }
  }

  def "test with context and wrapper"() {
    setup:
    def context = new AnnotationConfigApplicationContext(ScheduledTasksConfig)
    def task = context.getBean(ScheduledTasks)
    def wrapper = new RunnableWrapper(task);
    wrapper.run();
    task.blockUntilExecute();

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "ScheduledTasks.run"
          operationName "scheduled.call"
          parent()
          errored false
          tags {
            "$Tags.COMPONENT" "spring-scheduling"
            defaultTags()
          }
        }
      }
    }
  }
}
