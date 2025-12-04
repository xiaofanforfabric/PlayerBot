package com.xiaofan.version;

import com.mojang.logging.LogUtils;
import com.xiaofan.api.ILogger;
import org.slf4j.Logger;

/**
 * 1.20.1 版本的日志实现
 */
public class LoggerImpl implements ILogger {
    private final Logger logger = LogUtils.getLogger();
    
    @Override
    public void info(String message) {
        logger.info(message);
    }
    
    @Override
    public void info(String format, Object... args) {
        logger.info(format, args);
    }
    
    @Override
    public void warn(String message) {
        logger.warn(message);
    }
    
    @Override
    public void warn(String format, Object... args) {
        logger.warn(format, args);
    }
    
    @Override
    public void error(String message) {
        logger.error(message);
    }
    
    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
    
    @Override
    public void debug(String message) {
        logger.debug(message);
    }
    
    @Override
    public void debug(String format, Object... args) {
        logger.debug(format, args);
    }
}

