package dev.darrencodes.pcloadletterdb.resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class demonstrates the need to close database resources after use. The application will quickly abend.
 */
public class ExhaustingConnections {

    public static void main(String[] args) {

        try {
            for (int i = 0; i < 2_000; i++) {
                // These connections are opened but never closed.
                final Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pc-load-letter-db",
                        "postgres", "p0stgr@s");
                if (i % 10 == 0) {
                    System.out.printf("%,d connections made.\n", i);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
