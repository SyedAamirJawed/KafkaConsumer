package com.kafka.config;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import com.kafka.model.FileWrapper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.remote.handler.FileTransferringMessageHandler;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, FileWrapper> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "file_consumer_group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, FileWrapper.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 20 * 1024 * 1024); 
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 20 * 1024 * 1024); 
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FileWrapper> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FileWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public SessionFactory<DirEntry> sftpSessionFactory1() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("localhost");
        factory.setPort(4444);
        factory.setUser("aamir");
        factory.setPassword("aamir123");
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate() {
        return new SftpRemoteFileTemplate(sftpSessionFactory1());
    }

    @KafkaListener(topics = "my-topic", groupId = "file_consumer_group")
    public void listen(FileWrapper fileWrapper) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileWrapper.getFileContent());
            Message<?> message = MessageBuilder.withPayload(byteArrayInputStream)
                    .setHeader("file_name", fileWrapper.getFileName())
                    .build();
            sftpMessageHandler().handleMessage(message);
            System.out.println("File transferred to SFTP ------->  " + fileWrapper.getFileName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Bean
    @ServiceActivator(inputChannel = "sftpChannel")
    public MessageHandler sftpMessageHandler() {
        FileTransferringMessageHandler<DirEntry> handler = new FileTransferringMessageHandler<>(sftpRemoteFileTemplate());
        handler.setRemoteDirectoryExpressionString("'/testing2'");
        handler.setAutoCreateDirectory(true);
        return handler;
    }
}
