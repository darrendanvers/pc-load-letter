package dev.darrencodes.pcloadletter.webstream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This REST endpoint demonstrates streaming a large result set from the database to the HTTP response directly.
 */
@RestController
public class DataStreamEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DataStreamEndpoint.class);

    private final DataSource dataSource;

    /**
     * Constructs a new DataStreamEndpoint.
     *
     * @param dataSource The DataSource to use to run queries.
     */
    public DataStreamEndpoint(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * GET endpoint. It will stream all records in the database.
     *
     * @param format An optional format to return the values as. Valid values are "json" or "csv". If not
     *               set or any other value is set, will return JSON.
     * @param response The HttpServletResponse to write the data to.
     * @throws IOException Any error will be propagated.
     */
    @GetMapping
    public void streamIds(@RequestParam(name = "format", defaultValue = "json") final String format, final HttpServletResponse response) throws IOException {

        if (Objects.equals(format, "csv")) {
            this.doCsvStream(response.getOutputStream());
        } else {
            this.doJsonStream(response.getOutputStream());
        }
    }

    /**
     * Streams all records in a JSON array to the response.
     *
     * @param outputStream The OutputStream to write the JSON array to.
     * @throws IOException Any error will be propagated.
     */
    private void doJsonStream(final OutputStream outputStream) throws IOException {

        // These objects will allow us to take a ResultSet, marshall each row to an object, use Jackson to
        // convert that object to JSON, and allow Jackson to write that JSON to the HTTP response.
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectWriter objectWriter =  objectMapper.writerFor(IdWrapper.class);
        this.doDataStream(objectWriter, outputStream);
    }

    /**
     * Streams all records in a CSV format to the response.
     *
     * @param outputStream The OutputStream to write the CSV to.
     * @throws IOException Any error will be propagated.
     */
    private void doCsvStream(final OutputStream outputStream) throws IOException {

        final CsvMapper mapper = new CsvMapper();
        final CsvSchema csvSchema = mapper.schemaFor(IdWrapper.class)
                .withHeader();
        final ObjectWriter objectWriter = mapper.writer(csvSchema);
        this.doDataStream(objectWriter, outputStream);
    }

    /**
     * Does the actual work of streaming the data to the response.
     *
     * @param objectWriter The ObjectWriter to write the data with.
     * @param outputStream The OutputStream for the ObjectWriter to write the data to.
     * @throws IOException Any error will be propagated.
     */
    private void doDataStream(final ObjectWriter objectWriter, final OutputStream outputStream) throws IOException {

        try (SequenceWriter sequenceWriter = objectWriter.writeValuesAsArray(outputStream)) {

            final SequenceWritingCallbackHandler<IdWrapper> sequenceWritingCallbackHandler =
                    new SequenceWritingCallbackHandler<>(sequenceWriter, new IdWrapperRowMapper());
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

            // This will do the actual work of running the query and writing all the records to the HTTP response.
            jdbcTemplate.query("select id, text_val from pc_load_letter.source", sequenceWritingCallbackHandler);

            logger.info(String.format("Streamed a total of %,d records.", sequenceWritingCallbackHandler.getRowCount()));
        }
    }
}
