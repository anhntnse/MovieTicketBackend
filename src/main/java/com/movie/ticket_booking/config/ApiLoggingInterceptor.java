package com.movie.ticket_booking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Slf4j
@Component
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private static final String ATTR_START_NANOS = ApiLoggingInterceptor.class.getName() + ".startNanos";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START_NANOS, System.nanoTime());

        String query = Optional.ofNullable(request.getQueryString()).map(q -> "?" + q).orElse("");
        String handlerName = resolveHandlerName(handler);

        log.info("--> {} {}{} handler={}", request.getMethod(), request.getRequestURI(), query, handlerName);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        long durationMs = 0L;
        Object start = request.getAttribute(ATTR_START_NANOS);
        if (start instanceof Long startNanos) {
            durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        }

        String query = Optional.ofNullable(request.getQueryString()).map(q -> "?" + q).orElse("");
        String handlerName = resolveHandlerName(handler);

        if (ex != null) {
            log.error("<-- {} {}{} status={} durationMs={} handler={} error={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    query,
                    response.getStatus(),
                    durationMs,
                    handlerName,
                    ex.toString(),
                    ex
            );
            return;
        }

        log.info("<-- {} {}{} status={} durationMs={} handler={}",
                request.getMethod(),
                request.getRequestURI(),
                query,
                response.getStatus(),
                durationMs,
                handlerName
        );
    }

    private String resolveHandlerName(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
        }
        return handler == null ? "unknown" : handler.getClass().getSimpleName();
    }
}
