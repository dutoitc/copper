package ch.mno.copper.web.helper;

import ch.mno.copper.web.helpers.InstantHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

class InstantHelperTest {

    @Test
    void testDefault() {
        Instant instant = LocalDateTime.parse("2021-07-12T16:13:00").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant, InstantHelper.findInstant(null, instant, false));
        Assertions.assertNull(InstantHelper.findInstant("null", instant, false));
    }

    @Test
    void test1() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T00:00:00").toInstant(ZoneOffset.UTC);
        Instant instant2 = LocalDateTime.parse("2021-07-12T23:59:59").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant1, InstantHelper.findInstant("12.07.2021", null, true));
        Assertions.assertEquals(instant2, InstantHelper.findInstant("12.07.2021", null, false));
    }

    @Test
    void test2() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T00:00:00").toInstant(ZoneOffset.UTC);
        Instant instant2 = LocalDateTime.parse("2021-07-12T23:59:59").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant1, InstantHelper.findInstant("2021-07-12", null, true));
        Assertions.assertEquals(instant2, InstantHelper.findInstant("2021-07-12", null, false));
    }

    @Test
    void test3() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T12:34:00").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant1, InstantHelper.findInstant("2021-07-12 12:34", null, true));
    }

    @Test
    void test4() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T12:34:00").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant1, InstantHelper.findInstant("2021-07-12T12:34", null, true));
    }

    @Test
    void test5() {
        Instant instant1 = LocalDateTime.parse("2021-07-12T12:34:00").toInstant(ZoneOffset.UTC);
        Assertions.assertEquals(instant1, InstantHelper.findInstant("12.07.2021T12:34", null, true));
    }

}
