package com.routeresq.realtime.service;

import com.routeresq.realtime.model.RealtimeEvent;
import com.routeresq.realtime.model.RealtimeEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RealtimeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RealtimeEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicLong globalSequence = new AtomicLong(1);

    public RealtimeEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public <T> RealtimeEvent<T> publish(String topic,
                                      RealtimeEventType eventType,
                                      String entityType,
                                      String entityId,
                                      T payload,
                                      UUID simulationId,
                                      UUID incidentId,
                                      UUID optimizationRunId) {
        long seq = globalSequence.getAndIncrement();
        RealtimeEvent<T> event = RealtimeEvent.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .occurredAt(Instant.now())
                .sequence(seq)
                .simulationId(simulationId)
                .incidentId(incidentId)
                .optimizationRunId(optimizationRunId)
                .payload(payload)
                .build();

        // Broadcast to target topic
        messagingTemplate.convertAndSend(topic, event);

        // Mirror high-level business events to global operations stream /topic/operations
        if (eventType != RealtimeEventType.VEHICLE_POSITION_UPDATED) {
            messagingTemplate.convertAndSend("/topic/operations", event);
            log.info("Broadcasted RealtimeEvent [{}] to topic {} and /topic/operations: seq={}", eventType, topic, seq);
        }

        return event;
    }

    public <T> RealtimeEvent<T> publish(String topic, RealtimeEventType eventType, T payload) {
        return publish(topic, eventType, "SYSTEM", null, payload, null, null, null);
    }
}
