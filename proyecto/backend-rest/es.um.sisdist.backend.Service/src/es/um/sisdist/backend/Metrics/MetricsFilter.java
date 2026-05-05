package es.um.sisdist.backend.Metrics;

import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;

@Provider
public class MetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START = "start";

    @Override
    public void filter(ContainerRequestContext req) {
        req.setProperty(START, System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) {


        Object startObj = req.getProperty(START);

        if (!(startObj instanceof Long)) {
            return;
        }

        long start = (long) req.getProperty(START);
        long duration = System.nanoTime() - start;

        String method = req.getMethod();

        java.util.List<String> uris = req.getUriInfo().getMatchedURIs();

        String path = uris.isEmpty() ? "unknown" : uris.get(0);

        int status = res.getStatus();

        MetricsRegistry.registry.timer(
            "http_server_requests",
            "method", method,
            "uri", path,
            "status", String.valueOf(status)
        ).record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);

        // tiempo de requests
        Timer.builder("http_request_timer")
        .tag("method", method)
                .tag("uri", path)
                .tag("status", String.valueOf(status))
                .register(MetricsRegistry.registry)
                .record(duration, TimeUnit.NANOSECONDS);

        // numero de requests
        Counter.builder("http_requests_total")
                .tag("method", method)
                .tag("uri", path)
                .tag("status", String.valueOf(status))
                .register(MetricsRegistry.registry)
                .increment();

        
    }

}