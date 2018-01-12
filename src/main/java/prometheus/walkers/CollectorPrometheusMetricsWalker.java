package prometheus.walkers;

import java.util.ArrayList;
import java.util.List;

import prometheus.types.Counter;
import prometheus.types.Gauge;
import prometheus.types.Histogram;
import prometheus.types.MetricFamily;
import prometheus.types.Summary;

/**
 * This simply collects all metrics in all families and provides a list to the families.
 */
public class CollectorPrometheusMetricsWalker implements PrometheusMetricsWalker {

    private List<MetricFamily> finishedList;
    private boolean finished;

    /**
     * @return indicates if this walker has finished processing all metric families.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @return if this walker has finished processing all metric families, this will return the list of the
     *         metric families processed. If the walker hasn't finished yet, null is returned.
     */
    public List<MetricFamily> getAllMetricFamilies() {
        return (finished) ? finishedList : null;
    }

    @Override
    public void walkStart() {
        finished = false;
        finishedList = new ArrayList<>();
    }

    @Override
    public void walkFinish(int familiesProcessed, int metricsProcessed) {
        finished = true;
    }

    @Override
    public void walkMetricFamily(MetricFamily family, int index) {
        finishedList.add(family);
    }

    @Override
    public void walkCounterMetric(MetricFamily family, Counter metric, int index) {
    }

    @Override
    public void walkGaugeMetric(MetricFamily family, Gauge metric, int index) {
    }

    @Override
    public void walkSummaryMetric(MetricFamily family, Summary metric, int index) {
    }

    @Override
    public void walkHistogramMetric(MetricFamily family, Histogram metric, int index) {
    }
}
