package dev.darrencodes.pcloadletterdb.locking;

import java.util.Timer;
import java.util.TimerTask;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Demonstrates the problem with selecting a record in one call and updating it in another when there is a reasonable
 * expectation that multiple services will be doing so to the same record.
 */
//@SpringBootApplication
public class NonLocking implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NonLocking.class);

    private final DataSource dataSource;

    /**
     * TimerTask that will allow the application to select and update records in two separate threads. With this
     * implementation, there is no blocking.
     */
    private static final class UpdateTask extends TimerTask {

        private final DataSource dataSource;
        private final long sleepTime;

        private final Timer timer;

        private UpdateTask(final DataSource dataSource, final long sleepTime, final Timer timer) {
            this.dataSource = dataSource;
            this.sleepTime = sleepTime;
            this.timer = timer;
        }

        @Override
        public void run() {

            final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

            // Select the record to update. Note that this query has `for update` in it for parity with the Locking
            // class, but has no effect.
            final Long id = jdbcTemplate.queryForObject("select id from pc_load_letter.locking where locked_val = false order by id limit 1 for update", Long.TYPE);
            logger.info("Updating record with the ID {}.", id);

            // Artificial delay.
            try {
                if (this.sleepTime > 0) {
                    Thread.sleep(this.sleepTime);
                }
            } catch (InterruptedException e) {
                logger.error("Unable to sleep requested time period: {}.", e.getMessage());
            }

            // Update the record.
            jdbcTemplate.update("update pc_load_letter.locking set locked_val = true, owner = ? where id = ?", Thread.currentThread().getName(), id);
            logger.info("Updated record with the ID {}.", id);

            // We only want to run these once.
            this.timer.cancel();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(NonLocking.class, args);
    }

    public NonLocking(final DataSource dataSource) {
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
