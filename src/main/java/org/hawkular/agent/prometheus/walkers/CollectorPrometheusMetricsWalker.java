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

import java.util.ArrayList;
import java.util.List;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;

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