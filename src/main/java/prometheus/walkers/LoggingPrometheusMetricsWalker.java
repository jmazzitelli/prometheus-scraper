package prometheus.walkers;

import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import prometheus.types.Counter;
import prometheus.types.Gauge;
import prometheus.types.Histogram;
import prometheus.types.MetricFamily;
import prometheus.types.Summary;

/**
 * This implementation simply logs the metric values.
 */
public class LoggingPrometheusMetricsWalker implements PrometheusMetricsWalker {
    private static final Logger log = Logger.getLogger(LoggingPrometheusMetricsWalker.class);
    private Level logLevel;

    public LoggingPrometheusMetricsWalker() {
        this(null);
    }

    public LoggingPrometheusMetricsWalker(Level logLevel) {
        this.logLevel = (logLevel != null) ? logLevel : Level.DEBUG;
    }

    @Override
    public void walkStart() {
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
    }

    @Override
    public void walkMetricFamily(MetricFamily family, int index) {
        log.logf(getLogLevel(), "Metric Family [%s] of type [%s] has [%d] metrics: %s",
                family.getName(),
                family.getType(),
                family.getMetrics().size(),
                family.getHelp());
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter metric, int index) {
        log.logf(getLogLevel(), "COUNTER: %s%s=%f",
                metric.getName(),
                buildLabelListString(metric.getLabels()),
                metric.getValue());
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge metric, int index) {
        log.logf(getLogLevel(), "GAUGE: %s%s=%f",
                metric.getName(),
                buildLabelListString(metric.getLabels()),
                metric.getValue());
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary metric, int index) {
        log.logf(getLogLevel(), "SUMMARY: %s%s: count=%d, sum=%f, quantiles=%s",
                metric.getName(),
                buildLabelListString(metric.getLabels()),
                metric.getSampleCount(),
                metric.getSampleSum(),
                metric.getQuantiles());
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram metric, int index) {
        log.logf(getLogLevel(), "HISTOGRAM: %s%s: count=%d, sum=%f, buckets=%s",
                metric.getName(),
                buildLabelListString(metric.getLabels()),
                metric.getSampleCount(),
                metric.getSampleSum(),
                metric.getBuckets());
    }

    /**
     * The default implementations of the walk methods will log the metric data with this given log level.
     *
     * @return the log level
     */
    protected Level getLogLevel() {
        return this.logLevel;
    }

    protected String buildLabelListString(Map<String, String> labels) {
        return buildLabelListString(labels, "{", "}");
    }
}
