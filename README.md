# KafkaConsumer
## Overview

This code configures a Kafka consumer that listens to a Kafka topic for messages containing file data, then transfers these files to a remote SFTP server. It integrates Spring Kafka for consuming messages and Spring Integration for handling file transfers over SFTP.

### Key Components

1. **ConsumerFactory & KafkaListenerContainerFactory**
   - The `ConsumerFactory` creates Kafka consumer instances with the required configuration properties.
   - The `KafkaListenerContainerFactory` is used to configure the `@KafkaListener` annotation for concurrent message consumption.

2. **SFTP Session Configuration**
   - The SFTP session is configured using a `SessionFactory`, which defines the connection details like host, port, user credentials, and more.

3. **SFTP File Template**
   - The `SftpRemoteFileTemplate` is used to handle operations on the remote SFTP server, such as uploading files.

4. **Kafka Listener**
   - The `@KafkaListener` method listens to the specified Kafka topic (`my-topic`) and processes incoming messages. The messages are deserialized into `FileWrapper` objects, which contain the file content and file name.

5. **Message Handling**
   - The message handler (`sftpMessageHandler`) is responsible for transferring the received files to a remote SFTP server. The handler uses the SFTP template to upload the files to a specified directory on the server.

### Example Usage

1. **Kafka Consumer Configuration**
   - The consumer is configured to connect to a Kafka broker on `localhost:9092` and belongs to the consumer group `file_consumer_group`.
   - The maximum fetch size is set to 20MB to handle large file transfers.

2. **SFTP Configuration**
   - The SFTP server is set up to run on `localhost` at port `4444` with user credentials (`aamir/aamir123`).
   - The remote directory for file uploads is set to `/testing2`, and the directory is automatically created if it doesn't exist.

3. **File Transfer Process**
   - When a file message is received from the Kafka topic, the file content is wrapped in a `FileWrapper` object.
   - The file is then transferred to the remote SFTP server using the configured SFTP session.

### Code Walkthrough

- **`consumerFactory()`**: Configures Kafka consumer properties like bootstrap servers, group ID, and deserializers.
  
- **`kafkaListenerContainerFactory()`**: Provides a container factory for Kafka listeners.

- **`sftpSessionFactory1()`**: Configures the SFTP session with the necessary connection details.

- **`sftpRemoteFileTemplate()`**: Provides a template for SFTP file operations.

- **`listen(FileWrapper fileWrapper)`**: Listens to the Kafka topic, processes the file data, and triggers the SFTP file transfer.

- **`sftpMessageHandler()`**: Handles the actual file transfer to the remote SFTP server.

### Dependencies

- **Spring Kafka**
- **Spring Integration**
- **Apache Kafka**
- **Java 8+**

### Conclusion

This configuration enables seamless integration between Kafka and an SFTP server, allowing for automated file transfers based on messages received from a Kafka topic. The setup ensures secure file handling and supports large file sizes for efficient data processing and transfer.
