package ru.itis.rabbitmq2.models;

public enum ExchangeType {
    FANOUT, DIRECT, TOPIC;

    @Override
    public String toString() {
        return super.name().toLowerCase();
    }
}
