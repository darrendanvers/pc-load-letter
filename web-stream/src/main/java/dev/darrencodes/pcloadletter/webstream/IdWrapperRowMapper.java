package dev.darrencodes.pcloadletter.webstream;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper to construct IdWrappers from a row in a ResultSet.
 */
public class IdWrapperRowMapper implements RowMapper<IdWrapper> {

    @Override
    public IdWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new IdWrapper()
                .setId(rs.getLong("id"))
                .setText(rs.getString("text_val"));
    }
}
