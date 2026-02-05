package com.example.service;

import com.example.dto.ClickEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class EventPublisher {

    private static final Logger LOG = Logger.getLogger(EventPublisher.class);

    @ConfigProperty(name = "quarkus.pulsar.client.serviceUrl", defaultValue = "pulsar://localhost:6650")
    String pulsarUrl;

    @ConfigProperty(name = "app.pulsar.topic", defaultValue = "persistent://public/default/click-events")
    String topic;

    private PulsarClient pulsarClient;
    private Producer<String> producer;

    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        try {
            LOG.infof("Initializing Pulsar client: %s", pulsarUrl);

            pulsarClient = PulsarClient.builder()
                    .serviceUrl(pulsarUrl)
                    .build();

            producer = pulsarClient.newProducer(Schema.STRING)
                    .topic(topic)
                    .producerName("url-service-producer")
                    .sendTimeout(5, TimeUnit.SECONDS)
                    .create();

            LOG.info("Pulsar producer initialized successfully");
        } catch (PulsarClientException e) {
            LOG.error("Failed to initialize Pulsar client", e);
            // Don't throw - let the service start even if Pulsar is down
        }
    }

    @PreDestroy
    void cleanup() {
        try {
            if (producer != null) {
                producer.close();
            }
            if (pulsarClient != null) {
                pulsarClient.close();
            }
            LOG.info("Pulsar client closed");
        } catch (PulsarClientException e) {
            LOG.error("Error closing Pulsar client", e);
        }
    }

    /**
     * Publish click event to Pulsar
     *
     * Fire-and-forget (non-blocking)
     */
    public void publishClickEvent(ClickEvent event) {
        if (producer == null) {
            LOG.warn("Pulsar producer not initialized, skipping event");
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            producer.sendAsync(json)
                    .thenAccept(msgId ->
                            LOG.debugf("Published click event: %s (msgId: %s)",
                                    event.getShortCode(), msgId))
                    .exceptionally(ex -> {
                        LOG.errorf("Failed to publish click event for %s: %s",
                                event.getShortCode(), ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            LOG.errorf("Error serializing click event for %s: %s",
                    event.getShortCode(), e.getMessage());
            // Don't throw - analytics failure shouldn't block redirect
        }
    }

    /**
     * Publish click event asynchronously with CompletableFuture
     */
    public CompletableFuture<Void> publishClickEventAsync(ClickEvent event) {
        return CompletableFuture.runAsync(() -> publishClickEvent(event));
    }

    /**
     * Check if Pulsar is connected
     */
    public boolean isConnected() {
        return producer != null && producer.isConnected();
    }
}