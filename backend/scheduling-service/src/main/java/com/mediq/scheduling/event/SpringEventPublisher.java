package com.mediq.scheduling.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public <T> void publish(EventEnvelope<T> event) {
        log.info("Publishing domain event: type={}, id={}, source={}",
                event.eventType(), event.eventId(), event.source());
        applicationEventPublisher.publishEvent(event);
    }
}
