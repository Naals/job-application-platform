package com.project.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY               = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request  = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        response.getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, finalCorrelationId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ctx -> {
                    MDC.put(MDC_KEY, finalCorrelationId);
                    return ctx.put(MDC_KEY, finalCorrelationId);
                })
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }
}