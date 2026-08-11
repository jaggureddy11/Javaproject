package com.routeresq.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables Spring @Async support with a dedicated thread pool for the
 * Timefold solver.  The solver can take up to 60 s, so it must NOT
 * run on a web-server thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "solverExecutor")
    public Executor solverExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("solver-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90);
        executor.initialize();
        return executor;
    }
}
