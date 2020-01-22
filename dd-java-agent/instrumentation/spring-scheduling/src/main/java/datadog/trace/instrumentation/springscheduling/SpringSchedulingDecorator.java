// This file includes software developed at SignalFx

package datadog.trace.instrumentation.springscheduling;

import datadog.trace.agent.decorator.BaseDecorator;
import datadog.trace.api.DDTags;
import datadog.trace.instrumentation.api.AgentSpan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringSchedulingDecorator extends BaseDecorator {
  public static final SpringSchedulingDecorator DECORATE = new SpringSchedulingDecorator();

  private SpringSchedulingDecorator() {}

  @Override
  protected String[] instrumentationNames() {
    return new String[] {"spring-scheduling"};
  }

  @Override
  protected String spanType() {
    return null;
  }

  @Override
  protected String component() {
    return "spring-scheduling";
  }

  public AgentSpan onRun(final AgentSpan span, final Runnable runnable) {
    if (runnable != null) {
      final String className = runnable.getClass().getName();
      final String methodName = "run";
      final String resourceName = className + "." + methodName;
      span.setTag(DDTags.RESOURCE_NAME, resourceName);
    }
    return span;
  }
}
