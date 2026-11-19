package io.xream.sqli.dialect;

import io.xream.sqli.exception.NotSupportedException;
import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IoTDBDialectTest {

    private final IoTDBDialect dialect = new IoTDBDialect();

    @Test
    public void usesIoTDBKeyAndTableModelInsertSql() {
        Parsed parsed = new Parsed(Device.class);
        parsed.setTableName("device_metrics");
        BeanElement time = element("time", Timestamp.class);
        BeanElement temperature = element("temperature", Double.class);
        temperature.setMapper("temperature_c");

        assertEquals("iotdb", dialect.getKey());
        assertEquals("INSERT INTO device_metrics ( time , temperature_c ) VALUES (?,?)",
                dialect.createSql(parsed, Arrays.asList(time, temperature)));
    }

    @Test
    public void usesLimitOffsetAndSupportsLimitOne() {
        StringBuilder sql = dialect.buildPageSql(new StringBuilder("SELECT * FROM device_metrics"), 20, 10, 0);

        assertEquals("SELECT * FROM device_metrics LIMIT 10 OFFSET 20", sql.toString());
        assertEquals(" LIMIT 1", dialect.getLimitOne());
    }

    @Test
    public void convertsUtilDateToJdbcTimestampAndPreservesSqlTemporalValues() {
        Date date = new Date(123456789L);
        Object converted = dialect.filterValue(date);

        assertTrue(converted instanceof Timestamp);
        assertEquals(date.getTime(), ((Timestamp) converted).getTime());

        Timestamp timestamp = Timestamp.valueOf("2026-11-18 12:34:56.123456789");
        assertSame(timestamp, dialect.filterValue(timestamp));

        java.sql.Date sqlDate = java.sql.Date.valueOf("2026-11-18");
        assertSame(sqlDate, dialect.filterValue(sqlDate));
    }

    @Test
    public void mapsJdbcTimestampToEntityDateTypes() {
        Timestamp value = Timestamp.valueOf("2026-11-18 12:34:56.123");
        Date mappedDate = (Date) dialect.mappingToObject(value, element("date", Date.class));
        LocalDateTime mappedDateTime = (LocalDateTime) dialect.mappingToObject(value,
                element("dateTime", LocalDateTime.class));

        assertEquals(value.getTime(), mappedDate.getTime());
        assertEquals(value.toLocalDateTime(), mappedDateTime);
    }

    @Test
    public void rejectsMySqlReplaceSemantics() {
        try {
            dialect.createOrReplaceSql("INSERT INTO device_metrics (time) VALUES (?)");
            fail("IoTDB must not advertise MySQL REPLACE semantics");
        } catch (NotSupportedException expected) {
            assertTrue(expected.getMessage().contains("IoTDB"));
        }
    }

    private static BeanElement element(String name, Class<?> type) {
        BeanElement element = new BeanElement();
        element.setProperty(name);
        element.setClz(type);
        return element;
    }

    private static final class Device {
        private Timestamp time;
        private Double temperature;
    }
}
