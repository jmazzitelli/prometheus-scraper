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

package prometheus;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger.Level;
import org.junit.Assert;
import org.junit.Test;
import prometheus.binary.BinaryPrometheusMetricDataParser;
import prometheus.binary.BinaryPrometheusMetricsProcessor;
import prometheus.walkers.LoggingPrometheusMetricsWalker;
import prometheus.walkers.PrometheusMetricsWalker;

import io.prometheus.client.Metrics.MetricFamily;

public class BinaryPrometheusParserTest {
    @Test
    public void testGetMetricsFromStream() throws Exception {

        List<MetricFamily> metricFamilies = new ArrayList<>();
        try (InputStream testData = this.getClass().getClassLoader().getResourceAsStream("prometheus.data")) {
            BinaryPrometheusMetricDataParser parser = new BinaryPrometheusMetricDataParser(testData);
            while (true) {
                MetricFamily family = parser.parse();
                if (family == null) {
                    break;
                }
                metricFamilies.add(family);
            }
        }
        Assert.assertNotNull(metricFamilies);
        Assert.assertEquals(71, metricFamilies.size());

        // walk the data and make sure it has what we expect
        final AtomicInteger familyCount = new AtomicInteger(0);
        final AtomicInteger fullCount = new AtomicInteger(0);
        PrometheusMetricsWalker walker = new LoggingPrometheusMetricsWalker(Level.INFO) {
            public void walkMetricFamily(prometheus.types.MetricFamily family, int index) {
                super.walkMetricFamily(family, index);
                familyCount.incrementAndGet();
                fullCount.addAndGet(family.getMetrics().size());
            }
        };

        try (InputStream testData = this.getClass().getClassLoader().getResourceAsStream("prometheus.data")) {
            new BinaryPrometheusMetricsProcessor(testData, walker).walk();
        }
        Assert.assertEquals(metricFamilies.size(), familyCount.get());
        Assert.assertEquals(126, fullCount.get());
    }

    @Test
    public void testGetMetricsFromUrlAndFile() throws Exception {
        URL testDataUrl = this.getClass().getClassLoader().getResource("prometheus.data");
        File testDataFile;
        try {
            testDataFile = new File(testDataUrl.toURI());
        } catch (URISyntaxException e) {
            testDataFile = new File(testDataUrl.getPath());
        }

        // walks the data and counts some things that we'll look at later to make sure we get what we expect
        final AtomicInteger familyCount = new AtomicInteger(0);
        final AtomicInteger fullCount = new AtomicInteger(0);
        PrometheusMetricsWalker walker = new LoggingPrometheusMetricsWalker(Level.INFO) {
            public void walkMetricFamily(prometheus.types.MetricFamily family, int index) {
                super.walkMetricFamily(family, index);
                familyCount.incrementAndGet();
                fullCount.addAndGet(family.getMetrics().size());
            }
        };

        // test the URL constructor
        PrometheusScraper scraper = new PrometheusScraper(testDataUrl, PrometheusDataFormat.BINARY);
        scraper.scrape(walker);
        Assert.assertEquals(71, familyCount.get());
        Assert.assertEquals(126, fullCount.get());

        // test the File constructor
        familyCount.set(0);
        fullCount.set(0);
        scraper = new PrometheusScraper(testDataFile, PrometheusDataFormat.BINARY);
        scraper.scrape(walker);
        Assert.assertEquals(71, familyCount.get());
        Assert.assertEquals(126, fullCount.get());

        // test the scrape() method
        scraper = new PrometheusScraper(testDataUrl, PrometheusDataFormat.BINARY);
        List<prometheus.types.MetricFamily> allFamilies = scraper.scrape();
        Assert.assertEquals(71, allFamilies.size());
    }
}
