package camel.contribution.components.natsjetstream;

import org.apache.camel.spi.Metadata;

public interface NatsJetStreamConstants {
    @Metadata(description = "The timestamp of a consumed message.", javaType = "long")
    String NATS_MESSAGE_TIMESTAMP = "CamelNatsMessageTimestamp";
    @Metadata(description = "The SID of a consumed message.", javaType = "String")
    String NATS_SID = "CamelNatsSID";
    @Metadata(description = "The ReplyTo of a consumed message (may be null).", javaType = "String")
    String NATS_REPLY_TO = "CamelNatsReplyTo";
    @Metadata(description = "The Subject of a consumed message.", javaType = "String")
    String NATS_SUBJECT = "CamelNatsSubject";
    @Metadata(description = "The Queue name of a consumed message (may be null).", javaType = "String")
    String NATS_QUEUE_NAME = "CamelNatsQueueName";
}
