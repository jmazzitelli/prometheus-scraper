package prometheus.walkers;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import prometheus.Util;
import prometheus.types.Counter;
import prometheus.types.Gauge;
import prometheus.types.Histogram;
import prometheus.types.Histogram.Bucket;
import prometheus.types.MetricFamily;
import prometheus.types.Summary;
import prometheus.types.Summary.Quantile;

public class JSONPrometheusMetricsWalker implements PrometheusMetricsWalker {

    @Override
    public void walkStart() {
        System.out.println("[");
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
        if (familiesProcessed > 0) {
            System.out.println("    ]");
            System.out.println("  }");
        }
        System.out.println("]");
    }

    @Override
    public void walkMetricFamily(MetricFamily familyInfo, int index) {
        if (index > 0) {
            System.out.printf("    ]\n");
            System.out.printf("  },\n");
        }

        System.out.printf("  {\n");
        System.out.printf("    \"name\":\"%s\",\n", familyInfo.getName());
        System.out.printf("    \"help\":\"%s\",\n", familyInfo.getHelp());
        System.out.printf("    \"type\":\"%s\",\n", familyInfo.getType());
        System.out.printf("    \"metrics\":[\n");
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter metric, int index) {
        System.out.printf("      {\n");
        outputLabels(metric.getLabels());
        System.out.printf("        \"value\":\"%s\"\n", Util.convertDoubleToString(metric.getValue()));
        if ((index + 1) == family.getMetrics().size()) {
            System.out.printf("      }\n");
        } else {
            System.out.printf("      },\n"); // there are more coming
        }
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge metric, int index) {
        System.out.printf("      {\n");
        outputLabels(metric.getLabels());
        System.out.printf("        \"value\":\"%s\"\n", Util.convertDoubleToString(metric.getValue()));
        if ((index + 1) == family.getMetrics().size()) {
            System.out.printf("      }\n");
        } else {
            System.out.printf("      },\n"); // there are more coming
        }
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary metric, int index) {
        System.out.printf("      {\n");
        outputLabels(metric.getLabels());
        if (!metric.getQuantiles().isEmpty()) {
            System.out.printf("        \"quantiles\":{\n");
            Iterator<Quantile> iter = metric.getQuantiles().iterator();
            while (iter.hasNext()) {
                Quantile quantile = iter.next();
                System.out.printf("          \"%f\":\"%f\"%s\n",
                        quantile.getQuantile(), quantile.getValue(), (iter.hasNext()) ? "," : "");
            }
            System.out.printf("        },\n");
        }
        System.out.printf("        \"count\":\"%d\",\n", metric.getSampleCount());
        System.out.printf("        \"sum\":\"%s\"\n", Util.convertDoubleToString(metric.getSampleSum()));
        if ((index + 1) == family.getMetrics().size()) {
            System.out.printf("      }\n");
        } else {
            System.out.printf("      },\n"); // there are more coming
        }
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram metric, int index) {
        System.out.printf("      {\n");
        outputLabels(metric.getLabels());
        if (!metric.getBuckets().isEmpty()) {
            System.out.printf("        \"buckets\":{\n");
            Iterator<Bucket> iter = metric.getBuckets().iterator();
            while (iter.hasNext()) {
                Bucket bucket = iter.next();
                System.out.printf("          \"%f\":\"%d\"%s\n",
                        bucket.getUpperBound(), bucket.getCumulativeCount(), (iter.hasNext()) ? "," : "");
            }
            System.out.printf("        },\n");
        }
        System.out.printf("        \"count\":\"%d\",\n", metric.getSampleCount());
        System.out.printf("        \"sum\":\"%s\"\n", Util.convertDoubleToString(metric.getSampleSum()));
        if ((index + 1) == family.getMetrics().size()) {
            System.out.printf("      }\n");
        } else {
            System.out.printf("      },\n"); // there are more coming
        }
    }

    private void outputLabels(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return;
        }
        System.out.printf("        \"labels\":{\n");
        Iterator<Entry<String, String>> iter = labels.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> labelPair = iter.next();
            String comma = (iter.hasNext()) ? "," : "";
            System.out.printf("          \"%s\":\"%s\"%s\n", labelPair.getKey(), labelPair.getValue(), comma);
        }
        System.out.printf("        },\n");
    }
}
