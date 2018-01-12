/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus.walkers;

import java.util.Map;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

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
