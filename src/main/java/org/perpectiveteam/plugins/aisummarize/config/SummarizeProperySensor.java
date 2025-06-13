package org.perpectiveteam.plugins.aisummarize.config;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;

/*
    Project-specific variables are added to the sensor context,
    otherwise it is impossible to get non-Global ones in the CE execution context
 */
public class SummarizeProperySensor implements Sensor {

    private final Configuration configuration;

    public SummarizeProperySensor(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.name(getClass().getName());
    }

    @Override
    public void execute(SensorContext sensorContext) {
        configuration.get(SummarizeConfig.FILE_LIMIT)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.FILE_LIMIT, v));
        configuration.get(SummarizeConfig.AI_PROVIDER)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.AI_PROVIDER, v));
        configuration.get(SummarizeConfig.AI_MODEL)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.AI_MODEL, v));
        configuration.get(SummarizeConfig.AI_CLIENT_API_KEY)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.AI_CLIENT_API_KEY, v));
        configuration.get(SummarizeConfig.AI_PROMPT_TEMPLATE)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.AI_PROMPT_TEMPLATE, v));
        configuration.get(SummarizeConfig.IS_ENABLED)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.IS_ENABLED, v));
        configuration.get(SummarizeConfig.FILE_MAX_LINES)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(SummarizeConfig.FILE_MAX_LINES, v));
    }

}
