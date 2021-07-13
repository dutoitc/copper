package ch.mno.copper.report;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class Slf4jReporterTest {

    @Test
    void reportMustNotFail() {
        try {
            Map<String, String> map = new HashMap<>();
            new Slf4jReporter("name").report("key", map);
        } catch (RuntimeException e) {
            fail();
        }
    }

    @Test
    void testLogger() {
        Map<String, String> map = new HashMap<>();
        Slf4jReporter reporter = new Slf4jReporter("name");
        reporter.logger = Mockito.mock(Logger.class);
        reporter.report("key", map);
        ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(reporter.logger).info(arg.capture());
        assertEquals("[key]", arg.getAllValues().toString());
    }
}
