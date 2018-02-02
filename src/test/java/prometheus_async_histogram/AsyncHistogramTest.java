package prometheus_async_histogram;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;

import java.util.UUID;

public class AsyncHistogramTest {

    private CollectorRegistry registry;
    private AsyncHistogram<UUID> asyncHistogram;

    @Before
    public void setup() {
        registry = new CollectorRegistry();
        asyncHistogram = AsyncHistogram.<UUID>build().register(registry);
    }

}
