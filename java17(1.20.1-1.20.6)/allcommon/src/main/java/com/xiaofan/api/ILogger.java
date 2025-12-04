package com.xiaofan.api;

/**
 * 日志接口
 * 用于在不同版本之间提供统一的日志功能
 */
public interface ILogger {
    void info(String message);
    void info(String format, Object... args);
    void warn(String message);
    void warn(String format, Object... args);
    void error(String message);
    void error(String message, Throwable throwable);
    void debug(String message);
    void debug(String format, Object... args);
}

