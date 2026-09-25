# Kafka Event-Driven Notification

This Doc demonstrates **event-driven communication using Apache Kafka and Zookeeper** between the Order Service and Notification Service.

When an order is successfully placed, the **Order Service acts as the Kafka Producer** and publishes an `OrderPlaceEvent` containing the order ID to the `notificationTopic`. The **Notification Service acts as the Kafka Consumer**, receives the event, and processes the notification.

## Architecture

```text
                         Order placed successfully
                                  |
                                  v
                         +------------------+
                         |   Order Service  |
                         |     Producer     |
                         +--------+---------+
                                  |
                                  | OrderPlaceEvent
                                  | { orderId }
                                  v
                         +------------------+
                         |      Kafka       |
                         | notificationTopic|
                         +--------+---------+
                                  |
                                  | OrderPlaceEvent
                                  v
                     +------------------------+
                     |  Notification Service  |
                     |        Consumer       |
                     +-----------+------------+
                                 |
                                 v
                          Process Notification
```

## How It Works

The communication follows an asynchronous event-driven approach:

1. The user places an order.
2. The Order Service successfully creates the order.
3. The Order Service creates an `OrderPlaceEvent` using the generated `orderId`.
4. The event is published to the Kafka `notificationTopic`.
5. Kafka stores and delivers the event to the appropriate consumer.
6. The Notification Service consumes the `OrderPlaceEvent`.
7. The Notification Service extracts the `orderId` and performs the notification logic.

## Order Service — Producer

The **Order Service** is responsible for publishing an event after an order has been successfully placed.

```java
kafkaTemplate.send(
    "notificationTopic",
    new OrderPlaceEvent(order.getOrderId())
);
```

Here:

* `KafkaTemplate` is used to publish the message.
* `notificationTopic` is the Kafka topic.
* `OrderPlaceEvent` is the event object.
* `order.getOrderId()` provides the ID of the newly created order.

### OrderPlaceEvent

Instead of sending the order ID directly, the order ID is wrapped inside a custom `OrderPlaceEvent` object.

```java
public class OrderPlaceEvent {

    private String orderId;

    public OrderPlaceEvent(String orderId) {
        this.orderId = orderId;
    }
}
```

This provides a dedicated event structure and allows additional event information to be added in the future without changing the overall messaging approach.

For example, the event contains:

```json
{
  "orderId": "12345"
}
```

## Kafka Topic

The producer publishes the event to:

```text
notificationTopic
```

The topic acts as the communication channel between the Order Service and Notification Service.

```text
Order Service
      |
      | OrderPlaceEvent
      v
notificationTopic
      |
      | OrderPlaceEvent
      v
Notification Service
```

## Notification Service — Consumer

The **Notification Service** consumes messages from the `notificationTopic`.

A Kafka listener is used to receive the event:

```java
@KafkaListener(topics = "notificationTopic")
public void handleNotification(OrderPlaceEvent event) {

    String orderId = event.getOrderId();
}
```

The consumer receives the same `OrderPlaceEvent` published by the Order Service and extracts the `orderId` for further processing.

## Kafka Configuration

Kafka producer and consumer configurations are defined in their respective `application.properties` files.

### Order Service

The Order Service is configured as the Kafka producer:

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.template.default-topic=notificationTopic
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.properties.spring.json.type.mapping=event:com.example.order_service.event.OrderPlaceEvent
```

### Notification Service

The Notification Service is configured as the Kafka consumer:

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.template.default-topic=notificationTopic
spring.kafka.consumer.group-id=notification-service-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.type.mapping=event:com.example.parent.OrderPlaceEvent
```

The `group-id` identifies the consumer group used by the Notification Service.

## Zookeeper

Zookeeper is used to support the Kafka environment in this setup.

```text
+-------------+
|  Zookeeper  |
+------+------+
       |
       v
+-------------+
|    Kafka    |
+-------------+
       |
       +----------------------+
       |                      |
       v                      v
Order Service          Notification Service
  Producer                  Consumer
```


## Why Kafka?

Kafka allows the Order Service and Notification Service to communicate **asynchronously** without directly depending on each other.

Without Kafka:

```text
Order Service -------> Notification Service
       Direct Communication
```

With Kafka:

```text
Order Service -------> Kafka -------> Notification Service
       Producer                       Consumer
```

This decouples the two services and allows the Notification Service to process order events independently.

