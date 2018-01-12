package prometheus.binary;

import java.io.IOException;
import java.io.InputStream;

import io.prometheus.client.Metrics;
import io.prometheus.client.Metrics.MetricFamily;
import prometheus.PrometheusMetricDataParser;

/**
 * Provides a method that can scrape Permetheus binary metric data from input streams.
 */
public class BinaryPrometheusMetricDataParser extends PrometheusMetricDataParser<MetricFamily> {

    /**
     * Provides the input stream where the parser will look for metric data.
     * NOTE: this object will not own this stream - it will never attempt to close it.
     *
     * @param inputStream the stream where the metric data can be found
     */
    public BinaryPrometheusMetricDataParser(InputStream inputStream) {
        super(inputStream);
    }

    public MetricFamily parse() throws IOException {
        return Metrics.MetricFamily.parseDelimitedFrom(getInputStream());
    }
}
