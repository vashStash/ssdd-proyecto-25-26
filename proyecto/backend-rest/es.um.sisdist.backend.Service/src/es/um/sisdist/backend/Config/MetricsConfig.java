package es.um.sisdist.backend.Config;

import es.um.sisdist.backend.Metrics.MetricsRegistry;
import io.micrometer.core.instrument.binder.jvm.*;

public class MetricsConfig {

    public static void init() {
        new ClassLoaderMetrics().bindTo(MetricsRegistry.registry);
        new JvmMemoryMetrics().bindTo(MetricsRegistry.registry);
        new JvmGcMetrics().bindTo(MetricsRegistry.registry);
        new JvmThreadMetrics().bindTo(MetricsRegistry.registry);
        new JvmHeapPressureMetrics().bindTo(MetricsRegistry.registry);
    }
}
