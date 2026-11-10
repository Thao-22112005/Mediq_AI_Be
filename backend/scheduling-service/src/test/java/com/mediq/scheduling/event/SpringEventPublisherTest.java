package com.mediq.scheduling.event;

import com.mediq.scheduling.event.payload.ScheduleApprovedEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SpringEventPublisher springEventPublisher;

    @Test
    @DisplayName("Should publish EventEnvelope matching standard format")
    void shouldPublishEventEnvelope() {
        UUID scheduleId = UUID.randomUUID();
        ScheduleApprovedEventPayload payload = new ScheduleApprovedEventPayload(
                scheduleId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        EventEnvelope<ScheduleApprovedEventPayload> event = EventEnvelope.of(
                "Schedule.Approved",
                "scheduling-service",
                payload
        );

        springEventPublisher.publish(event);

        verify(applicationEventPublisher).publishEvent(argThat((EventEnvelope<?> e) -> {
            assertThat(e.eventId()).isNotNull();
            assertThat(e.eventType()).isEqualTo("Schedule.Approved");
            assertThat(e.source()).isEqualTo("scheduling-service");
            assertThat(e.version()).isEqualTo(1);
            assertThat(e.occurredAt()).isNotNull();
            assertThat(e.payload()).isEqualTo(payload);
            return true;
        }));
    }
}
