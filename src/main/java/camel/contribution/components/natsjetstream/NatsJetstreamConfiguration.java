package camel.contribution.components.natsjetstream;

import io.nats.client.Options;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.ObjectHelper;

@UriParams
public class NatsJetstreamConfiguration {

    @UriPath(label = "common")
    @Metadata(required = true, description = "Comma separated list of Nats servers to connect to.")
    private String servers;
    @UriParam(label = "common", description = "Subject filter to use when receiving messages.")
    @Metadata(required = true)
    private String subject;
    @UriParam(label = "consumer", defaultValue = "10")
    @Metadata(description = "Number of messages to fetch at a time.")
    private int batchSize = 10;

    @UriParam(label = "common")
    @Metadata(required = true, description = "Durable name for the consumer")
    private String durable;

    public Options.Builder createOptions() {
        Options.Builder builder = new Options.Builder();
        String splitServers = splitServers();
        builder.server(splitServers);
        return builder;
    }

    private String splitServers() {
        StringBuilder servers = new StringBuilder();
        String prefix = "nats://";

        ObjectHelper.notNull(getServers(), "servers");

        String[] pieces = getServers().split(",");
        for (int i = 0; i < pieces.length; i++) {
            if (i < pieces.length - 1) {
                servers.append(prefix + pieces[i] + ",");
            } else {
                servers.append(prefix + pieces[i]);
            }
        }
        return servers.toString();
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getDurable() {
        return durable;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }
}
