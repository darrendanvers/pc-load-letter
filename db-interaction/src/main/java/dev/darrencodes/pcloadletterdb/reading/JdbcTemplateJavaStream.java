package dev.darrencodes.pcloadletterdb.reading;

import java.time.Instant;
import java.util.function.Consumer;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Demonstrates reading and processing a large ResultSet using JdbcTemplate and its internal Stream generation
 * methods.
 */
//@SpringBootApplication
public class JdbcTemplateJavaStream implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateJavaStream.class);

    private final DataSource dataSource;

    /**
     * Consumer that will log each String passed to it and count how many Strings it has consumed.
     */
    private static final class CountingConsumer implements Consumer<String> {

        private int rowsProcessed = 0;

        @Override
        public void accept(String s) {
            logger.info(Util.abbreviate(s, 50));
            this.rowsProcessed++;
        }

        private int getRowsProcessed() {
            return this.rowsProcessed;
        }
    }
    public static void main(String[] args) {
        SpringApplication.run(JdbcTemplateJavaStream.class, args);
    }

    public JdbcTemplateJavaStream(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        final Instant start = Instant.now();

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

        final RowMapper<String> rowMapper = (rs, i) -> rs.getString("text_val");
        final CountingConsumer countingConsumer = new CountingConsumer();
        jdbcTemplate.queryForStream("select text_val from pc_load_letter.source", rowMapper)
                .forEach(countingConsumer);

        final Instant end = Instant.now();
        logger.info(String.format("%,d rows processed in %,d milliseconds.", countingConsumer.getRowsProcessed(),
                end.toEpochMilli() - start.toEpochMilli()));
    }
}
