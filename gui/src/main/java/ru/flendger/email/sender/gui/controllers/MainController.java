package ru.flendger.email.sender.gui.controllers;

import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.flendger.email.sender.core.JakartaMailSender;
import ru.flendger.email.sender.core.MailSender;
import ru.flendger.email.sender.gui.model.SenderSchedule;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class MainController implements Initializable {

    private MailSender mailSender;
    private Properties properties;

    private Task<Void> sendingTask;
    private SenderSchedule senderSchedule;

    public TextArea textArea;
    public TextField toField;
    public TextField copyField;
    public TextField fromField;
    public TextField subjectField;
    public TextField hostField;
    public Spinner<Integer> portField;
    public TextField loginField;
    public PasswordField passwordField;
    public CheckBox mondayCheck;
    public CheckBox tuesdayCheck;
    public CheckBox wednesdayCheck;
    public CheckBox thursdayCheck;
    public CheckBox fridayCheck;
    public CheckBox saturdayCheck;
    public CheckBox sundayCheck;
    public Spinner<Integer> hourField;
    public Spinner<Integer> minuteField;
    public Label statusLabel;
    public Label lastLabel;
    public Label nextLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StringConverter<Integer> converter = new IntegerStringConverter() {
            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "00";
                }
                return new DecimalFormat("00").format(value);
            }
        };
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isEmpty()) {
                return null;
            }

            if (text.matches("[0-9]*")) {
                return change;
            }

            return null;
        };
        hourField.getEditor().setTextFormatter(new TextFormatter<>(converter, -1, filter));
        minuteField.getEditor().setTextFormatter(new TextFormatter<>(converter, -1, filter));
        portField.getEditor().setTextFormatter(new TextFormatter<>(filter));

        SpinnerValueFactory<Integer> portValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99999, 465);
        portField.setValueFactory(portValueFactory);
        SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9);
        hourField.setValueFactory(hourValueFactory);
        SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteField.setValueFactory(minuteValueFactory);

        senderSchedule = new SenderSchedule();
        mailSender = new JakartaMailSender();
        properties = new Properties();
        mailSender.init(properties);
        properties.put("mail.transport.protocol", "smtps");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.debug", "true");

        //TEST INIT PARAMETERS

        //DEFAULT VALUES
        subjectField.setText("Работу начал %date%");
        mondayCheck.setSelected(true);
        tuesdayCheck.setSelected(true);
        wednesdayCheck.setSelected(true);
        thursdayCheck.setSelected(true);
        fridayCheck.setSelected(true);

        setSchedule();
        refreshStatusFields();
    }

    public void onSendClick() {
        sendMessage();
    }

    private void sendMessage() {
        properties.put("mail.smtp.host", hostField.getText());
        properties.put("mail.smtp.port", portField.getEditor().getText());
        mailSender.setLogin(loginField.getText());
        mailSender.setPassword(passwordField.getText());
        try {
            mailSender.send(fromField.getText(),
                    toField.getText(),
                    copyField.getText(),
                    getSubject(subjectField.getText()),
                    textArea.getText());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String getSubject(String subject) {
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return subject.replaceAll("%date%", date);
    }

    public void startSending() {
        if (senderSchedule.getStatus()) return;

        if (sendingTask == null || sendingTask.getState() != Worker.State.RUNNING) {
            sendingTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    setSchedule();
                    senderSchedule.setNextDateTime(senderSchedule.getScheduleDateTime());
                    while (senderSchedule.getStatus() && senderSchedule.isDaySelected()) {
                        if (LocalDateTime.now().compareTo(senderSchedule.getNextDateTime()) >= 0) {
                            sendMessage();
                            senderSchedule.setLastDateTime(LocalDateTime.now());
                            setSchedule();

                            if (!senderSchedule.isDaySelected()) break;

                            senderSchedule.setNextDateTime(senderSchedule.getScheduleDateTime());
                        }

                        Platform.runLater(() -> refreshStatusFields());
                        Thread.sleep(2000);
                    }
                    senderSchedule.stop();
                    Platform.runLater(() -> refreshStatusFields());
                    return null;
                }
            };
        }

        senderSchedule.start();
        Thread thread = new Thread(sendingTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setSchedule() {
        senderSchedule.setSchedule(mondayCheck.isSelected(),
                                    tuesdayCheck.isSelected(),
                                    wednesdayCheck.isSelected(),
                                    thursdayCheck.isSelected(),
                                    fridayCheck.isSelected(),
                                    saturdayCheck.isSelected(),
                                    sundayCheck.isSelected(),
                                    Integer.parseInt(hourField.getEditor().getText()),
                                    Integer.parseInt(minuteField.getEditor().getText()));
    }

    public void stopSending() {
        senderSchedule.stop();
        refreshStatusFields();
    }

    private void refreshStatusFields() {
        statusLabel.setText(senderSchedule.getStatus() ? "ACTIVE" : "STOPPED");
        lastLabel.setText(senderSchedule.getLastDateTime() == null ?
                "--/--" : senderSchedule.getLastDateTime()
                                    .format(DateTimeFormatter
                                            .ofPattern("dd.MM.yyyy HH:mm:ss")));
        nextLabel.setText(senderSchedule.getNextDateTime() == null ?
                "--/--" : senderSchedule.getNextDateTime()
                                .format(DateTimeFormatter
                                        .ofPattern("dd.MM.yyyy HH:mm:ss")));
    }
}
