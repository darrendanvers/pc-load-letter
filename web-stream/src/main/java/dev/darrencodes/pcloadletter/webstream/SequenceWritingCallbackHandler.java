package dev.darrencodes.pcloadletter.webstream;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.SequenceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

/**
 * RowCallbackHandler that will construct an object from each row in a ResultSet and write it
 * to a Jackson SequenceWriter. Clients of this class hold the responsibility of closing the SequenceWriter
 * after all objects have been written.
 *
 * @param <T> The type of object to write to the SequenceWriter.
 */
/* default */ final class SequenceWritingCallbackHandler<T> implements RowCallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(SequenceWritingCallbackHandler.class);

    private final SequenceWriter sequenceWriter;
    private final RowMapper<T> rowMapper;

    private int rowCount = 0;

    /**
     * Constructs a new HttpStreamCallbackHandler.
     *
     * @param sequenceWriter The Jackson SequenceWriter to write objects to.
     * @param rowMapper The RowMapper to use to construct objects from each row in the ResultSet.
     */
    /* default */ SequenceWritingCallbackHandler(final SequenceWriter sequenceWriter, final RowMapper<T> rowMapper) {
        this.sequenceWriter = sequenceWriter;
        this.rowMapper = rowMapper;
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {

        try {
            this.sequenceWriter.write(this.rowMapper.mapRow(rs, this.rowCount));
            this.rowCount++;
            if (this.rowCount % 1_000 == 0) {
                logger.info(String.format("Streamed %,d records", this.rowCount));
            }
        } catch (IOException e) {
            throw new SQLException("Unable to marshall object", e);
        }
    }

    /**
     * Returns the number of rows processed so far.
     *
     * @return The number of rows processed so far.
     */
    /* default */ int getRowCount() {
        return this.rowCount;
    }
}
