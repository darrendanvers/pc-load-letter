package dev.darrencodes.pcloadletterdb.loading;

import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * This class serves two purposes. The first is to load some data for other demonstrations. The second is to demonstrate
 * the performance gain from JDBC bulk inserts.
 */
public class DataInsert {

    private static final String INSERT_SQL = "insert into pc_load_letter.source (id, text_val) values (?, ?)";

    private static final int MAX_STRING_SIZE = 5_000;
    private static final int STRING_LOWER_BOUND = 48;
    private static final int STRING_UPPER_BOUND = 122;

    public static void main(String[] args) {

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pc-load-letter-db",
                "postgres", "p0stgr@s")) {
            connection.setAutoCommit(false);

            // Generate a bunch of random strings so this doesn't affect the timing comparison.
            final List<String> generatedStrings = genRandomStringList(50_000);

            // So and time the slow insert.
            Instant start = Instant.now();
            int rowsInserted = slowSourceInsert(connection, 0, 50_000, generatedStrings);
            connection.commit();
            Instant end = Instant.now();
            System.out.printf("%,d records inserted in %,d milliseconds the slow way.\n", rowsInserted, end.toEpochMilli() - start.toEpochMilli());

            // Do and time the fast insert.
            start = Instant.now();
            rowsInserted = fastSourceInsert(connection, 50_000, 1_000_000, generatedStrings);
            connection.commit();
            end = Instant.now();
            System.out.printf("%,d records inserted in %,d milliseconds the fast way.\n", rowsInserted, end.toEpochMilli() - start.toEpochMilli());

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Demonstrates a bunch of inserts that does not use JDBC bulk insert.
    private static int slowSourceInsert(final Connection connection, final int start, final int count,
                                        final List<String> textValues) throws SQLException {

        int totalRowsInserted = 0;

        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            for (int i = 0; i < count; i++) {
                final int rowNum = start + i;
                preparedStatement.setLong(1, rowNum);
                preparedStatement.setString(2, textValues.get(i % textValues.size()));
                // Send each row to the DB as it is generated.
                totalRowsInserted += preparedStatement.executeUpdate();
                if (i % 1_000 == 0) {
                    System.out.print(".");
                }
            }
        }

        System.out.println();
        return totalRowsInserted;
    }

    // Demonstrates inserts with JDBC bulk insert.
    private static int fastSourceInsert(final Connection connection, final int start, final int count,
                                        final List<String> textValues) throws SQLException {

        int totalRowsInserted = 0;

        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            boolean needsFlush = false;

            for (int i = 0; i < count; i++) {
                final int rowNum = start + i;
                preparedStatement.setLong(1, rowNum);
                preparedStatement.setString(2, textValues.get(i % textValues.size()));
                // This is the key difference. This batches stuff to send over to the DB in chunks rather than
                // one at a time.
                preparedStatement.addBatch();
                needsFlush = true;

                // To keep the buffer from becoming too big, flush it every 1,000 records. This is a parameter
                // developers can play with.
                if (i % 1_000 == 0) {
                    final int[] rowsInserted =  preparedStatement.executeBatch();
                    totalRowsInserted += Arrays.stream(rowsInserted).sum();
                    needsFlush = false;
                    System.out.print(".");
                }
            }

            // The driver will not automatically flush the buffer on close. If there is stuff still in the buffer,
            // you have to explicitly send it to the DB.
            if (needsFlush) {
                final int[] rowsInserted =  preparedStatement.executeBatch();
                totalRowsInserted += Arrays.stream(rowsInserted).sum();
                System.out.print(".");
            }
        }

        System.out.println();
        return totalRowsInserted;
    }


    // Generates a list of numberToGen random Strings.
    private static List<String> genRandomStringList(final int numberToGen) {

        final List<String> toReturn = new ArrayList<>(numberToGen);
        for (int i = 0; i < numberToGen; i++) {
            toReturn.add(genRandomString());
        }

        return toReturn;
    }

    // Generates a random String of up to 5,000 characters.
    private static String genRandomString() {

        final Random randomGenerator = new Random();

        final int numChars = randomGenerator.nextInt(MAX_STRING_SIZE);
        return randomGenerator.ints(STRING_LOWER_BOUND, STRING_UPPER_BOUND + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(numChars)
                .collect(() -> new StringBuilder(numChars), StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
