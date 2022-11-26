package dev.darrencodes.pcloadletterdb.pooling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This class demonstrates the performance gain from using database connection pooling.
 */
public class PoolingTiming {

    public static void main(String[] args) {

        // This will turn off the logs that Hikari generates.
        final Logger log = (Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        log.setLevel(Level.WARN);

        try {
            // Make 2,000 non-pooled connections to the database and report the timing.
            Instant start = Instant.now();
            noPooling(2_000);
            Instant end = Instant.now();
            System.out.printf("2,000 non-pooled connections made in %,d milliseconds.\n", end.toEpochMilli() - start.toEpochMilli());

            // Make 2,000 pooled connections to the database and report the timing.
            start = Instant.now();
            pooling(2_000);
            end = Instant.now();
            System.out.printf("2,000 pooled connections made in %,d milliseconds.\n", end.toEpochMilli() - start.toEpochMilli());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Make a bunch of connections using the DB pool.
    private static void pooling(final int connectionCount) throws SQLException {

        // When you use Spring, it will use Hikari by default and manage all
        // the configuration for you.
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/pc-load-letter-db");
        config.setUsername("postgres");
        config.setPassword("p0stgr@s");

        try (HikariDataSource dataSource = new HikariDataSource(config)) {

            // This will keep re-using the same connection each time.
            for (int i = 0; i < connectionCount; i++) {
                try (Connection connection = dataSource.getConnection()) {
                    if (i % 10 == 0) {
                        System.out.print(".");
                    }
                }
            }
        }
        System.out.println();
    }

    // Make a bunch of raw connections.
    private static void noPooling(final int connectionCount) throws SQLException {

        for (int i = 0; i < connectionCount; i++) {
            // This will make a new connection to the database each iteration of the loop.
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pc-load-letter-db",
                    "postgres", "p0stgr@s")) {
                if (i % 10 == 0) {
                    System.out.print(".");
                }
            }
        }
        System.out.println();
    }
}
