package ch.mno.copper.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.HtmlEmail;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dutoitc on 26.04.2019.
 */
public class MailReporterTest {

    @Test
    public void testAll() throws MessagingException, IOException {
        MailReporter reporter = new TestableMailReporter("dummyServer", "aUser", "aPass", 6666, "from@dummy.xxx", "to@dummy.xxx");
        Map<String, String> values = new HashMap<>();
        values.put(MailReporter.PARAMETERS.TO.name(), "to2@dummy.xxx");
        values.put(MailReporter.PARAMETERS.TITLE.name(), "aTitle");
        values.put(MailReporter.PARAMETERS.BODY.name(), "some body");
        reporter.report(null, values);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TestableHtmlEmail.data.writeTo(os);
        String mail = new String(os.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(mail);
        Assert.assertTrue(mail.contains("From: from@dummy.xxx"));
        Assert.assertTrue(mail.contains("Reply-To: to@dummy.xxx"));
        Assert.assertTrue(mail.contains("To: to2@dummy.xxx"));
        Assert.assertTrue(mail.contains("Subject: aTitle"));
        Assert.assertTrue(mail.contains("some body"));
    }

    private static class TestableHtmlEmail extends HtmlEmail {

        private static MimeMessage  data;

        public String sendMimeMessage() {
            this.data = this.getMimeMessage();
            return null;
        }
    }

    private static class TestableMailReporter extends MailReporter {
/*
        HtmlEmail mock = Mockito.mock(HtmlEmail.class);
        {

        }*/

        public TestableMailReporter(String server, String serverUsername, String serverPassword, int serverPort, String from, String replyTo) {
            super(server, serverUsername, serverPassword, serverPort, from, replyTo);
        }

        @Override
        protected HtmlEmail buildHtmlEmail() {
            return new TestableHtmlEmail();
        }
    }

}
