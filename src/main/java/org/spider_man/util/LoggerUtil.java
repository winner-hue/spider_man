package org.spider_man.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.*;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LoggerUtil {
    public static String filename = "spider_man.log";
    public static long bufferSize = 8 * FileSize.KB_COEFFICIENT;
    public static String appenderName = "FILE";
    public static String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} [%file:%line] [%msg] %n";
    public static String filenamePattern = "spider_man.%d{yyyy-MM-dd}.%i.log";
    public static int maxHistory = 30;
    public static long maxFileSize = 50 * FileSize.MB_COEFFICIENT;
    public static Level level = Level.INFO;
    private static LoggerContext loggerContext = null;
    private static RollingFileAppender rollingFileAppender = null;
    private static TimeBasedRollingPolicy timeBasedRollingPolicy = null;
    private static SizeAndTimeBasedFNATP sizeAndTimeBasedFNATP = null;
    private static PatternLayoutEncoder encoder = null;
    private static ConsoleAppender consoleAppender = null;

    public static Logger getLogger(Class clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        loggerContext = logger.getLoggerContext();
        Appender appender = getAppender();
        logger.addAppender(appender);
        Appender consoleAppender = getConsoleAppender();
        logger.addAppender(consoleAppender);
        logger.setLevel(level);
        logger.setAdditive(false);
        return logger;
    }

    private synchronized static Appender getConsoleAppender() {
        consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(getLayout());
        consoleAppender.start();
        return consoleAppender;
    }

    private synchronized static Appender getAppender() {
        rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setFile(filename);
        rollingFileAppender.setRollingPolicy(getTriggeringPolicy());
        rollingFileAppender.setRollingPolicy(getRollingPolicy());
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setBufferSize(new FileSize(bufferSize));
        rollingFileAppender.setEncoder(getLayout());
        rollingFileAppender.setName(appenderName);
        rollingFileAppender.start();

        return rollingFileAppender;
    }

//    private static Encoder getEncoder() {
//        LayoutWrappingEncoder layoutWrappingEncoder = new LayoutWrappingEncoder();
////        layoutWrappingEncoder.setLayout(getLayout());
//        layoutWrappingEncoder.setParent(rollingFileAppender);
//        layoutWrappingEncoder.start();
//        return layoutWrappingEncoder;
//    }

    private static PatternLayoutEncoder getLayout() {
        encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        // 设置格式
        encoder.setPattern(pattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        return encoder;
    }

    private static RollingPolicy getRollingPolicy() {
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);
        rollingPolicy.setFileNamePattern(filenamePattern);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setMaxHistory(maxHistory);
        rollingPolicy.start();
        return rollingPolicy;
    }

    private static RollingPolicy getTriggeringPolicy() {
        timeBasedRollingPolicy = new TimeBasedRollingPolicy();
        timeBasedRollingPolicy.setFileNamePattern(filenamePattern);
        timeBasedRollingPolicy.setMaxHistory(maxHistory);
        timeBasedRollingPolicy.setContext(loggerContext);
        timeBasedRollingPolicy.setParent(rollingFileAppender);
        timeBasedRollingPolicy.start();

        timeBasedRollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(getTimeBasedFileNamingAndTriggeringPolicy());
        return timeBasedRollingPolicy;
    }

    private static TimeBasedFileNamingAndTriggeringPolicy getTimeBasedFileNamingAndTriggeringPolicy() {
        sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP();
        sizeAndTimeBasedFNATP.setMaxFileSize(new FileSize(maxFileSize));
        sizeAndTimeBasedFNATP.setTimeBasedRollingPolicy(timeBasedRollingPolicy);
        sizeAndTimeBasedFNATP.setContext(loggerContext);
        sizeAndTimeBasedFNATP.start();
        return sizeAndTimeBasedFNATP;
    }

}
