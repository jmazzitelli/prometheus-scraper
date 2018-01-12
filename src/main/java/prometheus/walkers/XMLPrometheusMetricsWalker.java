package prometheus.walkers;

import java.net.URL;

import prometheus.Util;
import prometheus.types.Counter;
import prometheus.types.Gauge;
import prometheus.types.Histogram;
import prometheus.types.Histogram.Bucket;
import prometheus.types.MetricFamily;
import prometheus.types.MetricType;
import prometheus.types.Summary;
import prometheus.types.Summary.Quantile;

public class XMLPrometheusMetricsWalker implements PrometheusMetricsWalker {

    private final URL url;

    public XMLPrometheusMetricsWalker() {
        this(null);
    }

    /**
     * Use this constructor if you know the URL where the metric data came from.
     *
     * @param metricFamilies
     * @param url the protocol endpoint that supplied the Prometheus metric data
     */
    public XMLPrometheusMetricsWalker(URL url) {
        this.url = url;
    }

    @Override
    public void walkStart() {
        System.out.printf("<metricFamilies>\n");

        // only provide the URL endpoint element if we know the URL where the metrics came from
        if (url != null) {
            System.out.printf("  <url>%s</url>\n", url);
        }
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
        if (familiesProcessed > 0) {
            System.out.printf("  </metricFamily>\n");
        }
        System.out.println("</metricFamilies>");
    }

    @Override
    public void walkMetricFamily(MetricFamily family, int index) {
        if (index > 0) {
            System.out.printf("  </metricFamily>\n");
        }

        System.out.printf("  <metricFamily>\n");
        System.out.printf("    <name>%s</name>\n", family.getName());
        System.out.printf("    <type>%s</type>\n", family.getType());
        System.out.printf("    <help>%s</help>\n", family.getHelp());
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter metric, int index) {
        System.out.printf("    <metric>\n");
        System.out.printf("      <name>%s</name>\n", family.getName());
        System.out.printf("      <type>%s</type>\n", MetricType.COUNTER);
        System.out.printf("      <labels>%s</labels>\n", buildLabelListString(metric.getLabels(), null, null));
        System.out.printf("      <value>%s</value>\n", Util.convertDoubleToString(metric.getValue()));
        System.out.printf("    </metric>\n");
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge metric, int index) {
        System.out.printf("    <metric>\n");
        System.out.printf("      <name>%s</name>\n", family.getName());
        System.out.printf("      <type>%s</type>\n", MetricType.GAUGE);
        System.out.printf("      <labels>%s</labels>\n", buildLabelListString(metric.getLabels(), null, null));
        System.out.printf("      <value>%s</value>\n", Util.convertDoubleToString(metric.getValue()));
        System.out.printf("    </metric>\n");
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary metric, int index) {
        System.out.printf("    <metric>\n");
        System.out.printf("      <name>%s</name>\n", family.getName());
        System.out.printf("      <type>%s</type>\n", MetricType.SUMMARY);
        System.out.printf("      <labels>%s</labels>\n", buildLabelListString(metric.getLabels(), null, null));
        System.out.printf("      <count>%d</count>\n", metric.getSampleCount());
        System.out.printf("      <sum>%s</sum>\n", Util.convertDoubleToString(metric.getSampleSum()));
        if (!metric.getQuantiles().isEmpty()) {
            System.out.printf("      <quantiles>\n");
            for (Quantile quantile : metric.getQuantiles()) {
                System.out.printf("        <quantile>%s</quantile>\n", quantile);
            }
            System.out.printf("      </quantiles>\n");
        }
        System.out.printf("    </metric>\n");
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram metric, int index) {
        System.out.printf("    <metric>\n");
        System.out.printf("      <name>%s</name>\n", family.getName());
        System.out.printf("      <type>%s</type>\n", MetricType.HISTOGRAM);
        System.out.printf("      <labels>%s</labels>\n", buildLabelListString(metric.getLabels(), null, null));
        System.out.printf("      <count>%d</count>\n", metric.getSampleCount());
        System.out.printf("      <sum>%s</sum>\n", Util.convertDoubleToString(metric.getSampleSum()));
        if (!metric.getBuckets().isEmpty()) {
            System.out.printf("      <buckets>\n");
            for (Bucket bucket : metric.getBuckets()) {
                System.out.printf("        <bucket>%s</bucket>\n", bucket);
            }
            System.out.printf("      </bucket>\n");
        }
        System.out.printf("    </metric>\n");
    }
}
