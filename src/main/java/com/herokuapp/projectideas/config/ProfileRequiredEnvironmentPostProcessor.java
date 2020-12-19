package com.herokuapp.projectideas.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * EnvironmentPostProcessor that requires that either the dev or production profile
 * is active when the application is run.
 *
 * This class must be registered in src/main/resources/META-INF/spring.factories
 */
public class ProfileRequiredEnvironmentPostProcessor
    implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(
        ConfigurableEnvironment environment,
        SpringApplication application
    ) {
        List<String> activeProfiles = Arrays.asList(
            environment.getActiveProfiles()
        );

        if (
            !activeProfiles.contains("dev") && !activeProfiles.contains("prod")
        ) {
            throw new IllegalStateException(
                "projectideas must be run with either the 'dev' or 'prod' profile active."
            );
        }
    }
}
