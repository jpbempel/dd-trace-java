package datadog.trace.logging.simplelogger;

import datadog.trace.logging.LogLevel;
import datadog.trace.logging.LoggerHelper;
import java.util.Date;
import org.slf4j.Marker;

/**
 * A {@link LoggerHelper} that logs in a way compatible with the {@code SimpleLogger} from SLF4J.
 */
class SLCompatHelper extends LoggerHelper {
  private final String logName;
  private final LogLevel logLevel;
  private final SLCompatSettings settings;

  SLCompatHelper(String name, SLCompatSettings settings) {
    this(settings.logNameForName(name), settings.logLevelForName(name), settings);
  }

  SLCompatHelper(String logName, LogLevel logLevel, SLCompatSettings settings) {
    this.logName = logName;
    this.logLevel = logLevel;
    this.settings = settings;
  }

  private void appendFormattedDate(StringBuilder builder, long timeMillis, long startTimeMillis) {
    if (settings.dateTimeFormatter != null) {
      Date date = new Date(timeMillis);
      String dateString;
      synchronized (settings.dateTimeFormatter) {
        dateString = settings.dateTimeFormatter.format(date);
      }
      builder.append(dateString);
    } else {
      builder.append(timeMillis - startTimeMillis);
    }
  }

  @Override
  public boolean enabled(LogLevel level, Marker marker) {
    // SimpleLogger ignores markers
    return level.isEnabled(this.logLevel);
  }

  @Override
  public void log(LogLevel level, String message, Throwable t) {
    long timeMillis = Integer.MIN_VALUE;
    if (settings.showDateTime) {
      timeMillis = System.currentTimeMillis();
    }
    log(level, SLCompatFactory.START_TIME, timeMillis, message, t);
  }

  void log(LogLevel level, long startTimeMillis, long timeMillis, String message, Throwable t) {
    String threadName = null;
    if (settings.showThreadName) {
      threadName = Thread.currentThread().getName();
    }
    log(level, startTimeMillis, timeMillis, threadName, message, t);
  }

  void log(
      LogLevel level,
      long startTimeMillis,
      long timeMillis,
      String threadName,
      String message,
      Throwable t) {
    StringBuilder buf = new StringBuilder(32);

    if (timeMillis >= 0 && settings.showDateTime) {
      appendFormattedDate(buf, timeMillis, startTimeMillis);
      buf.append(' ');
    }

    if (settings.showThreadName && threadName != null) {
      buf.append('[');
      buf.append(threadName);
      buf.append("] ");
    }

    if (settings.levelInBrackets) {
      buf.append('[');
    }

    if (settings.warnLevelString != null && level == LogLevel.WARN) {
      buf.append(settings.warnLevelString);
    } else {
      buf.append(level.name());
    }
    if (settings.levelInBrackets) {
      buf.append(']');
    }
    buf.append(' ');

    if (logName.length() > 0) {
      buf.append(logName).append(" - ");
    }

    buf.append(message);

    settings.printStream.println(buf.toString());
    if (t != null) {
      t.printStackTrace(settings.printStream);
    }
  }
}
