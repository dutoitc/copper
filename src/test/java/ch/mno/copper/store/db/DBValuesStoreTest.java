package ch.mno.copper.store.db;

import ch.mno.copper.store.StoreValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DBValuesStoreTest {

    @Test
    void getValuesMapString() {
        var values = new ArrayList<StoreValue>();
        values.add(new StoreValue(1,"k1", "v1", null, null, null, 1));
        values.add(new StoreValue(2,"k2", "v2", null, null, null, 1));

        var server = Mockito.mock(DBServer.class);
        Mockito.when(server.readLatest()).thenReturn(values);
        var vs = new DBValuesStore(server);

        // Test
        var map = vs.getValuesMapString();
        assertEquals(2, map.size());
    }


}