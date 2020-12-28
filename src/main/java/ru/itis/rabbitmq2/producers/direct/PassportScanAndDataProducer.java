package ru.itis.rabbitmq2.producers.direct;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.rabbitmq2.lib.ReferenceStrings;
import ru.itis.rabbitmq2.models.ExchangeType;
import ru.itis.rabbitmq2.models.PassportData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class PassportScanAndDataProducer {

    public PassportScanAndDataProducer() {
        start();
    }

    public void start() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Writer writer = new StringWriter()) {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создаем exchange
            channel.exchangeDeclare(
                    ReferenceStrings.DIRECT_EXCHANGE_NAME,
                    ExchangeType.DIRECT.toString()
            );

            channel.queueDeclare(
                    ReferenceStrings.DIRECT_QUEUE_NAME,
                    false,
                    false,
                    false,
                    null
            );

            // подвязываем очередь по нужному роутинг ключу
            channel.queueBind(
                    ReferenceStrings.DIRECT_QUEUE_NAME,
                    ReferenceStrings.DIRECT_EXCHANGE_NAME,
                    ReferenceStrings.DIRECT_CONFIRM_ROUTING_KEY
            );

            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите ваши данные: ");
            System.out.println("1) Имя");
            System.out.println("2) Фамилия ");
            System.out.println("3) Номер паспорта");
            System.out.println("4) Ваш возраст");
            System.out.println("5) Дата выдачи паспорта в формате yyyy-mm-dd");
            System.out.println("6) А также скан вашего паспорта"); // passport.jpg

            PassportData passportData = PassportData.builder()
                    .name(scanner.nextLine())
                    .surname(scanner.nextLine())
                    .passportId(scanner.nextLine())
                    .age(Integer.parseInt(scanner.nextLine()))
                    .dateOfIssue(Date.valueOf(scanner.nextLine()))
                    .scan(scanner.nextLine())
                    .build();

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("passportData",
                            Json.createObjectBuilder()
                                    .add("name", passportData.getName())
                                    .add("surname", passportData.getSurname())
                                    .add("passportId", passportData.getPassportId())
                                    .add("age", passportData.getAge())
                                    .add("dateOfIssue", new SimpleDateFormat("yyyy/MM/dd")
                                            .format(passportData.getDateOfIssue()))
                                    .add("scan", ReferenceStrings.SCANS_DIST.concat(passportData.getScan())));

            JsonObject jsonObject = jsonObjectBuilder.build();
            Json.createWriter(writer).write(jsonObject);

            channel.basicPublish(
                    ReferenceStrings.DIRECT_EXCHANGE_NAME,
                    ReferenceStrings.DIRECT_CONFIRM_ROUTING_KEY,
                    null,
                    writer.toString().getBytes()
            );
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
