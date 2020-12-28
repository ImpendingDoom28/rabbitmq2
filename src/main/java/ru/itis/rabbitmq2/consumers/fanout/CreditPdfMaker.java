package ru.itis.rabbitmq2.consumers.fanout;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import ru.itis.rabbitmq2.lib.ReferenceStrings;
import ru.itis.rabbitmq2.models.ExchangeType;
import ru.itis.rabbitmq2.models.PassportData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

public class CreditPdfMaker {

    private final String PDF_NAME = "credit.pdf";
    private final String PDF_DIST = ReferenceStrings.PDFS_DIST.concat(PDF_NAME);

    public CreditPdfMaker() {
        start();
    }

        public void start() {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");

            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                channel.basicQos(2);

                channel.exchangeDeclare(
                        ReferenceStrings.FANOUT_EXCHANGE_NAME,
                        ExchangeType.FANOUT.toString()
                );
                // создаем временную очередь со случайным названием
                String queue = channel.queueDeclare().getQueue();

                // привязали очередь к EXCHANGE_NAME
                channel.queueBind(
                        queue,
                        ReferenceStrings.FANOUT_EXCHANGE_NAME,
                        ""
                );

                DeliverCallback deliverCallback = (consumerTag, message) -> {
                    String bodyString = new String(message.getBody());

                    JsonReader reader = Json.createReader(new StringReader(bodyString));

                    JsonObject jsonObject = reader.readObject();

                    System.out.println(jsonObject);

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
                                .build();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(PDF_DIST));
                        Document document = new Document(pdfDocument);

                        //Font declaration for Cyrillic(Russian) symbols
                        PdfFont f1 = PdfFontFactory.createFont(
                                ReferenceStrings.ARIAL_FONT,
                                "Identity-H",
                                true
                        );

                        document.add(new Paragraph("Заявление на кредит").setFont(f1));
                        document.add(new Paragraph("Я согласен на все условия договора и на обработку моих данных").setFont(f1));
                        document.add(new Paragraph("Данные:").setFont(f1));
                        document.add(new Paragraph(
                                        "Ваше имя: "
                                                .concat(passportData.getName() + "\n")
                                                .concat("Ваша фамилия: " + passportData.getSurname() + "\n")
                                                .concat("Номер и серия паспорта: " + passportData.getPassportId() + "\n")
                                                .concat("Возраст: " + passportData.getAge() + "\n")
                                                .concat("Дата выдачи паспорта: " + passportData.getDateOfIssue() + "\n")
                                ).setFont(f1)
                        );
                        document.close();
                        System.out.println("PDF for Credit was created");
                        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                    } catch (IOException e) {
                        System.out.println("Task was rejected with error: " + e.getLocalizedMessage());
                        channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                    }
                };

                channel.basicConsume(queue, false, deliverCallback, consumerTag -> {
                });
            } catch (IOException | TimeoutException e) {
                throw new IllegalArgumentException(e);
            }
        }
}
