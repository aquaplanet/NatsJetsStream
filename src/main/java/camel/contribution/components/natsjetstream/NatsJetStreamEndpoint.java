package camel.contribution.components.natsjetstream;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * true component which does bla bla.
 * <p>
 * TODO: Update one line description above what the component does.
 */
@UriEndpoint(firstVersion = "1.0-SNAPSHOT", scheme = "natsjetstream", title = "NatsJetStream", syntax = "natsjetstream:server", category = {Category.MESSAGING})
public class NatsJetStreamEndpoint extends DefaultEndpoint {

    @UriParam
    private final NatsJetstreamConfiguration configuration;

    public NatsJetStreamEndpoint(String uri, NatsJetStreamComponent component, NatsJetstreamConfiguration configuration) {
        super(uri, component);
        this.configuration = configuration;
    }

    @Override
    public Producer createProducer() {
        throw new UnsupportedOperationException("Create producer is not implemented");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        Consumer consumer = new NatsJetStreamConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    public Connection getConnection() throws IOException, InterruptedException {
        Options.Builder builder = getConfiguration().createOptions();
        Options options = builder.build();
        return Nats.connect(options);
    }

    public ExecutorService createExecutor() {
        return getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, "NatsTopic[" + getConfiguration().getSubject() + "]", getConfiguration().getBatchSize());
    }

    public NatsJetstreamConfiguration getConfiguration() {
        return configuration;
    }
}
