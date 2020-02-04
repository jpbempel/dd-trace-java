import datadog.trace.agent.test.AgentTestRunner
import datadog.trace.instrumentation.api.Tags
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class SpringSchedulingTest extends AgentTestRunner {

  Lock lock = new ReentrantLock()

  def "schedule interval test"() {
    setup:
    def context = new AnnotationConfigApplicationContext(IntervalTaskConfig)
    def task = context.getBean(IntervalTask)
    
    lock.lock()
    TEST_WRITER.clear()

    task.blockUntilExecute()

    expect:
    assert task != null
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "IntervalTask.run"
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
    lock.unlock()


  }

  def "schedule trigger test according to cron expression"() {
    setup:
    def context = new AnnotationConfigApplicationContext(TriggerTaskConfig)
    def task = context.getBean(TriggerTask)

    lock.lock()
    TEST_WRITER.clear()

    task.blockUntilExecute()

    expect:
    assert task != null
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          resourceName "TriggerTask.run"
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
    lock.unlock()
  }
}
