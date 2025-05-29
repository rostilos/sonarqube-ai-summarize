package org.perpectiveteam.plugins.aisummarize.config;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;

/*
    Project-specific variables are added to the sensor context,
    otherwise it is impossible to get non-Global ones in the CE execution context
 */
public class AiSummarizeProperySensor implements Sensor {

    private final Configuration configuration;

    public AiSummarizeProperySensor(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.name(getClass().getName());
    }

    @Override
    public void execute(SensorContext sensorContext) {
        configuration.get(AiSummarizeConfig.FILE_LIMIT)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(AiSummarizeConfig.FILE_LIMIT, v));

        configuration.get(AiSummarizeConfig.AI_PROVIDER)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(AiSummarizeConfig.AI_PROVIDER, v));

        configuration.get(AiSummarizeConfig.AI_MODEL)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(AiSummarizeConfig.AI_MODEL, v));
        configuration.get(AiSummarizeConfig.AI_CLIENT_API_KEY)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(AiSummarizeConfig.AI_CLIENT_API_KEY, v));
        configuration.get(AiSummarizeConfig.AI_PROMPT_TEMPLATE)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> sensorContext.addContextProperty(AiSummarizeConfig.AI_PROMPT_TEMPLATE, v));

    }

}
