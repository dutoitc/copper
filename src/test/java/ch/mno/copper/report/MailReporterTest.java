package ch.mno.copper.report;

import ch.mno.copper.collect.connectors.ConnectorException;
import org.apache.commons.mail.HtmlEmail;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by dutoitc on 26.04.2019.
 */
class MailReporterTest {

    @Test
    void testAll() throws ConnectorException, MessagingException, IOException {
        MailReporter reporter = new TestableMailReporter("dummyServer", "aUser", "aPass", 6666, "from@dummy.xxx", "to@dummy.xxx");
        Map<String, String> values = new HashMap<>();
        values.put(MailReporter.PARAMETERS.TO.name(), "to2@dummy.xxx");
        values.put(MailReporter.PARAMETERS.TITLE.name(), "aTitle");
        values.put(MailReporter.PARAMETERS.BODY.name(), "some body");
        reporter.report(null, values);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TestableHtmlEmail.data.writeTo(os);
        String mail = os.toString(StandardCharsets.UTF_8);
        System.out.println(mail);
        assertTrue(mail.contains("From: from@dummy.xxx"));
        assertTrue(mail.contains("Reply-To: to@dummy.xxx"));
        assertTrue(mail.contains("To: to2@dummy.xxx"));
        assertTrue(mail.contains("Subject: aTitle"));
        assertTrue(mail.contains("some body"));
    }

    private static class TestableHtmlEmail extends HtmlEmail {

        private static MimeMessage data;

        public String sendMimeMessage() {
            data = this.getMimeMessage();
            return null;
        }
    }

    private static class TestableMailReporter extends MailReporter {

        public TestableMailReporter(String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
            super(server, serverUsername, serverPassword, serverPort, from, replyTo);
        }

        @Override
        protected HtmlEmail buildHtmlEmail() {
            return new TestableHtmlEmail();
        }
    }

}
