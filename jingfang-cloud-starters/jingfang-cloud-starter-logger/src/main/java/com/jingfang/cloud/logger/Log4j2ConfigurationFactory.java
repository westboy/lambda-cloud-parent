package com.jingfang.cloud.logger;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.*;
import org.apache.logging.log4j.core.appender.rolling.action.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.lookup.StrLookup;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log4j2ConfigurationFactory
 *
 * @author jpjoo
 */
@Order(10)
@Plugin(name = "Log4j2ConfigurationFactory", category = ConfigurationFactory.CATEGORY)
public class Log4j2ConfigurationFactory extends ConfigurationFactory {

    private static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{".yml"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        int i = COUNT.incrementAndGet();
        if (i == 1) {
            return null;
        }
        return new Log4j2Configuration(loggerContext, source);
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
        return LoggerContext.getContext(false).getConfiguration();
    }


    @SuppressWarnings({"squid:S110", "squid:S1075"})
    public static class Log4j2Configuration extends YamlConfiguration {
        private static final String PATTERN = "[%-5p] %d{ISO8601} [%t] %c{1} - %m%n%throwable";
        private static final String JF_APPENDER = "JFAppender";
        private static final String PATH_SEPARATOR = "/";
        private static final String DEFAULT_PATH = "/logs";
        private static final String LOGGING_PATH = "spring:logging.path";
        private static final String LOGGING_FILE = "/server.log";
        private static final String LOGGING_PATTERN = "/server-%d{yyyy-MM-dd}-%i.gz";
        private static final String GLOB = "server-????-??-??-{?,??}.gz";
        private static final String FILE_SIZE = "50M";
        private static final int MIN = 1;
        private static final int MAX = 24;
        private static final String SCHEDULE = "0 0 0 * * ? *";
        private static final Duration STORE_DURATION = Duration.parse("7d");

        public Log4j2Configuration(LoggerContext loggerContext, ConfigurationSource configSource) {
            super(loggerContext, configSource);
        }

        @Override
        protected void doConfigure() {
            super.doConfigure();
            LoggerConfig root = getRootLogger();
            StrLookup resolver = getStrSubstitutor().getVariableResolver();
            String path = Optional.ofNullable(resolver.lookup(LOGGING_PATH)).orElse(DEFAULT_PATH);
            final PatternLayout layout = PatternLayout.newBuilder()
                    .withPattern(PATTERN).withConfiguration(this).withDisableAnsi(true).build();
            TriggeringPolicy policy2 = SizeBasedTriggeringPolicy.createPolicy(FILE_SIZE);
            TriggeringPolicy policy1 = CronTriggeringPolicy.createPolicy(this, Boolean.TRUE.toString(), SCHEDULE);
            TriggeringPolicy polices = CompositeTriggeringPolicy.createPolicy(policy1, policy2);
            PathCondition condition1 = IfFileName.createNameCondition(GLOB, null);
            PathCondition condition2 = IfLastModified.createAgeCondition(STORE_DURATION);
            PathCondition[] pathConditions = new PathCondition[]{condition1, condition2};
            DeleteAction deleteAction = DeleteAction.createDeleteAction(path, true, 1, false, null,
                    pathConditions, null, this);
            DefaultRolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
                    .withMin(String.valueOf(MIN))
                    .withMax(String.valueOf(MAX))
                    .withCustomActions(new Action[]{deleteAction})
                    .build();

            Map<String, Appender> appenderMap = getAppenders();
            appenderMap.values().stream().filter(ConsoleAppender.class::isInstance).forEach(appender -> this.removeAppender(appender.getName()));
            ConsoleAppender consoleAppender = ConsoleAppender.createDefaultAppenderForLayout(layout);
            consoleAppender.start();
            this.addAppender(consoleAppender);
            root.addAppender(consoleAppender, null, null);

            RollingFileAppender legacy = this.getAppender(JF_APPENDER);
            if (legacy != null && DEFAULT_PATH.equals(path)) {
                String filename = legacy.getFileName();
                path = filename.substring(0, filename.lastIndexOf(PATH_SEPARATOR));
            }
            removeAppender(JF_APPENDER);
            RollingFileAppender jfAppender = RollingFileAppender.newBuilder()
                    .setName(JF_APPENDER)
                    .setLayout(layout)
                    .setConfiguration(this)
                    .withFileName(path + LOGGING_FILE)
                    .withFilePattern(path + LOGGING_PATTERN)
                    .withPolicy(polices)
                    .withStrategy(strategy)
                    .build();
            jfAppender.start();
            this.addAppender(jfAppender);
            root.addAppender(jfAppender, null, null);

        }

    }
}
