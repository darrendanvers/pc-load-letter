package dev.darrencodes.pcloadletterdb.reading;

import java.sql.*;
import java.time.Instant;

/**
 * This class provides an example of using raw JDBC to read and process a large ResultSet.
 */
public class RawJdbc {

    public static void main(String[] args) {

        // Make the connection. This should be in a try-with-resources block.
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pc-load-letter-db",
                "postgres", "p0stgr@s")) {


            // Timing starts after the connection to better mimic the timings of the classes using JdbcTemplate.
            final Instant start = Instant.now();
            int rowsProcessed = 0;

            // ou can provide hints about what you want to do with the results. Doing so may provide efficiencies and
            // keep you from having unintended results. This should be in a try-with-resources block.
            try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {

                // Open the ResultSet. This should be in a try-with-resources block.
                try (ResultSet resultSet = statement.executeQuery("select text_val from pc_load_letter.source")) {

                    // Loop through the results and process them.
                    while (resultSet.next()) {
                        final String s = resultSet.getString("text_val");
                        rowsProcessed++;
                        if (rowsProcessed % 1_000 == 0) {
                            System.out.printf("%s\n", Util.abbreviate(s, 50));
                        }
                    }
                }
            }

            // The timing here will be a bit different from the others since this does not go through the logger, but it's
            // good enough for this demonstration.
            final Instant end = Instant.now();
            System.out.printf("%,d rows processed in %,d milliseconds.\n", rowsProcessed, end.toEpochMilli() - start.toEpochMilli());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
