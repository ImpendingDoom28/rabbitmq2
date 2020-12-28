package ru.itis.rabbitmq2.lib;

public class ReferenceStrings {

    public final static String PDFS_DIST = "pdfs/";
    public final static String ASSETS_DIST = "C:\\Program Files\\JavaProjects\\Minecraft Modding\\1.7.10\\RabbitMqHomeTask2\\assets\\";
    public final static String SCANS_DIST = ASSETS_DIST.concat("img\\");
    public final static String ARIAL_FONT = ASSETS_DIST.concat("fonts/arial.ttf");
    public final static String TEMPLATES_DIST = ASSETS_DIST.concat("html\\");
    public final static String CONFIRM_HTML_DIST = TEMPLATES_DIST.concat("confirm.html");
    public final static String CONFIRM_TEMPLATE_DIST = TEMPLATES_DIST.concat("confirm_template.html");

    public final static String FANOUT_EXCHANGE_NAME = "PDFs";

    public final static String DIRECT_EXCHANGE_NAME = "ToBeConfirmed";
    public final static String DIRECT_QUEUE_NAME = "ConfirmDirectQueue";
    public final static String DIRECT_CONFIRM_ROUTING_KEY = "Confirm";

    public final static String TOPIC_EXCHANGE_NAME = "Citizenship";
    public final static String RUSSIAN_CITIZENSHIP_ROUTING_KEY = "citizenship.ru";
    public final static String GERMAN_CITIZENSHIP_ROUTING_KEY = "citizenship.de";

}
