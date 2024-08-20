package com.wiremock;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    public int movie_id;
    public String cast;
    public String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate release_date;
    public int year;

    public Movie(String cast, int year) {
        this.cast = cast;
        this.year = year;
    }
}