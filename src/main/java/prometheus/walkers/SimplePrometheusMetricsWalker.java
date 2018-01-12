package prometheus.walkers;

import java.net.URL;

import prometheus.types.Counter;
import prometheus.types.Gauge;
import prometheus.types.Histogram;
import prometheus.types.MetricFamily;
import prometheus.types.Summary;

public class SimplePrometheusMetricsWalker implements PrometheusMetricsWalker {

    private final URL url;

    public SimplePrometheusMetricsWalker() {
        this(null);
    }

    /**
     * Use this constructor if you know the URL where the metric data came from.
     *
     * @param url the protocol endpoint that supplied the Prometheus metric data
     */
    public SimplePrometheusMetricsWalker(URL url) {
        this.url = url;
    }

    @Override
    public void walkStart() {
        if (url != null) {
            System.out.println("Scraping metrics from Prometheus protocol endpoint: " + url);
        }
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
        if (metricsProcessed == 0) {
            System.out.println("There are no metrics");
        }
    }

    @Override
    public void walkMetricFamily(MetricFamily family, int index) {
        System.out.printf("* %s (%s): %s\n", family.getName(), family.getType(), family.getHelp());
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter metric, int index) {
        System.out.printf("  +%2d. %s%s [%f]\n",
                index,
                metric.getName(),
                buildLabelListString(metric.getLabels(), "{", "}"),
                metric.getValue());
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge metric, int index) {
        System.out.printf("  +%2d. %s%s [%f]\n",
                index,
                metric.getName(),
                buildLabelListString(metric.getLabels(), "{", "}"),
                metric.getValue());
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary metric, int index) {
        System.out.printf("  +%2d. %s%s [%d/%f] {%s}\n",
                index,
                metric.getName(),
                buildLabelListString(metric.getLabels(), "{", "}"),
                metric.getSampleCount(),
                metric.getSampleSum(),
                metric.getQuantiles());
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram metric, int index) {
        System.out.printf("  +%2d. %s%s [%d/%f] {%s}\n",
                index,
                metric.getName(),
                buildLabelListString(metric.getLabels(), "{", "}"),
                metric.getSampleCount(),
                metric.getSampleSum(),
                metric.getBuckets());
    }
}
