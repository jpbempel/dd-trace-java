import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.agent.test.utils.ConfigUtils
import datadog.trace.instrumentation.api.Tags
import datadog.trace.instrumentation.springscheduling.SpringSchedulingInstrumentation.RunnableWrapper
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class ReportTimeTest extends AgentTestRunner {

  static {
    ConfigUtils.updateConfig {
      System.clearProperty("dd.trace.annotations")
    }
  }

  def "trace fired"() {
    setup:
    def scheduledTask = new ScheduledTasks();
    scheduledTask.blockUntilExecute();

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "ScheduledTasks.reportCurrentTime"
          operationName "trace.annotation"
          parent()
          errored false
          tags {
//            "$Tags.COMPONENT" "scheduled"
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test with context"() {
    setup:
    def context = new AnnotationConfigApplicationContext(ScheduledTasksConfig) // provide config as argument
    def task = context.getBean(ScheduledTasks) // provide class we are trying to test
    task.blockUntilExecute();

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "ScheduledTasks.reportCurrentTime"
          operationName "trace.annotation"
          parent()
          errored false
          tags {
//            "$Tags.COMPONENT" "scheduled"
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }

  def "test with context and wrapper"() {
    setup:
    def context = new AnnotationConfigApplicationContext(ScheduledTasksConfig) // provide config as argument
    def task = context.getBean(ScheduledTasks) // provide class we are trying to test
    def wrapper = new RunnableWrapper(task);
    wrapper.run();
    task.blockUntilExecute();

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          serviceName "test"
          resourceName "ScheduledTasks.reportCurrentTime"
          operationName "trace.annotation"
          parent()
          errored false
          tags {
//            "$Tags.COMPONENT" "scheduled"
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
      }
    }
  }
}
