package ch.mno.copper.store.db;

import ch.mno.copper.store.data.InstantValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InstantValueMapperTest {

    @Test
    void testAll() throws SQLException {
        Instant instant = Instant.parse("2021-07-13T11:42:00Z");
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getLong("idValueStore")).thenReturn(42L);
        Mockito.when(rs.getString("c1")).thenReturn("key1");
        Mockito.when(rs.getString("vvalue")).thenReturn("value1");
        Mockito.when(rs.getTimestamp("ts")).thenReturn(Timestamp.from(instant));

        // Run
        InstantValue iv = InstantValueMapper.map(rs);

        // Check
        assertEquals(42l, iv.getId());
        assertEquals("key1", iv.getKey());
        assertEquals("value1", iv.getValue());
        assertEquals(instant, iv.getTimestamp());
    }

    @Test
    void testDefault() throws SQLException {
        Instant instant = Instant.parse("2021-07-13T11:42:00Z");
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString("c1")).thenReturn("key1");
        Mockito.when(rs.getString("vvalue")).thenReturn("value1");

        // Run
        InstantValue iv = InstantValueMapper.map(rs);

        // Check
        assertEquals(-1, iv.getId());
        assertEquals("key1", iv.getKey());
        assertEquals("value1", iv.getValue());
        assertNull(iv.getTimestamp());
    }

}
