package ru.itis.rabbitmq2.consumers.topic;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.rabbitmq2.lib.ReferenceStrings;
import ru.itis.rabbitmq2.models.PassportData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

public class RussianCitizenshipConsumer {

    public RussianCitizenshipConsumer() {
        start();
    }

    public void start() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(
                    queueName,
                    ReferenceStrings.TOPIC_EXCHANGE_NAME,
                    ReferenceStrings.RUSSIAN_CITIZENSHIP_ROUTING_KEY
            );

            channel.basicConsume(queueName, false, (consumerTag, message) -> {
                String bodyString = new String(message.getBody());

                JsonReader reader = Json.createReader(new StringReader(bodyString));

                JsonObject jsonObject = reader.readObject();

                JsonObject passportDataJson = jsonObject.getJsonObject("passportData");

                PassportData passportData = null;

                try {
                    passportData = PassportData.builder()
                            .name(passportDataJson.getString("name"))
                            .surname(passportDataJson.getString("surname"))
                            .age(passportDataJson.getInt("age"))
                            .passportId(passportDataJson.getString("passportId"))
                            .dateOfIssue(
                                    new SimpleDateFormat("yyyy/MM/dd")
                                            .parse(passportDataJson.getString("dateOfIssue"))
                            )
                            .scan(passportDataJson.getString("scan"))
                            .citizenship(passportDataJson.getString("citizenship"))
                            .build();
                } catch (ParseException e) {
                    System.out.println("Reject happened with " + e.getLocalizedMessage());
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }

                System.out.println("Отображаю текст на русском");

            }, (CancelCallback) null);

        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
