package com.example.service.Impl;

import com.example.domain.Url;
import com.example.dto.ClickEvent;
import com.example.service.IEventPublisher;
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

/**
 * Implementation of event publishing operations using Apache Pulsar
 */
@ApplicationScoped
public class EventPublisher implements IEventPublisher {

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

    public CompletableFuture<Void> publishClickEventAsync(ClickEvent event) {
        return CompletableFuture.runAsync(() -> publishClickEvent(event));
    }

    public boolean isConnected() {
        return producer != null && producer.isConnected();
    }

    @Override
    public void publishUrlCreated(Url url) {

    }

    @Override
    public void publishUrlAccessed(Url url, String userAgent, String ipAddress) {

    }

    @Override
    public void publishUrlUpdated(Url url) {

    }

    @Override
    public void publishUrlDeleted(String shortCode, String userId) {

    }
}