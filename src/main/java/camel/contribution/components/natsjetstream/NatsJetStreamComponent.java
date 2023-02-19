package camel.contribution.components.natsjetstream;

import org.apache.camel.Endpoint;
import org.apache.camel.support.HeaderFilterStrategyComponent;

import java.util.Map;

@org.apache.camel.spi.annotations.Component("natsjetstream")
public class NatsJetStreamComponent extends HeaderFilterStrategyComponent {
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        NatsJetstreamConfiguration configuration = new NatsJetstreamConfiguration();
        configuration.setServers(remaining);

        Endpoint endpoint = new NatsJetStreamEndpoint(uri, this, configuration);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
