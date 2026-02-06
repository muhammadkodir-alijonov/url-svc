package com.example.service;

import com.example.domain.Url;

/**
 * Interface for publishing domain events
 */
public interface IEventPublisher {

    /**
     * Publish URL created event
     *
     * @param url the created URL entity
     */
    void publishUrlCreated(Url url);

    /**
     * Publish URL accessed event
     *
     * @param url the accessed URL entity
     * @param userAgent user agent string from request
     * @param ipAddress IP address of the client
     */
    void publishUrlAccessed(Url url, String userAgent, String ipAddress);

    /**
     * Publish URL updated event
     *
     * @param url the updated URL entity
     */
    void publishUrlUpdated(Url url);

    /**
     * Publish URL deleted event
     *
     * @param shortCode the short code of deleted URL
     * @param userId the user ID who deleted the URL
     */
    void publishUrlDeleted(String shortCode, String userId);
}
