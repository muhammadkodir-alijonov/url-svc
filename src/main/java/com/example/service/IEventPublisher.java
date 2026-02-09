package com.example.service;

import com.example.domain.Url;

/**
 * Interface for publishing domain events
 */
public interface IEventPublisher {
    void publishUrlCreated(Url url);

    void publishUrlAccessed(Url url, String userAgent, String ipAddress);

    void publishUrlUpdated(Url url);

    void publishUrlDeleted(String shortCode, String userId);
}
