package com.wiremock.service;

import com.wiremock.Movie;
import com.wiremock.constants.MoviesAppConstants;
import com.wiremock.exception.MovieErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class MoviesRestClient {

    private static final Logger log = LoggerFactory.getLogger(MoviesRestClient.class);
    private final WebClient webClient;

    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies() {
        try {
            return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMovieById: {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retriveMovieById: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie retrieveMovieById(int movieId) {
        try {
            return webClient.get().uri(MoviesAppConstants.GET_MOVIE_BY_ID, movieId)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMovieById: {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retriveMovieById: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> retrieveMovieByName(String name) {
        try {
            String endpoint = UriComponentsBuilder.fromUriString(MoviesAppConstants.GET_MOVIE_BY_NAME)
                    .queryParam("movie_name", name)
                    .buildAndExpand()
                    .toUriString();

            return webClient.get().uri(endpoint)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMovieByName: {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retriveMovieByName: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> retrieveMovieByYear(int year) {
        String endpoint = UriComponentsBuilder.fromUriString(MoviesAppConstants.GET_MOVIE_BY_YEAR)
                .queryParam("year", year)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get().uri(endpoint)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retriveMovieByYear: {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retriveMovieByYear: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie addMovie(Movie movie) {
        try {
            return webClient.post().uri(MoviesAppConstants.POST_MOVIE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .syncBody(movie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in POST Movie {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in POST Movie: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie updateMovie(int movieId, Movie movie) {
        try {
            return webClient.put().uri(MoviesAppConstants.GET_MOVIE_BY_ID, movieId)
                    .syncBody(movie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in PUT Movie {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in PUT Movie: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public String deleteMovie(int movieId) {
        try {
            return webClient.delete().uri(MoviesAppConstants.GET_MOVIE_BY_ID, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in DELETE Movie {}, Status code: {}", ex.getResponseBodyAsString(), ex.getRawStatusCode());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in DELETE Movie: ", ex);
            throw new MovieErrorResponse(ex);
        }
    }
}
