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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.hawkular.agent.prometheus.Util;
import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.Histogram.Bucket;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;
import org.hawkular.agent.prometheus.types.Summary.Quantile;

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