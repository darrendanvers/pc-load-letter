package dev.darrencodes.pcloadletterdb.locking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demonstrates locking and updating records when there is a reasonable expectation that multiple services will be
 * doing so to the same record.
 */
//@SpringBootApplication
public class Locking implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Locking.class);

    private final DataSource dataSource;

    /**
     * TimerTask that will allow the application to select and update records in two separate threads. With this
     * implementation, one will block the other.
     */
    private static final class UpdateTask extends TimerTask {

        private final DataSource dataSource;
        private final long sleepTime;

        private final Timer timer;

        private int doUpdate() throws SQLException {

            try (Connection connection = this.dataSource.getConnection()) {
                connection.setAutoCommit(false);

                // Key point: include "for update" and set the concurrency type to CONCUR_UPDATABLE.
                // This works better if you can constrain on a key in the table because the number of rows locked will be smaller.
                // You may be able to do this with the JdbcTemplate.execute() method, but I was not able to do so. I believe
                // JdbcTemplate is a bit too clever and re-uses connections in a way that keeps that from working.
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, locked_val, owner from pc_load_letter.locking where locked_val = false order by id limit 1 for update",
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        if (!resultSet.next()) {
                            throw new RuntimeException("No record available to update");
                        }

                        final long id = resultSet.getLong("id");
                        logger.info("Updating record with the ID {}.", id);

                        // Artificial delay. The records being updated (and probably more depending on the DB) will
                        // be locked during this time.
                        try {
                            if (this.sleepTime > 0) {
                                Thread.sleep(this.sleepTime);
                            }
                        } catch (InterruptedException e) {
                            logger.error("Unable to sleep requested time period: {}.", e.getMessage());
                        }

                        // Make sure the record has not already been updated.
                        final boolean wasLocked = resultSet.getBoolean("locked_val");
                        if (wasLocked) {
                            return 0;
                        }

                        logger.info("Updating record with the ID {}.", id);
                        resultSet.updateBoolean("locked_val", true);
                        resultSet.updateString("owner", Thread.currentThread().getName());
                        resultSet.updateRow();
                        connection.commit();
                        return 1;
                    }
                }
            }
        }

        private UpdateTask(final DataSource dataSource, final long sleepTime, final Timer timer) {
            this.dataSource = dataSource;
            this.sleepTime = sleepTime;
            this.timer = timer;
        }

        @Override
        public void run() {

            // Do in a loop in case we fail to update the record the first time.
            int rowsUpdated = 0;
            while (rowsUpdated == 0) {

                try {
                    // Try and update the record.
                    rowsUpdated = this.doUpdate();

                    // doUpdate will return 0 if the row we try to update was already updated.
                    if (rowsUpdated == 0) {
                        logger.warn("Unable to update row, retrying.");
                    }
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                    break;
                }
            }

            // We only want to run this once.
            this.timer.cancel();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Locking.class, args);
    }

    public Locking(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        // Timers for new threads.
        final Timer thread1 = new Timer("first-thread");
        final Timer thread2 = new Timer("second-thread");

        // Make this one start right away, but have a long time between the read and write.
        thread1.schedule(new UpdateTask(this.dataSource, 15_000, thread1), 0);

        // Make this one have a brief delay in starting, but read and write in quick succession.
        thread2.schedule(new UpdateTask(this.dataSource, 0, thread2), 5_000);
    }
}
