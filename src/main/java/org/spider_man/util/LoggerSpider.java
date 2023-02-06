package org.spider_man.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.*;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerSpider {
    private static TimeBasedFileNamingAndTriggeringPolicy timeBasedFileNamingAndTriggeringPolicy;

    public static Logger getLogger(Class clazz) {
        String fileName = "logs/spider_man.log";
        return getLogger(clazz, fileName);
    }

    public static Logger getLogger(Class clazz, String fileName) {
        long bufferSize = 4 * FileSize.KB_COEFFICIENT;
        return getLogger(clazz, fileName, bufferSize);
    }

    public static Logger getLogger(Class clazz, String fileName, Long bufferSize) {
        String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} [%file:%line] [%msg] %n";
        return getLogger(clazz, fileName, bufferSize, pattern);
    }

    public static Logger getLogger(Class clazz, String fileName, Long bufferSize, String pattern) {
        int maxHistory = 30;
        return getLogger(clazz, fileName, bufferSize, pattern, maxHistory);
    }

    public static Logger getLogger(Class clazz, String fileName, Long bufferSize, String pattern, int maxHistory) {
        long maxFileSize = 50 * FileSize.MB_COEFFICIENT;
        return getLogger(clazz, fileName, bufferSize, pattern, maxHistory, maxFileSize);
    }

    public static Logger getLogger(Class clazz, String fileName, Long bufferSize, String pattern, int maxHistory, Long maxFileSize) {
        Level level = Level.INFO;
        return getLogger(clazz, fileName, bufferSize, pattern, maxHistory, maxFileSize, level);
    }

    public static Logger getLogger(Class clazz, String fileName, Long bufferSize, String pattern, int maxHistory, Long maxFileSize, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        String filenamePattern = fileName.replace(".log", "") + ".%d{yyyy-MM-dd}.%i.log";
        Appender appender = getAppender(loggerContext, fileName, filenamePattern, maxHistory, bufferSize, pattern, maxFileSize);
        Appender consoleAppender = getConsoleAppender(loggerContext, pattern);
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(clazz.getName());
        logger.addAppender(appender);
        logger.addAppender(consoleAppender);
        logger.setLevel(level);
        logger.setAdditive(false);
        return logger;
    }

    private synchronized static Appender getConsoleAppender(LoggerContext loggerContext, String pattern) {
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(loggerContext);
        LayoutWrappingEncoder layoutWrappingEncoder = new LayoutWrappingEncoder();
        layoutWrappingEncoder.setLayout(getLayout(loggerContext, pattern));
        layoutWrappingEncoder.setParent(consoleAppender);
        layoutWrappingEncoder.start();
        consoleAppender.setEncoder(layoutWrappingEncoder);
        consoleAppender.start();
        return consoleAppender;
    }


    private synchronized static Appender getAppender(LoggerContext loggerContext, String fileName, String fileNamePattern, int maxHistory, Long bufferSize, String pattern, Long maxFileSize) {
        RollingFileAppender rollingFileAppender = new RollingFileAppender();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setFile(fileName);
        rollingFileAppender.setRollingPolicy(getTriggeringPolicy(fileNamePattern, maxHistory, loggerContext, rollingFileAppender, maxFileSize));
        rollingFileAppender.setRollingPolicy(getRollingPolicy(fileNamePattern, rollingFileAppender, loggerContext, maxHistory));
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setBufferSize(new FileSize(bufferSize));
        rollingFileAppender.setEncoder(getEncoder(loggerContext, rollingFileAppender, pattern));
        rollingFileAppender.setName("FILE");
        rollingFileAppender.start();
        return rollingFileAppender;
    }

    private static Encoder getEncoder(LoggerContext loggerContext, RollingFileAppender rollingFileAppender, String pattern) {
        LayoutWrappingEncoder layoutWrappingEncoder = new LayoutWrappingEncoder();
        layoutWrappingEncoder.setLayout(getLayout(loggerContext, pattern));
        layoutWrappingEncoder.setParent(rollingFileAppender);
        layoutWrappingEncoder.start();
        return layoutWrappingEncoder;
    }

    private static Layout getLayout(LoggerContext loggerContext, String pattern) {
        PatternLayout layout = new PatternLayout();
        layout.setPattern(pattern);
        layout.setContext(loggerContext);
        layout.start();
        return layout;
    }


    private static RollingPolicy getTriggeringPolicy(String fileNamePattern, int maxHistory, LoggerContext loggerContext, RollingFileAppender rollingFileAppender, Long maxFileSize) {
        TimeBasedRollingPolicy timeBasedRollingPolicy = new TimeBasedRollingPolicy();
        timeBasedRollingPolicy.setFileNamePattern(fileNamePattern);
        timeBasedRollingPolicy.setMaxHistory(maxHistory);
        timeBasedRollingPolicy.setContext(loggerContext);
        timeBasedRollingPolicy.setParent(rollingFileAppender);
        timeBasedRollingPolicy.start();
        timeBasedFileNamingAndTriggeringPolicy = getTimeBasedFileNamingAndTriggeringPolicy(maxFileSize, timeBasedRollingPolicy, loggerContext);
        timeBasedRollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedFileNamingAndTriggeringPolicy);
        return timeBasedRollingPolicy;
    }

    private static RollingPolicy getRollingPolicy(String fileNamePattern, RollingFileAppender rollingFileAppender, LoggerContext loggerContext, int maxHistory) {
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedFileNamingAndTriggeringPolicy);
        rollingPolicy.setFileNamePattern(fileNamePattern);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setMaxHistory(maxHistory);
        rollingPolicy.start();
        return rollingPolicy;
    }

    private static TimeBasedFileNamingAndTriggeringPolicy getTimeBasedFileNamingAndTriggeringPolicy(long maxFileSize, TimeBasedRollingPolicy timeBasedRollingPolicy, LoggerContext loggerContext) {
        SizeAndTimeBasedFNATP sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP();
        sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(maxFileSize));
        sizeAndTimeBasedFNATP.setTimeBasedRollingPolicy(timeBasedRollingPolicy);
        sizeAndTimeBasedFNATP.setContext(loggerContext);
        sizeAndTimeBasedFNATP.start();
        return sizeAndTimeBasedFNATP;
    }

}
