package com.example.service;

import com.example.dto.ClickEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;

@ApplicationScoped
public class EventPublisher {

    private static final Logger LOG = Logger.getLogger(EventPublisher.class);

    @Inject
    @Channel("click-events")
    Emitter<ClickEvent> clickEventEmitter;

    /**
     * Publish click event to Pulsar
     *
     * Fire-and-forget (non-blocking)
     */
    public void publishClickEvent(ClickEvent event) {
        try {
            clickEventEmitter.send(event);
            LOG.debugf("Published click event: %s", event.getShortCode());
        } catch (Exception e) {
            LOG.errorf("Failed to publish click event for %s: %s",
                    event.getShortCode(), e.getMessage());
            // Don't throw - analytics failure shouldn't block redirect
        }
    }

    /**
     * Publish click event asynchronously
     */
    public void publishClickEventAsync(ClickEvent event) {
        // Run in separate thread to not block caller
        new Thread(() -> publishClickEvent(event)).start();
    }
}