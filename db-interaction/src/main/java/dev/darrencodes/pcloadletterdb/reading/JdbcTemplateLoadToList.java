package dev.darrencodes.pcloadletterdb.reading;

import java.util.List;
import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Demonstrates trying to read and process a large ResultSet using the standard JdbcTemplate methods people tend
 * to go for. This will eventually abend.
 */
//@SpringBootApplication
public class JdbcTemplateLoadToList implements CommandLineRunner {

    private final DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(JdbcTemplateLoadToList.class, args);
    }

    public JdbcTemplateLoadToList(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

        final RowMapper<String> rowMapper = (rs, i) -> rs.getString("text_val");
        final List<String> allIds = jdbcTemplate.query("select text_val from pc_load_letter.source", rowMapper);
        allIds.forEach(s -> System.out.printf("%s\n", Util.abbreviate(s, 50)));
    }
}
