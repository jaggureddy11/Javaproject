package com.routeresq.domain;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            boolean isDockerAvailable = DockerClientFactory.instance().isDockerAvailable();
            if (isDockerAvailable) {
                return ConditionEvaluationResult.enabled("Docker daemon is available for Testcontainers.");
            } else {
                return ConditionEvaluationResult.disabled("Docker daemon is not available. Skipping Testcontainers integration test.");
            }
        } catch (Throwable t) {
            return ConditionEvaluationResult.disabled("Docker daemon check failed: " + t.getMessage());
        }
    }
}
