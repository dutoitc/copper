package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class MailReporter implements AbstractReporter {
    private Logger LOG = LoggerFactory.getLogger(getClass());

    public enum PARAMETERS {TO, TITLE, BODY}

    private static long nbMessageInHour=0;
    private static long hour = -1;
    private static int MAX_MESSAGE_PER_HOUR = 10;
    private String server;
    private String serverUsername;
    private String serverPassword;
    private String from;
    private String replyTo;

    public MailReporter(String server, String serverUsername, String serverPassword, String from, String replyTo) {
        this.server = server;
        this.serverUsername = serverUsername;
        this.serverPassword = serverPassword;
        this.from = from;
        this.replyTo = replyTo;
    }

    @Override
    public void report(String message, Map<String, String> values) throws ConnectorException {
        int currHour = new Date().getHours();
        if (currHour==hour) {
            if (nbMessageInHour> MAX_MESSAGE_PER_HOUR) {
                LOG.warn("Too much message for this hour, skipping message: " + message);
                return;
            }
        } else {
            hour = currHour;
            nbMessageInHour=0;
        }
        nbMessageInHour++;

        try {
            HtmlEmail email = new HtmlEmail();
            email.setSmtpPort(587);
            email.setAuthenticator(new DefaultAuthenticator(serverUsername, serverPassword));
//            email.setSSLOnConnect(true);
            email.setHostName(server);
            String to = values.get(PARAMETERS.TO.toString());
            email.addTo(to);
            email.setFrom(from);
            if (replyTo!=null) email.addReplyTo(replyTo);
            email.setSubject(values.get(PARAMETERS.TITLE.toString()));

            // set the html message
            email.setHtmlMsg(values.get(PARAMETERS.BODY.toString()));

            // set the alternative message
            email.setTextMsg(values.get(PARAMETERS.BODY.toString())); // Fixme: convert to text

            // send the email
            email.send();
            LOG.info("Mail sent to " + to + ": " + email.getSubject());
        }catch (Exception e) {
            LOG.error("Cannot send mail: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws ConnectorException {

        MailReporter mr = new MailReporter(args[0], args[1], args[2], args[3], args[4]);
        Map<String, String> values = new HashMap<>();
        values.put(PARAMETERS.TO.toString(), args[5]);
        values.put(PARAMETERS.TITLE.toString(), "aTitle a b c");
        values.put(PARAMETERS.BODY.toString(), "html");
        mr.report("aMessage", values);
    }

}
