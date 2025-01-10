package com.example.backend.config;

import com.example.backend.handler.AudioWebSocketHandler;
import com.example.backend.interceptor.WebsocketIdentifyInterceptor;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final UserRepository userRepository;

    @Autowired
    public WebSocketConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 端点
        registry.addHandler(audioWebSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new WebsocketIdentifyInterceptor(userRepository));
    }

    @Bean
    public WebSocketHandler audioWebSocketHandler() {
        return new AudioWebSocketHandler();
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter(); // 启用 Spring WebSocket 支持
    }
}
