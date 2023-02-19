package camel.contribution.components.natsjetstream;

import io.nats.client.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class NatsJetStreamConsumer extends DefaultConsumer {
    private final NatsJetStreamEndpoint endpoint;
    private final Processor processor;
    private ExecutorService executor;
    private Connection connection;

    public NatsJetStreamConsumer(NatsJetStreamEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.processor = processor;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        this.executor = endpoint.createExecutor();
        this.connection = endpoint.getConnection();
        this.executor.submit(new NatsJetStreamConsumingTask(this.connection));
    }

    @Override
    protected void doStop() throws Exception {
        if (this.executor != null) {
            this.executor.shutdown();
        }
        this.executor = null;

        if (ObjectHelper.isNotEmpty(this.connection)) {
            if (!this.connection.getStatus().equals(Connection.Status.CLOSED)) {
                this.connection.close();
            }
        }
        super.doStop();
    }

    private class NatsJetStreamConsumingTask implements Runnable {
        private final Connection connection;

        public NatsJetStreamConsumingTask(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                JetStream jetStream = connection.jetStream();

                PullSubscribeOptions options = PullSubscribeOptions
                        .builder()
                        .durable(endpoint.getConfiguration().getDurable())
                        .build();

                JetStreamSubscription subscription = jetStream.subscribe(endpoint.getConfiguration().getSubject(), options);
                Consumer<Message> messageProcessor = new MessageProcessor();

                while (!Thread.currentThread().isInterrupted()) {
                    List<Message> batch = subscription.fetch(endpoint.getConfiguration().getBatchSize(), Duration.ofSeconds(1));
                    batch.forEach(messageProcessor);
                }
            } catch (Exception e) {
                NatsJetStreamConsumer.this.getExceptionHandler().handleException("Error during processing", e);
            }
        }

        class MessageProcessor implements java.util.function.Consumer<Message> {
            @Override
            public void accept(Message message) {
                Exchange exchange = NatsJetStreamConsumer.this.createExchange(false);
                try {
                    handleMessage(exchange, message);
                } catch (Exception e) {
                    NatsJetStreamConsumer.this.getExceptionHandler().handleException("Error during processing", exchange, e);
                } finally {
                    NatsJetStreamConsumer.this.releaseExchange(exchange, false);
                }
            }

            private void handleMessage(Exchange exchange, Message message) throws Exception {
                exchange.getIn().setBody(message.getData());
                exchange.getIn().setHeader(NatsJetStreamConstants.NATS_REPLY_TO, message.getReplyTo());
                exchange.getIn().setHeader(NatsJetStreamConstants.NATS_SID, message.getSID());
                exchange.getIn().setHeader(NatsJetStreamConstants.NATS_SUBJECT, message.getSubject());
                exchange.getIn().setHeader(NatsJetStreamConstants.NATS_QUEUE_NAME, message.getSubscription().getQueueName());
                exchange.getIn().setHeader(NatsJetStreamConstants.NATS_MESSAGE_TIMESTAMP, System.currentTimeMillis());

                if (message.hasHeaders()) {
                    // TODO: Filter headers with HeaderFilterStrategy
                    message.getHeaders().forEach((key, values) -> {
                        if (values.size() == 1) {
                            exchange.getIn().setHeader(key, values.get(0));
                        } else {
                            exchange.getIn().setHeader(key, values);
                        }
                    });
                }

                NatsJetStreamConsumer.this.processor.process(exchange);

                // TODO: Handle replies.

                message.ack();
            }
        }
    }
}
