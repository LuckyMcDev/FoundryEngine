package io.github.luckymcdev.foundryengine.common.log;

import org.apache.logging.log4j.Level;

public record LogEntry(long timestamp, String thread, Level level, String logger, String message) {
    public String format() {
        return String.format("[%tT] [%s/%s] [%s]: %s", timestamp, thread, level, logger, message);
    }
}