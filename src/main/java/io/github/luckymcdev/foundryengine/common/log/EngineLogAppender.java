package io.github.luckymcdev.foundryengine.common.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A Log Appender to make the {@link io.github.luckymcdev.foundryengine.client.editor.builtin.ConsolePanel} work.
 * {@link Holder} is the way of creating this, Although I don't like that and will likely remove it.
 */
public class EngineLogAppender extends AbstractAppender {
    private final List<LogEntry> logHistory = new LinkedList<>();
    public int maxLines = 1000;
    private Consumer<LogEntry> logListener;

    protected EngineLogAppender(
            final String name,
            final Filter filter,
            final Layout<? extends Serializable> layout,
            final boolean ignoreExceptions,
            final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    public static EngineLogAppender create(String name) {
        return new EngineLogAppender(name, null, null, true, Property.EMPTY_ARRAY);
    }

    public void setListener(Consumer<LogEntry> listener) {
        this.logListener = listener;
    }

    @Override
    public void append(LogEvent event) {
        LogEntry entry = new LogEntry(
                event.getTimeMillis(),
                event.getThreadName(),
                event.getLevel(),
                event.getLoggerName(),
                event.getMessage().getFormattedMessage()
        );

        synchronized (logHistory) {
            logHistory.add(entry);

            if (logHistory.size() > maxLines) {
                logHistory.removeFirst();
            }
        }

        if (logListener != null) {
            logListener.accept(entry);
        }
    }

    public List<LogEntry> getHistory() {
        synchronized (logHistory) {
            return new LinkedList<>(logHistory);
        }
    }

    public void clearHistory() {
        synchronized (logHistory) {
            logHistory.clear();
        }
    }

    public static class Holder {
        public static EngineLogAppender LOG_APPENDER;

        public static void addAppender() {
            LOG_APPENDER = EngineLogAppender.create("EngineLogAppender");

            Logger rootLogger = (Logger) LogManager.getRootLogger();

            LOG_APPENDER.start();

            rootLogger.addAppender(LOG_APPENDER);
        }

        public static EngineLogAppender get() {
            return LOG_APPENDER;
        }
    }
}