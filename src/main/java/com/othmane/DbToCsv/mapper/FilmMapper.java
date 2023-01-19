package com.othmane.DbToCsv.mapper;

import com.othmane.DbToCsv.entities.Film;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setTitle(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setLast_update(rs.getDate("last_update"));
        film.setLength(rs.getInt("length"));
        film.setRating(rs.getString("rating"));
        film.setRelease_year(rs.getInt("release_year"));
        film.setRental_rate(rs.getDouble("rental_rate"));
        film.setReplacement_cost(rs.getDouble("replacement_cost"));
        film.setSpecial_features(rs.getString("special_features"));

        return film;
    }
}
