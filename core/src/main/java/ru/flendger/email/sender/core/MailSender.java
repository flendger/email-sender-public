package ru.flendger.email.sender.core;

import jakarta.mail.MessagingException;

import java.util.Properties;

public interface MailSender {
    void init(Properties properties);
    void setLogin(String login);
    void setPassword(String password);
    void setRecipient(String rc);
    void setCC(String cc);
    void setSender(String sender);
    void setSubject(String subject);
    void setText(String text);
    void send() throws MessagingException;
    void send(String sender, String rc, String cc, String subject, String text) throws MessagingException;
}
