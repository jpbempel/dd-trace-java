import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.instrumentation.api.Tags
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class SpringSchedulingTest extends AgentTestRunner {

  def "test with context and wrapper"() {
    setup:
    def context = new AnnotationConfigApplicationContext(ScheduledTasksConfig)
    def task = context.getBean(ScheduledTasks)
    task.blockUntilExecute();

    expect:
    assert task != null;
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "org.springframework.scheduling.support.ScheduledMethodRunnable.run"
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
