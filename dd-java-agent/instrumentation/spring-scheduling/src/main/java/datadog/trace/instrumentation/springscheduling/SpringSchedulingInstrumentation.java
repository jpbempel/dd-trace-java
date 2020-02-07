// This file includes software developed at SignalFx

package datadog.trace.instrumentation.springscheduling;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import datadog.trace.api.DDTags;
import datadog.trace.instrumentation.api.AgentScope;
import datadog.trace.instrumentation.api.AgentSpan;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static datadog.trace.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.instrumentation.api.AgentTracer.activeSpan;
import static datadog.trace.instrumentation.api.AgentTracer.startSpan;
import static datadog.trace.instrumentation.springscheduling.SpringSchedulingDecorator.DECORATE;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

@AutoService(Instrumenter.class)
public final class SpringSchedulingInstrumentation extends Instrumenter.Default {

  public SpringSchedulingInstrumentation() {
    super("spring-scheduling");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return not(isInterface()).and(named("org.springframework.scheduling.config.Task"));
  }

  @Override
  public String[] helperClassNames() {
    return new String[] {
      "datadog.trace.agent.decorator.BaseDecorator",
      packageName + ".SpringSchedulingDecorator",
      getClass().getName() + "$RunnableWrapper",
    };
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(
        isConstructor().and(takesArgument(0, Runnable.class)),
        SpringSchedulingInstrumentation.class.getName() + "$RepositoryFactorySupportAdvice");
  }

  public static class SpringSchedulingAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onConstruction(
        @Advice.Argument(value = 0, readOnly = false) Runnable runnable) {
      runnable = RunnableWrapper.wrapIfNeeded(runnable);
    }
  }

  public static class RunnableWrapper implements Runnable {
    private final Runnable runnable;

    public RunnableWrapper(final Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void run() {
      final AgentSpan span = startSpan("");
      DECORATE.afterStart(span);

      try (final AgentScope scope = activateSpan(span, false)) {
        activeSpan().setTag(DDTags.SERVICE_NAME, "test");
        DECORATE.afterStart(span);
        scope.setAsyncPropagation(true);

        try {
          runnable.run();
        } catch (final Throwable throwable) {
          DECORATE.onError(span, throwable);
          DECORATE.beforeFinish(span);
          span.finish();
          throw throwable;
        }
      }
    }

    public static Runnable wrapIfNeeded(final Runnable task) {
      // We wrap only lambdas' anonymous classes and if given object has not already been wrapped.
      // Anonymous classes have '/' in class name which is not allowed in 'normal' classes.
      if (task instanceof RunnableWrapper) {
        return task;
      }
      return new RunnableWrapper(task);
    }
  }
}
