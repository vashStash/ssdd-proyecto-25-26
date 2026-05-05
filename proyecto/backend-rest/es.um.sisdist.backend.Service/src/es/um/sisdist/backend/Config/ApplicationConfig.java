package es.um.sisdist.backend.Config;

import org.glassfish.jersey.server.ResourceConfig;
import es.um.sisdist.backend.Service.MetricsEndpoint;
import jakarta.ws.rs.ApplicationPath;
import es.um.sisdist.backend.Metrics.MetricsFilter;

public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {

        // registrar métricas
        register(MetricsEndpoint.class);
        register(MetricsFilter.class);

        // inicializar métricas JVM
        MetricsConfig.init();

        // opcional: escanear recursos automáticamente
        packages("es.um.sisdist.backend");
    }
}