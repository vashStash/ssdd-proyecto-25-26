package es.um.sisdist.backend.Service;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import es.um.sisdist.backend.Metrics.MetricsRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

@Path("/metrics")
public class MetricsEndpoint {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String metrics() {

        Counter.builder("test_counter")
            .register(MetricsRegistry.registry)
            .increment();

            
        return MetricsRegistry.registry.scrape();
    }
}