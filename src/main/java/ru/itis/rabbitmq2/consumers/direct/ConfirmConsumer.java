package ru.itis.rabbitmq2.consumers.direct;

import com.rabbitmq.client.*;
import org.apache.commons.io.FileUtils;
import ru.itis.rabbitmq2.lib.ReferenceStrings;
import ru.itis.rabbitmq2.models.ExchangeType;
import ru.itis.rabbitmq2.models.PassportData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

public class ConfirmConsumer {

    public ConfirmConsumer() {
        start();
    }

    public void start() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, message) -> {
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
                            .build();
                } catch (ParseException e) {
                    System.out.println("Reject happened with " + e.getLocalizedMessage());
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }

                File template = generateHtmlFromData(passportData);
                Desktop.getDesktop().browse(template.toURI());
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            };

            CancelCallback cancelCallback = (parameter) -> {};

            channel.basicConsume(ReferenceStrings.DIRECT_QUEUE_NAME, false, deliverCallback, cancelCallback);

        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    private File generateHtmlFromData(PassportData passportData) {
        File htmlTemplateFile = new File(ReferenceStrings.CONFIRM_HTML_DIST);
        File newHtmlFile = null;
        String htmlString = null;
        try {
            htmlString = FileUtils.readFileToString(htmlTemplateFile, "UTF-8");
            htmlString = htmlString.replace("$$data", passportData.getName());
            String scanHtmlElement = "<img src=\"" + passportData.getScan() + "\"  width=\"512\" height=\"512\"/>";
            htmlString = htmlString.replace("$$scan", scanHtmlElement);
            newHtmlFile = new File(ReferenceStrings.CONFIRM_TEMPLATE_DIST);
            newHtmlFile.createNewFile();
            FileUtils.writeStringToFile(newHtmlFile, htmlString, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newHtmlFile;
    }
}
