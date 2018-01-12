# Prometheus Metrics Scraper

This provides an API and a command line utility to scrape metrics from a Prometheus protocol endpoint.
It supports both binary and text Prometheus data formats.

## Command Line Utility

The command line utility is run via:

````
java -jar prometheus-scraper*-cli.jar [--simple | --xml | --json] <url>
````

where `<url>` is the Prometheus protocol endpoint (typically something like `http://localhost:9090/metrics`).
Content negotiation will be used to establish the actual format to use.

## Java Scraper API

You can programmatically scrape a URL via the Java class `prometheus.PrometheusScraper`.
The `scrape()` method is usually what you want to use.
If you want to process a stream of data from the URL endpoint, you can write your own `prometheus.walkers.PrometheusMetricsWalker` implementation and use the `scrape(walker)` method.

### Maven Dependency

To obtain this Prometheus scraper, use the following Maven dependency:

````xml
<dependency>
  <groupId>org.github.jmazzitelli</groupId>
  <artifactId>prometheus-scraper</artifactId>
  <version>#.#.#</version>
</dependency>
````
## Extending

The current Prometheus Metrics Scraper supports the two main data formats - protocol buffer binary data and text data. Endpoints are allowed to support additional data formats (typically human-readable formats for debugging). You can extend this Prometheus Metrics Scraper to support those additional data formats.

First, subclass `prometheus.PrometheusMetricDataParser<T>` where T is the custom data representation of the metric family data with its metrics. You can optionally choose to use the common MetricFamily API (`prometheus.types.MetricFamily`) for this type. You must implement the `parse()` method which must read one metric family from the input stream per invocation.

Second, subclass `prometheus.PrometheusMetricsProcessor<T>` (where T is again the custom data representation of the metric family data or use the common MetricFamily class itself). Implement the `convert(T)` method to convert between the custom data representation of the metric family to the common API (or just return the object if you opted to use the common MetricFamily API). You must also implement `createPrometheusMetricDataParser()` to return an instance of the custom `PrometheusMetricDataParser<T>` class (see above).

To use your extension, create an input stream to your endpoint that contains the custom-formatted metric data, create a walker instance to walk your data (say, use the `prometheus.walkers.JSONPrometheusMetricsWalker` to generate a JSON document of your metric data or `prometheus.walkers.CollectorPrometheusMetricsWalker` to simply obtain a list of all metric families) and pass the stream and walker to your extension processor's constructor then call the `walk()` method.
