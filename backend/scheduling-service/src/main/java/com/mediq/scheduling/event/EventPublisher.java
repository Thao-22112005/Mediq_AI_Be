package com.mediq.scheduling.event;

public interface EventPublisher {
    <T> void publish(EventEnvelope<T> event);
}
