package prometheus;

import java.io.IOException;
import java.io.InputStream;

/**
 * An object that can parse Prometheus found in a specific data format in an input stream.
 * The type <T> is the metric family object for the specific data format.
 * It is the job of the associated {@link PrometheusMetricsProcessor} to process
 * the parsed data.
 *
 * @param <T> the metric family object type that the parser produces
 */
public abstract class PrometheusMetricDataParser<T> {
    private InputStream inputStream;

    /**
     * Provides the input stream where the parser will look for metric data.
     * NOTE: this object will not own this stream - it should never attempt to close it.
     *
     * @param inputStream the stream where the metric data can be found
     */
    public PrometheusMetricDataParser(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Stream must not be null");
        }
        this.inputStream = inputStream;
    }

    protected InputStream getInputStream() {
        return this.inputStream;
    }

    /**
     * Reads a single metric family from the Prometheus metric data stream and returns it.
     * Returns null when no more data is in the stream.
     *
     * This method is designed to be called several times, each time it returns the next metric family
     * found in the input stream.
     *
     * @return the metric family data found in the stream, or null
     * @throws IOException if failed to read the data from the stream
     */
    public abstract T parse() throws IOException;
}
