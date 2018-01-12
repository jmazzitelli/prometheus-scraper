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

import java.net.URL;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;

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