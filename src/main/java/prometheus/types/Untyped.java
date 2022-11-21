package prometheus.types;

public class Untyped extends Metric {

    public static class Builder extends Metric.Builder<Builder> {
        private double value = Double.NaN;

        public Untyped build() {
            return new Untyped(this);
        }

        public Builder setValue(double value) {
            this.value = value;
            return this;
        }
    }

    private final double value;

    public Untyped(Builder builder) {
        super(builder);
        this.value = builder.value;
    }

    public double getValue() {
        return value;
    }
}
