package com.lambda.autoconfig;

import com.lambda.cloud.core.jackson.LambdaObjectMapper;
import com.lambda.cloud.kafka.KafkaDelayTemplate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
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

/**
 * Kafka延时队列自动配置类
 * <p>
 * 提供Kafka延时消息功能的自动配置，包括：
 * <ul>
 *   <li>多种序列化方式的KafkaTemplate配置</li>
 *   <li>消费者工厂和监听器容器工厂配置</li>
 *   <li>延时消息模板配置</li>
 * </ul>
 *
 * @author jin
 * @since 1.0.0
 */
@Slf4j
@EnableKafka
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
@AutoConfigureAfter({JacksonAutoConfiguration.class})
@Import(KafkaDelayQueueConfigurer.class)
public class KafkaDelayAutoConfiguration {

    /**
     * Bean名称常量定义
     */
    public static final String STRING_TEMPLATE = "kafkaTemplate";

    public static final String JSON_TEMPLATE = "jsonKafkaTemplate";
    public static final String OBJECT_TEMPLATE = "objectKafkaTemplate";
    public static final String DELAY_TEMPLATE = "delayKafkaTemplate";
    public static final String JSON_PRODUCER_FACTORY = "jsonProducerFactory";
    public static final String OBJECT_PRODUCER_FACTORY = "objectProducerFactory";

    /**
     * Kafka配置属性
     */
    private KafkaProperties properties;

    /**
     * 消息过滤策略（可选）
     */
    private RecordFilterStrategy<Object, Object> recordFilterStrategy;

    /**
     * 构造函数
     */
    public KafkaDelayAutoConfiguration() {
        log.trace("Initializing KafkaDelayAutoConfiguration...");
    }

    /**
     * 设置Kafka配置属性
     *
     * @param properties Kafka配置属性
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    @Autowired
    public void setProperties(KafkaProperties properties) {
        this.properties = properties;
    }

    /**
     * 设置消息过滤策略（可选）
     *
     * @param recordFilterStrategy 消息过滤策略
     */
    @SuppressFBWarnings(value = "PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    @Autowired(required = false)
    public void setRecordFilterStrategy(RecordFilterStrategy<Object, Object> recordFilterStrategy) {
        this.recordFilterStrategy = recordFilterStrategy;
    }

    /**
     * 配置Lambda对象映射器
     *
     * @return LambdaObjectMapper实例
     */
    @Bean
    @ConditionalOnMissingBean
    public LambdaObjectMapper objectMapper() {
        return new LambdaObjectMapper();
    }

    /**
     * 配置字符串类型的Kafka生产者工厂
     *
     * @return 字符串类型的生产者工厂
     */
    @Bean
    @Primary
    public ProducerFactory<?, ?> kafkaProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties(null);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    /**
     * 配置字符串类型的Kafka模板
     *
     * @param kafkaProducerFactory 生产者工厂
     * @return 字符串类型的Kafka模板
     */
    @Primary
    @Bean(name = STRING_TEMPLATE)
    public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory) {
        KafkaTemplate<Object, Object> template = new KafkaTemplate<>(kafkaProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        return template;
    }

    /**
     * 配置JSON类型的Kafka生产者工厂
     *
     * @return JSON类型的生产者工厂
     */
    @Bean(name = JSON_PRODUCER_FACTORY)
    public ProducerFactory<String, Object> jsonProducerFactory() {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties(null);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProperties);
    }

    /**
     * 配置JSON类型的Kafka模板
     *
     * @param jsonProducerFactory JSON生产者工厂
     * @return JSON类型的Kafka模板
     */
    @Bean(name = JSON_TEMPLATE)
    public KafkaTemplate<String, ?> jsonKafkaTemplate(
            @Qualifier(JSON_PRODUCER_FACTORY) ProducerFactory<String, Object> jsonProducerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(jsonProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    /**
     * 配置对象类型的Kafka生产者工厂
     *
     * @param objectMapper 对象映射器
     * @return 对象类型的生产者工厂
     */
    @Bean(name = OBJECT_PRODUCER_FACTORY)
    public ProducerFactory<String, Object> objectProducerFactory(LambdaObjectMapper objectMapper) {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties(null);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        StringSerializer keySerializer = new StringSerializer();
        JsonSerializer<Object> valueSerializer = new JsonSerializer<>(objectMapper);
        return new DefaultKafkaProducerFactory<>(producerProperties, keySerializer, valueSerializer);
    }

    /**
     * 配置对象类型的Kafka模板
     *
     * @param objectMapper 对象映射器
     * @param objectProducerFactory 对象生产者工厂
     * @return 对象类型的Kafka模板
     */
    @Bean(name = OBJECT_TEMPLATE)
    public KafkaTemplate<?, ?> objectKafkaTemplate(
            LambdaObjectMapper objectMapper,
            @Qualifier(OBJECT_PRODUCER_FACTORY) ProducerFactory<String, Object> objectProducerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(objectProducerFactory);
        template.setProducerListener(new LoggingProducerListener<>());
        template.setMessageConverter(new StringJsonMessageConverter(objectMapper));
        return template;
    }

    /**
     * 配置Kafka消费者工厂
     *
     * @return Kafka消费者工厂
     */
    @Bean
    @Primary
    public ConsumerFactory<?, ?> kafkaConsumerFactory() {
        Map<String, Object> consumerProperties = this.properties.buildConsumerProperties(null);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

    /**
     * 配置字符串类型的监听器容器工厂
     *
     * @param configurer 容器工厂配置器
     * @param kafkaConsumerFactory 消费者工厂
     * @return 字符串类型的监听器容器工厂
     */
    @Bean(name = "stringContainerFactory")
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

    /**
     * 配置JSON类型的监听器容器工厂
     *
     * @param configurer 容器工厂配置器
     * @param kafkaConsumerFactory 消费者工厂
     * @return JSON类型的监听器容器工厂
     */
    @Bean(name = "jsonContainerFactory")
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

    /**
     * 配置对象类型的监听器容器工厂
     *
     * @param configurer 容器工厂配置器
     * @param kafkaConsumerFactory 消费者工厂
     * @param objectMapper 对象映射器
     * @return 对象类型的监听器容器工厂
     */
    @Bean(name = "objectContainerFactory")
    public KafkaListenerContainerFactory<?> objectContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            LambdaObjectMapper objectMapper) {
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

    /**
     * 配置批量对象类型的监听器容器工厂
     *
     * @param configurer 容器工厂配置器
     * @param kafkaConsumerFactory 消费者工厂
     * @param objectMapper 对象映射器
     * @return 批量对象类型的监听器容器工厂
     */
    @Bean(name = "batchObjectContainerFactory")
    public KafkaListenerContainerFactory<?> batchObjectContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            LambdaObjectMapper objectMapper) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConsumerFactory(kafkaConsumerFactory);
        factory.setBatchListener(true);
        factory.setBatchMessageConverter(
                new BatchMessagingMessageConverter(new StringJsonMessageConverter(objectMapper)));
        return factory;
    }

    /**
     * 配置延时Kafka模板
     *
     * @param kafkaTemplate 字符串类型的Kafka模板
     * @return 延时Kafka模板
     */
    @Bean(name = DELAY_TEMPLATE)
    public KafkaDelayTemplate delayKafkaTemplate(
            @Qualifier(STRING_TEMPLATE) KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaDelayTemplate(kafkaTemplate);
    }
}
