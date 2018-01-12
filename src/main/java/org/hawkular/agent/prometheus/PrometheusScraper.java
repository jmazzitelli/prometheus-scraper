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
package org.hawkular.agent.prometheus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.hawkular.agent.prometheus.binary.BinaryPrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.text.TextPrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.walkers.CollectorPrometheusMetricsWalker;
import org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker;
import org.jboss.logging.Logger;

/**
 * Given a Prometheus protocol endpoint, this will scrape the Prometheus data it finds there.
 * {@link #scrape()} is typically the API most consumers will want to use. Given a URL or file, this is
 * able to give you all the metric data found there, regardless of the format of the data.
 */
public class PrometheusScraper {
    private static final Logger log = Logger.getLogger(PrometheusScraper.class);

    private final URL url;
    private final PrometheusDataFormat knownDataFormat;

    // see openConnection() for where this is used
    protected class OpenConnectionDetails {
        public final InputStream inputStream;
        public final String contentType;

        public OpenConnectionDetails(InputStream is, String contentType) {
            this.inputStream = is;
            this.contentType = contentType;
        }
    }

    public PrometheusScraper(String host, int port, String context) throws MalformedURLException {
        if (host == null) {
            host = "127.0.0.1";
        }
        if (port == 0) {
            port = 9090;
        }
        if (context == null || context.isEmpty()) {
            context = "/metrics";
        }
        this.url = new URL("http", host, port, context);
        this.knownDataFormat = null;
        log.debugf("Will scrape Permetheus data from URL [%s]", this.url);
    }

    public PrometheusScraper(URL url) {
        this(url, null);
    }

    /**
     * This constructor allows you to explicitly indicate what data format is expected.
     * If the URL cannot provide a content type, this data format will determine what data format
     * will be assumed. If the URL does provide a content type, the given data format is ignored.
     * This is useful if you are providing a URL that actually refers to a file in which case
     * the URL connection will not be able to provide a content type.
     *
     * @see #PrometheusScraperUrl(File, PrometheusDataFormat)
     *
     * @param url the URL where the Prometheus metric data is found
     * @param dataFormat the data format of the metric data found at the URL, or null if
     *                   the URL endpoint can provide it for us via content negotiation.
     */
    public PrometheusScraper(URL url, PrometheusDataFormat dataFormat) {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null");
        }
        this.url = url;
        this.knownDataFormat = dataFormat;
        log.debugf("Will scrape Permetheus data from URL [%s] with data format [%s]",
                this.url, (this.knownDataFormat == null) ? "<TBD>" : this.knownDataFormat);
    }

    /**
     * Scrape data from the given file. The data format will indicate if it
     * is binary protocol buffer data or text data ("text/plain").
     *
     * @param file the file to scrape
     * @param dataFormat the format of the metric data in the file.
     */
    public PrometheusScraper(File file, PrometheusDataFormat dataFormat) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (dataFormat == null) {
            throw new IllegalArgumentException("Must provide the content type for the file");
        }

        try {
            this.url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("File does not have valid URL: " + file);
        }

        this.knownDataFormat = dataFormat;

        log.debugf("Will scrape Permetheus data from file [%s] with data format [%s]", this.url,
                this.knownDataFormat);
    }

    /**
     * This will collect all metric data from the endpoint and
     * return the entire list of all metric families found there.
     * @return all metric data found at the endpoint
     * @throws IOException if failed to scrape data
     */
    public List<MetricFamily> scrape() throws IOException {
        CollectorPrometheusMetricsWalker collector = new CollectorPrometheusMetricsWalker();
        scrape(collector);
        return collector.getAllMetricFamilies();
    }

    public void scrape(PrometheusMetricsWalker walker) throws IOException {
        OpenConnectionDetails connectionDetails = openConnection(this.url);
        if (connectionDetails == null || connectionDetails.inputStream == null) {
            throw new IOException("Failed to open the connection to the Prometheus endpoint");
        }

        try (InputStream inputStream = connectionDetails.inputStream) {
            String contentType = connectionDetails.contentType;

            // if we were given a content type - we use it always. If we were not given a content type,
            // then use the one given to the constructor (if one was given).
            if (contentType == null || contentType.contains("unknown")) {
                contentType = this.knownDataFormat.getContentType();
            }

            PrometheusMetricsProcessor<?> processor;

            if (contentType.contains("application/vnd.google.protobuf")) {
                processor = new BinaryPrometheusMetricsProcessor(inputStream, walker);
            } else if (contentType.contains("text/plain")) {
                processor = new TextPrometheusMetricsProcessor(inputStream, walker);
            } else {
                // unknown - since all Prometheus endpoints are required to support text, try it
                log.debugf("Unknown content type for URL [%s]. Trying text format.", url);
                processor = new TextPrometheusMetricsProcessor(inputStream, walker);
            }

            processor.walk();
        }
    }

    /**
     * This is the content type of the supported Prometheus binary format.
     * This can be used in the Accept header when making the HTTP request to the Prometheus endpoint.
     *
     * @return binary format content type
     */
    protected String getBinaryFormatContentType() {
        return "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";
    }

    /**
     * This is the content type of the supported Prometheus text format.
     * This can be used in the Accept header when making the HTTP request to the Prometheus endpoint.
     *
     * @return text format content type
     */
    protected String getTextFormatContentType() {
        return "text/plain; version 0.0.4";
    }

    /**
     * This provides a hook for subclasses to be able to connect to the Prometheus endpoint
     * and tell us what the content type is and to give us the actual stream to the data.
     *
     * This is useful in case callers need to connect securely to the Prometheus endpoint.
     * Subclasses need to open the secure connection with their specific secure credentials
     * and other security details and return the input stream to the data (as well as its content type).
     *
     * If subclasses return a null content type in the returned object the data format passed to this
     * object's constructor will be assumed as the data format in the input stream.
     *
     * The default implementation is to simply open an unsecured connection to the URL.
     *
     * @param url the Prometheus endpoint
     * @return connection details for the Prometheus endpoint
     *
     * @throws IOException if the connection could not be opened
     */
    protected OpenConnectionDetails openConnection(URL endpointUrl) throws IOException {
        URLConnection conn = endpointUrl.openConnection();
        conn.setRequestProperty("Accept", getBinaryFormatContentType());
        InputStream stream = conn.getInputStream();
        String contentType = conn.getContentType();
        return new OpenConnectionDetails(stream, contentType);
    }
}
