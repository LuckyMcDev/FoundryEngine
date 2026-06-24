package de.luckymcdev.foundryengine.common.log;

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
 * A Log Appender to make the {@link de.luckymcdev.foundryengine.client.editor.panel.tools.ConsolePanel} work.
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

    /**
     * Creates a new EngineLogAppender with the given name.
     */
    public static EngineLogAppender create(String name) {
        return new EngineLogAppender(name, null, null, true, Property.EMPTY_ARRAY);
    }

    /**
     * Sets a listener to be notified of new log entries.
     */
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

    /**
     * Returns a snapshot of the log history.
     */
    public List<LogEntry> getHistory() {
        synchronized (logHistory) {
            return new LinkedList<>(logHistory);
        }
    }

    /**
     * Clears all log history.
     */
    public void clearHistory() {
        synchronized (logHistory) {
            logHistory.clear();
        }
    }

    public static class Holder {
        public static final EngineLogAppender logAppender = EngineLogAppender.create("EngineLogAppender");

        /**
         * Adds the log appender to the root logger.
         */
        public static void addAppender() {
            Logger rootLogger = (Logger) LogManager.getRootLogger();

            logAppender.start();

            rootLogger.addAppender(logAppender);
        }

        /**
         * Returns the singleton EngineLogAppender instance.
         */
        public static EngineLogAppender get() {
            return logAppender;
        }
    }
}