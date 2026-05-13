package ru.senla.scooterrental.web.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());

        log.info(
                "HTTP request started: method={}, uri={}, remoteAddr={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()
        );

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");

        long durationMs = startTime == null
                ? 0
                : System.currentTimeMillis() - startTime;

        log.info(
                "HTTP request completed: method={}, uri={}, status={}, durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs
        );
    }
}