package com.swarm.dashboard.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${ai.api-key}")
    private String expectedKey;

    private static final Pattern AI_CALLBACK_PATTERN =
        Pattern.compile("^/api/simulations/[^/]+/(overview|issues|heatmap|wcag|fixes)$");

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String path = req.getRequestURI();
        boolean needsAuth = "POST".equalsIgnoreCase(req.getMethod())
                && AI_CALLBACK_PATTERN.matcher(path).matches();

        if (needsAuth) {
            String key = req.getHeader("X-API-Key");
            if (!expectedKey.equals(key)) {
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Invalid API key\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
