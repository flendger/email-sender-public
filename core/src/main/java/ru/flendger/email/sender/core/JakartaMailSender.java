package ru.flendger.email.sender.core;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class JakartaMailSender implements MailSender{

    private Properties properties;
    private String login;
    private String password;

    private String recipient = "";
    private String cc = "";
    private String sender = "";
    private String subject = "";
    private String text = "";


    @Override
    public void init(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setRecipient(String rc) {
        this.recipient = rc;
    }

    @Override
    public void setCC(String cc) {
        this.cc = cc;
    }

    @Override
    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void send() throws MessagingException{
        Session session = Session.getDefaultInstance(properties,
                new jakarta.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password);
                    }
                });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        if (!cc.isEmpty()) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        }
        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }

    @Override
    public void send(String sender, String rc, String cc, String subject, String text) throws MessagingException {
        setSender(sender);
        setRecipient(rc);
        setCC(cc);
        setSubject(subject);
        setText(text);
        send();
    }
}
