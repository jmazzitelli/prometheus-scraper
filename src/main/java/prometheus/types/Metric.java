package prometheus.types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Superclass to all metrics. All metrics have name and labels.
 */
public abstract class Metric {

    public abstract static class Builder<B extends Builder<?>> {
        private String name;
        private Map<String, String> labels;

        public B setName(String name) {
            this.name = name;
            return (B) this;
        }

        public B addLabel(String name, String value) {
            if (labels == null) {
                labels = new LinkedHashMap<>(); // used linked hash map to retain ordering
            }
            labels.put(name, value);
            return (B) this;
        }

        public B addLabels(Map<String, String> map) {
            if (labels == null) {
                labels = new LinkedHashMap<>(); // used linked hash map to retain ordering
            }
            labels.putAll(map);
            return (B) this;
        }

        public abstract <T extends Metric> T build();
    }

    private final String name;
    private final Map<String, String> labels;

    protected Metric(Builder<?> builder) {
        if (builder.name == null) {
            throw new IllegalArgumentException("Need to set name");
        }

        this.name = builder.name;
        this.labels = builder.labels;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getLabels() {
        if (labels == null) {
            return Collections.emptyMap();
        }
        return labels;
    }
}
