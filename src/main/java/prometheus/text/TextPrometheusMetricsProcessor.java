package prometheus.text;

import java.io.InputStream;

import prometheus.PrometheusMetricsProcessor;
import prometheus.types.MetricFamily;
import prometheus.walkers.PrometheusMetricsWalker;

/**
 * This will iterate over a list of Prometheus metrics that are given as text data.
 */
public class TextPrometheusMetricsProcessor extends PrometheusMetricsProcessor<MetricFamily> {
    public TextPrometheusMetricsProcessor(InputStream inputStream, PrometheusMetricsWalker theWalker) {
        super(inputStream, theWalker);
    }

    @Override
    public TextPrometheusMetricDataParser createPrometheusMetricDataParser() {
        return new TextPrometheusMetricDataParser(getInputStream());
    }

    @Override
    protected MetricFamily convert(MetricFamily metricFamily) {
        return metricFamily; // no conversion necessary - our text parser already uses the common api
    }

}
