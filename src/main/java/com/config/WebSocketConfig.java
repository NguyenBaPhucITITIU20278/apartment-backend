package com.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final WebSocketChannelInterceptor channelInterceptor;

    @Bean
    public ThreadPoolTaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192 * 4); // 32KB
        container.setMaxBinaryMessageBufferSize(8192 * 4); // 32KB
        container.setMaxSessionIdleTimeout(60000L);
        container.setAsyncSendTimeout(5000L);
        return container;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:8080",
                    "https://apartment-management-vpc4.onrender.com",
                    "https://apartment-backend-30kj.onrender.com"
                )
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app")
               .enableSimpleBroker("/topic", "/queue", "/user")
               .setHeartbeatValue(new long[]{10000, 10000})
               .setTaskScheduler(webSocketTaskScheduler());
        
        registry.setUserDestinationPrefix("/user");
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(15 * 1000) // 15 seconds
                   .setSendBufferSizeLimit(512 * 1024) // 512KB
                   .setMessageSizeLimit(128 * 1024) // 128KB
                   .setTimeToFirstMessage(30 * 1000); // 30 seconds

        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) throws Exception {
                        String token = extractToken(session);
                        if (token != null) {
                            // Validate token and set user principal
                            session.getAttributes().put("user", validateToken(token));
                        }
                        super.afterConnectionEstablished(session);
                    }
                };
            }
        });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                logger.info("Sending outbound message: {}", message);
                return message;
            }
        });
    }

    private String extractToken(org.springframework.web.socket.WebSocketSession session) {
        String token = session.getUri().getQuery();
        if (token != null && token.startsWith("token=")) {
            return token.substring(6);
        }
        return null;
    }

    private String validateToken(String token) {
        // Add your token validation logic here
        return token;
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        return true;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }
} 