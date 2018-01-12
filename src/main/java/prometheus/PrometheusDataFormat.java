package prometheus;

/**
 * The supported Prometheus data formats.
 */
public enum PrometheusDataFormat {
    TEXT("plain/text"), //
    BINARY("application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");

    private final String contentType;

    private PrometheusDataFormat(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
