package prometheus_async_histogram;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

import java.util.concurrent.TimeUnit;

public class AsyncHistogram<T> {

    private final Histogram histogram;
    private final Cache<T, Timer> timers;

    private AsyncHistogram(Histogram histogram, Cache<T, Timer> timers) {
        this.histogram = histogram;
        this.timers = timers;
    }

    public static <T> Builder<T> build() {
        return new Builder<>();
    }

    public Child<T> labels(String... labels) {
        return new Child<>(histogram.labels(labels), timers);
    }

    public void start(T id) {
        timers.put(id, new Timer());
    }

    public void observe(T id) {
        Timer timer = timers.getIfPresent(id);
        if (timer != null) {
            histogram.observe(timer.duration());
        }
    }

    static class Timer {

        private final double NANOSECONDS_PER_MILLISECOND = 1E6;

        private final long startTime;

        Timer(long startTime) {
            this.startTime = startTime;
        }

        Timer() {
            this(System.nanoTime());
        }

        double duration() {
            return (System.nanoTime() - startTime) / NANOSECONDS_PER_MILLISECOND;
        }

    }

    public static class Child<T> {

        private final Histogram.Child child;
        private final Cache<T, Timer> timers;

        Child(Histogram.Child child, Cache<T, Timer> timers) {
            this.child = child;
            this.timers = timers;
        }

        public void start(T id) {
            timers.put(id, new Timer());
        }

        public void observe(T id) {
            Timer timer = timers.getIfPresent(id);
            if (timer != null) {
                child.observe(timer.duration());
            }
        }

    }

    public static class Builder<T> {

        private Histogram.Builder histogramBuilder = Histogram.build();
        private CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        Builder() {
        }

        public Builder<T> name(String name) {
            histogramBuilder.name(name);
            return this;
        }

        public Builder<T> help(String help) {
            histogramBuilder.help(help);
            return this;
        }

        public Builder<T> buckets(double... buckets) {
            histogramBuilder.buckets(buckets);
            return this;
        }

        public Builder<T> labelNames(String... labelNames) {
            histogramBuilder.labelNames(labelNames);
            return this;
        }

        public Builder<T> expireAfterAccess(long duration, TimeUnit timeUnit) {
            cacheBuilder.expireAfterAccess(duration, timeUnit);
            return this;
        }

        public Builder<T> expireAfterWrite(long duration, TimeUnit timeUnit) {
            cacheBuilder.expireAfterWrite(duration, timeUnit);
            return this;
        }

        public AsyncHistogram<T> register() {
            return new AsyncHistogram<>(histogramBuilder.register(), cacheBuilder.build());
        }

        public AsyncHistogram<T> register(CollectorRegistry registry) {
            return new AsyncHistogram<>(histogramBuilder.register(registry), cacheBuilder.build());
        }

    }
}
