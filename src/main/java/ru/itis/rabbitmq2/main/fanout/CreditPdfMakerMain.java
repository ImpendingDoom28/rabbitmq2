package ru.itis.rabbitmq2.main.fanout;

import ru.itis.rabbitmq2.consumers.fanout.CreditPdfMaker;

public class CreditPdfMakerMain {

    public static void main(String[] args) {
        CreditPdfMaker pdfMaker = new CreditPdfMaker();
    }
}
