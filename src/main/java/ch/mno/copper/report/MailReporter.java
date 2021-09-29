package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class MailReporter implements AbstractReporter {
    private static final Logger LOG = LoggerFactory.getLogger(MailReporter.class);
    private static final int MAX_MESSAGE_PER_HOUR = 10;
    private static long nbMessageInHour = 0;
    private static long hour = -1;
    private String server;
    private String serverUsername;
    private String serverPassword;
    private int serverPort;
    private String from;
    private String replyTo;
    public MailReporter(String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
        this.server = server;
        this.serverUsername = serverUsername;
        this.serverPassword = serverPassword;
        this.serverPort = serverPort;
        this.from = from;
        this.replyTo = replyTo;
    }

    @SuppressWarnings("java:S2696")
    @Override
    public void report(String message, Map<String, String> values) throws ConnectorException {
        int currHour = new GregorianCalendar().get(Calendar.HOUR);
        if (currHour == MailReporter.hour) {
            if (MailReporter.nbMessageInHour > MAX_MESSAGE_PER_HOUR) {
                LOG.warn("Too much message for this hour, skipping message: {}", message);
                return;
            }
        } else {
            MailReporter.hour = currHour;
            MailReporter.nbMessageInHour = 0;
        }
        MailReporter.nbMessageInHour++;

        try {
            HtmlEmail email = buildHtmlEmail();
            email.setSmtpPort(serverPort);
            if (serverUsername != null) {
                email.setAuthenticator(new DefaultAuthenticator(serverUsername, serverPassword));
            }
            email.setHostName(server);
            email.setCharset(org.apache.commons.mail.EmailConstants.UTF_8);
            String to = values.get(PARAMETERS.TO.toString());
            String[] tos = to.split("[,;]");
            for (String to2 : tos) {
                email.addTo(to2);
            }

            int p1 = from.indexOf('<');
            int p2 = from.indexOf('>', p1 + 1);
            if (p1 > 0 && p2 > p1) {
                email.setFrom(from.substring(0, p1), from.substring(p1 + 1, p2));
            } else {
                email.setFrom(from);
            }
            if (replyTo != null) email.addReplyTo(replyTo);
            email.setSubject(values.get(PARAMETERS.TITLE.toString()));

            // set the html message
            email.setHtmlMsg(values.get(PARAMETERS.BODY.toString()));

            // set the alternative message
            email.setTextMsg(values.get(PARAMETERS.BODY.toString()));

            // send the email
            String d = email.send();
            LOG.info("Mail sent to {}: {}, reponse={}", to, email.getSubject(), d);
        } catch (Exception e) {
            LOG.error("Cannot send mail: " + e.getMessage(), e);
        }
    }

    // Method for testing purpose
    protected HtmlEmail buildHtmlEmail() {
        return new HtmlEmail();
    }

    public enum PARAMETERS {TO, TITLE, BODY}

}