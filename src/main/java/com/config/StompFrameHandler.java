package com.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class StompFrameHandler implements StompSessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(StompFrameHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("STOMP Connection established - Session ID: {}", session.getSessionId());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception while handling STOMP frame. Session ID: {}, Command: {}", session.getSessionId(), command, exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error("Got a transport error. Session ID: {}", session.getSessionId(), exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        logger.debug("Getting payload type for headers: {}", headers);
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        logger.info("Received frame - Headers: {}, Payload: {}", headers, payload);
    }
} 