package com.example.backend.interceptor;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
public class WebsocketIdentifyInterceptor extends HttpSessionHandshakeInterceptor {

    private final UserRepository userRepository;

    @Autowired
    public WebsocketIdentifyInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        String token = request.getURI().getQuery();
        if (token == null || !token.startsWith("token=")) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        token = token.substring("token=".length()); // 提取真正的 JWT 令牌

        if (token.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // Validate the JWT and extract user information
        try {
            Claims claims = JwtUtil.parseJWT(token);
            String userId = claims.getSubject();

            Optional<User> user = userRepository.findById(Integer.parseInt(userId));
            if (user.isEmpty()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            // Store user information in the WebSocket session attributes
            attributes.put("user", user.get());
            return super.beforeHandshake(request, response, wsHandler, attributes);
        } catch (Exception e) {
            // Handle invalid JWT
            System.err.println(e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }
}
