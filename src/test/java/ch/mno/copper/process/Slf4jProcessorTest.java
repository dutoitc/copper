package ch.mno.copper.process;

import ch.mno.copper.collect.connectors.ConnectorException;
import ch.mno.copper.report.Slf4jReporter;
import ch.mno.copper.store.MapValuesStore;
import ch.mno.copper.store.ValuesStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Slf4jProcessorTest {

    @Test
    void testReport() throws ConnectorException {
        List<String> valuesTrigger = new ArrayList<>();
        var proc = new Slf4jProcessor("procname", valuesTrigger);
        proc.reporter = Mockito.mock(Slf4jReporter.class);
        //
        ValuesStore values = new MapValuesStore();
        values.put("key1", "value1");
        values.put("key1", "value2");
        values.put("key2", "value1");
        proc.trig(values, Arrays.asList("key1", "key2"));

        //
        ArgumentCaptor<String> argStr = ArgumentCaptor.forClass(String.class);
        Mockito.verify(proc.reporter).report(argStr.capture(), Mockito.any());
        assertEquals("[Values changed: key1=value2,key2=value1]", argStr.getAllValues().toString());
    }


    @Test
    void testFindKnownKeys() {
        var proc = new Slf4jProcessor("procname", Arrays.asList("key1", "key2"));
        assertEquals("[key2]", proc.findKnownKeys(Arrays.asList("key2", "key3")).toString());
    }

}
