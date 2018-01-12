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
package org.hawkular.agent.prometheus.binary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hawkular.agent.prometheus.PrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker;

import io.prometheus.client.Metrics.LabelPair;
import io.prometheus.client.Metrics.Metric;
import io.prometheus.client.Metrics.MetricFamily;
import io.prometheus.client.Metrics.Quantile;
import io.prometheus.client.Metrics.Summary;

/**
 * This will iterate over a list of Prometheus metrics that are given as binary protocol buffer data.
 */
public class BinaryPrometheusMetricsProcessor extends PrometheusMetricsProcessor<MetricFamily> {
    public BinaryPrometheusMetricsProcessor(InputStream inputStream, PrometheusMetricsWalker theWalker) {
        super(inputStream, theWalker);
    }

    @Override
    public BinaryPrometheusMetricDataParser createPrometheusMetricDataParser() {
        return new BinaryPrometheusMetricDataParser(getInputStream());
    }

    @Override
    protected org.hawkular.agent.prometheus.types.MetricFamily convert(MetricFamily family) {
        org.hawkular.agent.prometheus.types.MetricFamily.Builder convertedFamilyBuilder;
        MetricType convertedFamilyType = MetricType.valueOf(family.getType().name());

        convertedFamilyBuilder = new org.hawkular.agent.prometheus.types.MetricFamily.Builder();
        convertedFamilyBuilder.setName(family.getName());
        convertedFamilyBuilder.setHelp(family.getHelp());
        convertedFamilyBuilder.setType(convertedFamilyType);

        for (Metric metric : family.getMetricList()) {
            org.hawkular.agent.prometheus.types.Metric.Builder<?> convertedMetricBuilder = null;
            switch (convertedFamilyType) {
                case COUNTER:
                    convertedMetricBuilder = new org.hawkular.agent.prometheus.types.Counter.Builder()
                            .setValue(metric.getCounter().getValue());
                    break;
                case GAUGE:
                    convertedMetricBuilder = new org.hawkular.agent.prometheus.types.Gauge.Builder()
                            .setValue(metric.getGauge().getValue());
                    break;
                case SUMMARY:
                    Summary summary = metric.getSummary();
                    List<Quantile> pqList = summary.getQuantileList();
                    List<org.hawkular.agent.prometheus.types.Summary.Quantile> hqList;
                    hqList = new ArrayList<>(pqList.size());
                    for (Quantile pq : pqList) {
                        org.hawkular.agent.prometheus.types.Summary.Quantile hq;
                        hq = new org.hawkular.agent.prometheus.types.Summary.Quantile(
                                pq.getQuantile(), pq.getValue());
                        hqList.add(hq);
                    }
                    convertedMetricBuilder = new org.hawkular.agent.prometheus.types.Summary.Builder()
                            .setSampleCount(metric.getSummary().getSampleCount())
                            .setSampleSum(metric.getSummary().getSampleSum())
                            .addQuantiles(hqList);
                    break;
                case HISTOGRAM:
                    /* NO HISTOGRAM SUPPORT IN PROMETHEUS JAVA MODEL API 0.0.2. Uncomment when 0.0.3 is released
                    Histogram histo = metric.getHistogram();
                    List<Bucket> pbList = histo.getBucketList();
                    List<org.hawkular.agent.prometheus.types.Histogram.Bucket> hbList;
                    hbList = new ArrayList<>(pbList.size());
                    for (Bucket pb : pbList) {
                        org.hawkular.agent.prometheus.types.Histogram.Bucket hb;
                        hb = new org.hawkular.agent.prometheus.types.Histogram.Bucket(pb.getUpperBound(),
                                pb.getCumulativeCount());
                        hbList.add(hb);
                    }
                    convertedMetricBuilder = new org.hawkular.agent.prometheus.types.Histogram.Builder()
                            .setSampleCount(metric.getHistogram().getSampleCount())
                            .setSampleSum(metric.getHistogram().getSampleSum())
                            .addBuckets(hbList);
                    */
                    break;
            }
            convertedMetricBuilder.setName(family.getName());
            for (LabelPair labelPair : metric.getLabelList()) {
                convertedMetricBuilder.addLabel(labelPair.getName(), labelPair.getValue());
            }
            convertedFamilyBuilder.addMetric(convertedMetricBuilder.build());
        }

        return convertedFamilyBuilder.build();
    }
}
