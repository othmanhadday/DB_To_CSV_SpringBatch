package com.othmane.DbToCsv.processor;

import com.othmane.DbToCsv.entities.Film;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FilmProcessor implements ItemProcessor<Film, Film> {

    @Override
    public Film process(Film film) throws Exception {
        return film;
    }
}
