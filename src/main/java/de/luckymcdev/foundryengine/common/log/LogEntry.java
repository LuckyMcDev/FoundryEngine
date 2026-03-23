package de.luckymcdev.foundryengine.common.log;

import org.apache.logging.log4j.Level;

/**
 * A record for a normal style formatted log entry.
 * @param timestamp the timestamp
 * @param thread the thread its on
 * @param level the log level
 * @param logger the logger name
 * @param message the log message
 */
public record LogEntry(long timestamp, String thread, Level level, String logger, String message) {
    public String format() {
        return String.format("[%tT] [%s/%s] [%s]: %s", timestamp, thread, level, logger, message);
    }
}