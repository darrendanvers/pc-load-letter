package dev.darrencodes.pcloadletterdb.reading;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Demonstrates using JdbcTemplate and a RowCallbackHandler to process a large ResultSet.
 */
//@SpringBootApplication
public class JdbcTemplateStreamProcess implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateStreamProcess.class);

    private final DataSource dataSource;

    /**
     * RowCallbackHandler that will log the text from each record in the ResultSet and
     * count how many rows it processed.
     */
    private static final class CountingRowCallbackHandler implements RowCallbackHandler {

        int rowsProcessed = 0;

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            String s = rs.getString("text_val");
            logger.info(Util.abbreviate(s, 50));
            this.rowsProcessed++;
        }

        private int getRowsProcessed() {
            return this.rowsProcessed;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(JdbcTemplateStreamProcess.class, args);
    }

    public JdbcTemplateStreamProcess(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        final Instant start = Instant.now();

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

        final CountingRowCallbackHandler rowCallbackHandler = new CountingRowCallbackHandler();
        jdbcTemplate.query("select text_val from pc_load_letter.source", rowCallbackHandler);

        final Instant end = Instant.now();
        logger.info(String.format("%,d rows processed in %,d milliseconds.", rowCallbackHandler.getRowsProcessed(),
                end.toEpochMilli() - start.toEpochMilli()));
    }
}
