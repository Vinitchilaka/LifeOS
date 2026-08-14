package com.lifeos.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLogService {

    @Async("taskExecutor")
    public void logTaskEvent(String eventDetails) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[ASYNC LOG START] Thread: " + threadName + " - Logging event: " + eventDetails);
        try {
            // Simulate slow processing (e.g. sending logs to external logging service)
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ASYNC LOG ERROR] Thread: " + threadName + " interrupted!");
        }
        System.out.println("[ASYNC LOG END] Thread: " + threadName + " - Logged successfully!");
    }
}
