package dev.darrencodes.pcloadletterdb.pooling;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This class demonstrates that pooled connections are not a limitless resource and developers should put some
 * thought into their connection pool sizes and release connections as quickly as possible.
 */
public class PoolLimits {

    public static void main(String[] args) {

        // This will turn off the logs that Hikari generates.
        final Logger log = (Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        log.setLevel(Level.WARN);

        final List<Connection> connectionList = new LinkedList<>();

        // the configuration for you.
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/pc-load-letter-db");
        config.setUsername("postgres");
        config.setPassword("p0stgr@s");
        // Set the timout to 1 second, just so the application fails quickly.
        config.setConnectionTimeout(1_000);

        try (HikariDataSource dataSource = new HikariDataSource(config)) {

            for (int i = 0; i < 30; i++) {
                connectionList.add(dataSource.getConnection());
                System.out.printf("%d connections made.\n", i);
            }
            for (Connection connection : connectionList) {
                connection.close();
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
