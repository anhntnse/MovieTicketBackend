package com.movie.ticket_booking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.log-endpoints", havingValue = "true", matchIfMissing = true)
public class EndpointCatalogLogger {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private volatile Integer localPort;

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        this.localPort = event.getWebServer().getPort();
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (var entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mapping = entry.getKey();
            HandlerMethod handler = entry.getValue();

            Set<String> paths = extractPaths(mapping);
            Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();

            for (String path : paths) {
                if (path == null || !path.startsWith("/api")) {
                    continue;
                }

                if (methods == null || methods.isEmpty()) {
                    endpoints.add(new Endpoint("ANY", path, handlerName(handler)));
                } else {
                    for (RequestMethod method : methods) {
                        endpoints.add(new Endpoint(method.name(), path, handlerName(handler)));
                    }
                }
            }
        }

        endpoints.sort(Comparator
                .comparing(Endpoint::path)
                .thenComparing(Endpoint::method)
                .thenComparing(Endpoint::handler)
        );

        String baseUrl = localPort == null
                ? "http://localhost" + "{port}"
                : "http://localhost:" + localPort;

        log.info("================ API ENDPOINTS ({} entry/entries) ================", endpoints.size());
        log.info("Base URL: {}", baseUrl);

        for (Endpoint endpoint : endpoints) {
            log.info("{} {}{}  ->  {}",
                    padRight(endpoint.method(), 6),
                    baseUrl,
                    endpoint.path(),
                    endpoint.handler()
            );
        }

        log.info("==================================================================");
    }

    private Set<String> extractPaths(RequestMappingInfo mapping) {
        try {
            // Spring Framework 6+ (PathPattern)
            if (mapping.getPathPatternsCondition() != null) {
                return mapping.getPathPatternsCondition().getPatternValues();
            }
        } catch (Throwable ignored) {
            // Fall through for older APIs.
        }

        // Fallback (legacy AntPathMatcher patterns)
        if (mapping.getPatternsCondition() != null) {
            return mapping.getPatternsCondition().getPatterns();
        }

        return Set.of();
    }

    private String handlerName(HandlerMethod handler) {
        return handler.getBeanType().getSimpleName() + "#" + handler.getMethod().getName();
    }

    private String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    private record Endpoint(String method, String path, String handler) {
    }
}
