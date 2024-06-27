package com.jingfang.autoconfig;


import com.jingfang.cloud.core.jackson.mapper.DefaultObjectMapper;
import com.jingfang.cloud.kafka.Factory;
import com.jingfang.cloud.kafka.Template;
import com.jingfang.cloud.kafka.delayqueue.DelayKafkaTemplate;
import com.jingfang.cloud.kafka.producer.internals.DefaultPartitioner;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static com.jingfang.cloud.kafka.Template.JSON_PRODUCER_FACTORY;
import static com.jingfang.cloud.kafka.Template.OBJECT_PRODUCER_FACTORY;

/**
 * @author jin
 */
@SuppressWarnings("squid:S1452")
@Slf4j
@EnableKafka
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
@Import(KafkaDelayQueueConfigurer.class)
public class KafkaAutoConfiguration {
    public KafkaAutoConfiguration() {
        log.trace("initializing...");
    }

    private KafkaProperties properties;

    public RecordFilterStrategy<Object, Object> recordFilterStrategy;

    @Autowired
    public void setProperties(KafkaProperties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    public void setRecordFilterStrategy(RecordFilterStrategy<Object, Object> recordFilterStrategy) {
        this.recordFilterStrategy = recordFilterStrategy;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultObjectMapper objectMapper() {
        return new DefaultObjectMapper();
    }

    @Bean
    @Primary
    public ProducerFactory<?, ?> kafkaProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DefaultPartitioner.class);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Primary
    @Bean(name = Template.STRING)
    public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory) {
        KafkaTemplate<Object, Object> template = new KafkaTemplate<>(kafkaProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        return template;
    }


    @Bean(name = JSON_PRODUCER_FACTORY)
    public ProducerFactory<String, Object> jsonProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DefaultPartitioner.class);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    @Bean(name = Template.JSON)
    public KafkaTemplate<String, ?> jsonKafkaTemplate(
            @Qualifier(JSON_PRODUCER_FACTORY) ProducerFactory<String, Object> jsonProducerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(jsonProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    @Bean(name = OBJECT_PRODUCER_FACTORY)
    public ProducerFactory<String, Object> objectProducerFactory(DefaultObjectMapper objectMapper) {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties();
        producerProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, DefaultPartitioner.class);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        StringSerializer keySerializer = new StringSerializer();
        JsonSerializer<Object> valueSerializer = new JsonSerializer<>(objectMapper);
        return new DefaultKafkaProducerFactory<>(producerProperties,
                keySerializer, valueSerializer);
    }

    @Bean(name = Template.OBJECT)
    public KafkaTemplate<?, ?> objectKafkaTemplate(DefaultObjectMapper objectMapper,
                                                   @Qualifier(OBJECT_PRODUCER_FACTORY) ProducerFactory<String,
                                                           Object> objectProducerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(objectProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        template.setMessageConverter(new StringJsonMessageConverter(objectMapper));
        return template;
    }


    @Bean
    @Primary
    public ConsumerFactory<?, ?> kafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.properties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    @Bean(name = Factory.STRING)
    public KafkaListenerContainerFactory<?> stringContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConsumerFactory(kafkaConsumerFactory);
        if (recordFilterStrategy != null) {
            factory.setRecordFilterStrategy(recordFilterStrategy);
        }
        return factory;
    }

    @Bean(name = Factory.JSON)
    public KafkaListenerContainerFactory<?> jsonContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setRecordMessageConverter(new StringJsonMessageConverter());
        if (recordFilterStrategy != null) {
            factory.setRecordFilterStrategy(recordFilterStrategy);
        }
        return factory;
    }

    @Bean(name = Factory.OBJECT)
    public KafkaListenerContainerFactory<?> objectContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory, DefaultObjectMapper objectMapper) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setRecordMessageConverter(new StringJsonMessageConverter(objectMapper));
        if (recordFilterStrategy != null) {
            factory.setRecordFilterStrategy(recordFilterStrategy);
        }
        return factory;
    }

    @Bean(name = Factory.BATCH)
    public KafkaListenerContainerFactory<?> batchObjectContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory, DefaultObjectMapper objectMapper) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setBatchListener(true);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(new StringJsonMessageConverter(objectMapper)));
        return factory;
    }



    @Bean(name = Template.DELAY)
    public DelayKafkaTemplate delaykafkaTemplate(@Qualifier(Template.STRING) KafkaTemplate<String, String> kafkaTemplate) {
        return new DelayKafkaTemplate(kafkaTemplate);
    }
}
