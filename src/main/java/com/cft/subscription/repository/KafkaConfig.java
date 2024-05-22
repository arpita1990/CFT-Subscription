package com.cft.subscription.repository;

import com.cft.appservice.core.ActionLog;
import com.cft.appservice.core.LoggableAction;
import com.cft.appservice.core.NotificationPackage;
import com.cft.appservice.model.FilePermissionWrapper;
import com.cft.appservice.model.RequestAccessFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${kafka.conf.bootstrapaddress}")
    private String bootstrapAddress;

    @Value("${kafka.conf.acks}")
    private String aksConf;

    @Value("${kafka.conf.retry}")
    private String retryConfig;

    @Value("${kafka.conf.defaultgroup}")
    private String defaultGroup;

    @Value("${kafka.conf.auto-offset-reset}")
    private String autoOffsetReset;

    @Bean
    public ProducerFactory<String, NotificationPackage> setProducer() {
        return new DefaultKafkaProducerFactory<String, NotificationPackage>(getNewConfMap());
    }

    @Bean
    public KafkaTemplate<String, NotificationPackage> kafkaTemplate()
    {
        return new KafkaTemplate<String, NotificationPackage>(setProducer());
    }

    private Map<String, Object> getNewConfMap() {
        Map<String, Object> confMap = new HashMap<>();
        confMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        confMap.put(ProducerConfig.ACKS_CONFIG, aksConf);
        confMap.put(ProducerConfig.RETRIES_CONFIG, retryConfig);
        confMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        confMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        confMap.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return confMap;
    }

    @Bean
    public KafkaTemplate<String, RequestAccessFeedback> kafkaTemplateRequestAccessFeedback()
    {
        DefaultKafkaProducerFactory producer = new DefaultKafkaProducerFactory<String, NotificationPackage>(getNewConfMap());
        return new KafkaTemplate<String, RequestAccessFeedback>(producer);
    }



    @Bean
    public KafkaTemplate<String, ActionLog<? extends LoggableAction>> kafkaTemplateActionLog(ObjectMapper objectMapper)
    {
        DefaultKafkaProducerFactory producer = new DefaultKafkaProducerFactory<String, ActionLog<? extends LoggableAction>>(getNewConfMap());
        producer.setValueSerializer(new JsonSerializer<ActionLog<? extends LoggableAction>>(objectMapper));
        return new KafkaTemplate<String, ActionLog<? extends LoggableAction>>(producer);
    }

    @Bean
    public ConsumerFactory<Object, Object> consumerFactory() {
        Map<String, Object> conf = new HashMap();
        conf.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        conf.put(ConsumerConfig.GROUP_ID_CONFIG, defaultGroup);
        conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        conf.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        conf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        conf.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory(conf, new StringDeserializer(),
                new JsonDeserializer<Object>(Object.class));
    }

    @Bean
    public KafkaListenerContainerFactory concurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

