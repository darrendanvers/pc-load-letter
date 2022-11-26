package dev.darrencodes.pcloadletterdb.resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is the counterpoint to ExhaustingConnections. It will successfully, though relatively slowly, run
 * to completion.
 */
public class ReleasingConnections {

    public static void main(String[] args) {

        try {
            for (int i = 0; i < 2_000; i++) {
                // We are making connections inside a try-with-resources block. This will close the connection
                // after connection leaves scope.
                try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pc-load-letter-db",
                        "postgres", "p0stgr@s")) {
                    if (i % 10 == 0) {
                        System.out.printf("%,d connections made.\n", i);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
