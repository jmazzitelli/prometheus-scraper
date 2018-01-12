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

import org.hawkular.agent.prometheus.Util;
import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.Histogram.Bucket;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.types.Summary;
import org.hawkular.agent.prometheus.types.Summary.Quantile;

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