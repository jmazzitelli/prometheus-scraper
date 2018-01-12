package prometheus.types;

public class Gauge extends Metric {

    public static class Builder extends Metric.Builder<Builder> {
        private double value = Double.NaN;

        public Gauge build() {
            return new Gauge(this);
        }

        public Builder setValue(double value) {
            this.value = value;
            return this;
        }
    }

    private final double value;

    public Gauge(Builder builder) {
        super(builder);
        this.value = builder.value;
    }

    public double getValue() {
        return value;
    }
}
