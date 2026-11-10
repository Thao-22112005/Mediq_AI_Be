package com.mediqai.appointment.config;

public class RabbitMQConfig {

    public static final String EXCHANGE_NAME =
            "mediqai.notification.exchange";

    public static final String ROUTING_KEY =
            "notification.email";
}