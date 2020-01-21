// This file includes software developed at SignalFx

package datadog.trace.instrumentation.springscheduling;

import datadog.trace.agent.decorator.BaseDecorator;
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
}
