package io.xream.sqli.dialect;

import io.xream.sqli.parser.BeanElement;
import io.xream.sqli.parser.Parsed;
import org.junit.Assume;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class IoTDBRemoteSmokeTest {

    @Test
    public void writesAndPagesRowsThroughIoTDBTableDialect() throws Exception {
        String url = setting("IOTDB_TEST_JDBC_URL");
        Assume.assumeTrue("Set IOTDB_TEST_JDBC_URL to an explicit local JDBC tunnel to opt in",
                url != null && !url.isBlank());
        Assume.assumeTrue("The live smoke test requires the IoTDB Table dialect",
                url.contains("sql_dialect=table"));
        Assume.assumeTrue("Use a local SSH tunnel so the test cannot touch another host",
                url.startsWith("jdbc:iotdb://127.0.0.1:") || url.startsWith("jdbc:iotdb://localhost:"));

        String database = "dbeco_sqli_smoke_" + UUID.randomUUID().toString().replace("-", "");
        long firstTime = System.currentTimeMillis();
        IoTDBDialect dialect = new IoTDBDialect();
        boolean created = false;
        try (Connection connection = connect(url); Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + database);
            created = true;
            statement.execute("USE " + database);
            statement.execute("CREATE TABLE reading (station STRING TAG, temperature DOUBLE FIELD)");

            Parsed parsed = new Parsed(Reading.class);
            parsed.setTableName("reading");
            String insertSql = dialect.createSql(parsed, Arrays.asList(
                    element("time", Timestamp.class),
                    element("station", String.class),
                    element("temperature", Double.class)));
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                for (int i = 0; i < 2; i++) {
                    insert.setTimestamp(1, new Timestamp(firstTime + i));
                    insert.setString(2, "station_a");
                    insert.setDouble(3, 12.5 + i);
                    assertFalse("IoTDB INSERT should not return a result set", insert.execute());
                }
            }

            StringBuilder page = dialect.buildPageSql(
                    new StringBuilder("SELECT time, temperature FROM reading WHERE station = 'station_a' ORDER BY time"),
                    1, 1, 0);
            try (ResultSet rows = statement.executeQuery(page.toString())) {
                assertTrue("second page should contain the later row", rows.next());
                assertEquals(firstTime + 1, rows.getTimestamp("time").getTime());
                assertEquals(13.5, rows.getDouble("temperature"), 0.000001);
                assertFalse("second page should contain exactly one row", rows.next());
            }
        } finally {
            if (created) {
                try (Connection cleanup = connect(url); Statement statement = cleanup.createStatement()) {
                    statement.execute("DROP DATABASE " + database);
                }
            }
        }
    }

    private static Connection connect(String url) throws Exception {
        Class.forName("org.apache.iotdb.jdbc.IoTDBDriver");
        Properties properties = new Properties();
        properties.setProperty("user", settingOrDefault("IOTDB_TEST_USER", "root"));
        properties.setProperty("password", settingOrDefault("IOTDB_TEST_PASSWORD", "root"));
        return DriverManager.getConnection(url, properties);
    }

    private static String setting(String name) {
        String property = System.getProperty(name);
        return property == null || property.isBlank() ? System.getenv(name) : property;
    }

    private static String settingOrDefault(String name, String defaultValue) {
        String value = setting(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static BeanElement element(String name, Class<?> type) {
        BeanElement element = new BeanElement();
        element.setProperty(name);
        element.setClz(type);
        return element;
    }

    private static final class Reading {
        public Timestamp time;
        public String station;
        public Double temperature;
    }
}
