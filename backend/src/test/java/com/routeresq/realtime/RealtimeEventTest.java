package com.routeresq.realtime;

import com.routeresq.realtime.model.RealtimeEvent;
import com.routeresq.realtime.model.RealtimeEventType;
import com.routeresq.realtime.service.RealtimeEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RealtimeEventTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RealtimeEventPublisher publisher;

    @Test
    @DisplayName("Unit Test: RealtimeEvent Envelope Construction & Builder")
    void testRealtimeEventEnvelopeBuilder() {
        UUID eventId = UUID.randomUUID();
        UUID simId = UUID.randomUUID();

        RealtimeEvent<Map<String, String>> event = RealtimeEvent.<Map<String, String>>builder()
                .eventId(eventId)
                .eventType(RealtimeEventType.ORDER_DELIVERED)
                .entityType("ORDER")
                .entityId("ORD-101")
                .sequence(42)
                .simulationId(simId)
                .payload(Map.of("customer", "Acme Corp"))
                .build();

        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getEventType()).isEqualTo(RealtimeEventType.ORDER_DELIVERED);
        assertThat(event.getEntityType()).isEqualTo("ORDER");
        assertThat(event.getSequence()).isEqualTo(42);
        assertThat(event.getPayload()).containsEntry("customer", "Acme Corp");
    }

    @Test
    @DisplayName("Unit Test: RealtimeEventPublisher Mirroring to /topic/operations")
    void testPublisherMirroring() {
        UUID simId = UUID.randomUUID();
        Map<String, String> payload = Map.of("orderNumber", "ORD-101");

        RealtimeEvent<Map<String, String>> event = publisher.publish(
                "/topic/simulation/" + simId,
                RealtimeEventType.ORDER_DELIVERED,
                "ORDER",
                "ORD-101",
                payload,
                simId,
                null,
                null
        );

        assertThat(event).isNotNull();
        assertThat(event.getSequence()).isGreaterThan(0);
        verify(messagingTemplate).convertAndSend(eq("/topic/simulation/" + simId), eq(event));
        verify(messagingTemplate).convertAndSend(eq("/topic/operations"), eq(event));
    }
}
