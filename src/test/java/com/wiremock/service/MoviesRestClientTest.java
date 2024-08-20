package com.wiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.wiremock.Movie;
import com.wiremock.constants.MoviesAppConstants;
import com.wiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {
    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig().port(8088).notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl : " + baseUrl);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));
    }

    @Test
    void deleteMovie() {
        //Set<Movie> moviesBeforeDelete = new HashSet<>(moviesRestClient.retrieveAllMovies());
        //Movie movieToDelete = moviesBeforeDelete.iterator().next();

        LocalDate date = LocalDate.parse("2024-06-18");
        Movie newMovie = new Movie(0, "Christian Bale 1, Heath Ledger 2 , Michael Caine 3", "New Movie 2024 3", date, 2024);
        stubFor(post(urlEqualTo(MoviesAppConstants.POST_MOVIE))
                .withRequestBody(matchingJsonPath("$.name", equalTo(newMovie.getName())))
                .withRequestBody(matchingJsonPath("$.year", equalTo(String.valueOf(newMovie.getYear()))))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie.json")));

        Movie addedMovie = moviesRestClient.addMovie(newMovie);

        stubFor(delete(urlEqualTo(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + addedMovie.getMovie_id()))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody("Movie Deleted Successfully")));

        String confirmDeleteMessage = moviesRestClient.deleteMovie(addedMovie.getMovie_id());
        //Set<Movie> moviesAfterDelete = new HashSet<>(moviesRestClient.retrieveAllMovies());

        //assertEquals(moviesBeforeDelete.size() - 1, moviesAfterDelete.size());
        //moviesBeforeDelete.remove(movieToDelete);
        //assertEquals(moviesBeforeDelete, moviesAfterDelete);
        assertEquals("Movie Deleted Successfully", confirmDeleteMessage);

        // VERIFY EXAMPLE:
        verify(exactly(1), postRequestedFor(urlEqualTo(MoviesAppConstants.POST_MOVIE))
                .withRequestBody(matchingJsonPath("$.name", equalTo(newMovie.getName())))
                .withRequestBody(matchingJsonPath("$.year", equalTo(String.valueOf(newMovie.getYear())))));

        verify(exactly(1), deleteRequestedFor(urlEqualTo(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + addedMovie.getMovie_id())));
    }

    @Test
    void deleteMovie_NotFound() {
//        Set<Movie> moviesBeforeDelete = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        int notExistedID = moviesBeforeDelete.stream().mapToInt(Movie::getMovie_id).max().getAsInt() + 1;

        stubFor(delete(urlPathMatching(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + "[0-9]+"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBody("No movie found for the year that’s passed.")));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(1));

//        Set<Movie> moviesAfterDelete = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        assertEquals(moviesBeforeDelete, moviesAfterDelete);
    }

    @Test
    void testUpdateMovie() {
//        Set<Movie> moviesBeforeUpdate = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        Movie selectedMovie = moviesBeforeUpdate.iterator().next();
//        System.out.println("Selected movie: " + selectedMovie);
        //Movie dataToUpdate = new Movie("Updated Cast data", selectedMovie.getYear());
        LocalDate date = LocalDate.parse("2021-06-09");
        Movie movie_3 = new Movie(3, "Christian Bale, Heath Ledger , Michael Caine", "The Dark Knight Rises", date, date.getYear());
        Movie movie_3_updated = new Movie("Updated Cast", movie_3.getYear());
        stubFor(put(urlPathMatching(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + "[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(movie_3_updated.getCast())))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("updated_movie.json")));

        Movie movieAfterStub = moviesRestClient.updateMovie(movie_3.getMovie_id(), movie_3_updated);
        assertEquals(movieAfterStub.getCast(), movie_3.getCast() + " " + movie_3_updated.getCast());
        //Movie movieAfterUpdate = moviesRestClient.updateMovie(selectedMovie.getMovie_id(), dataToUpdate);

//        Set<Movie> moviesAfterUpdate = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        String existedCast = selectedMovie.getCast();
//        selectedMovie.setCast(existedCast + ", Updated Cast data");
//        System.out.println("Selected movie with corrected cast: " + selectedMovie);

//        // object from response:
//        assertEquals(selectedMovie, movieAfterUpdate);
//        // object from list of all:
//        assertEquals(selectedMovie,
//                moviesAfterUpdate.stream().filter(m -> m.getMovie_id() == selectedMovie.getMovie_id()).collect(Collectors.toSet()).iterator().next());
    }

    @Test
    void testUpdateMovie_NotFound() {
//        Set<Movie> moviesBeforeUpdate = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        Movie dataToUpdate = new Movie("Updated Cast data", 2024);
//        int maxId = moviesBeforeUpdate.stream().mapToInt(m -> m.getMovie_id()).max().getAsInt();
        Movie movie_3_updated = new Movie("Updated Cast", 2021);
        stubFor(put(urlPathMatching(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + "[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(movie_3_updated.getCast())))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movie_3_updated.getMovie_id(), movie_3_updated));

        //Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(maxId + 1, dataToUpdate));

//        Set<Movie> moviesAfterInvalidUpdate = new HashSet<>(moviesRestClient.retrieveAllMovies());
//        assertEquals(moviesBeforeUpdate, moviesAfterInvalidUpdate);
    }

    @Test
    void testAddNewMovie() {
        //int moviesCountBeforeTest = moviesRestClient.retrieveAllMovies().size();
        LocalDate date = LocalDate.parse("2024-06-18");
        Movie newMovie = new Movie(0, "Christian Bale 1, Heath Ledger 2 , Michael Caine 3", "New Movie 2024 3", date, 2024);
        stubFor(post(urlEqualTo(MoviesAppConstants.POST_MOVIE))
                .withRequestBody(matchingJsonPath("$.name", equalTo(newMovie.getName())))
                .withRequestBody(matchingJsonPath("$.year", equalTo(String.valueOf(newMovie.getYear()))))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add_movie.json")));

        Movie addedMovie = moviesRestClient.addMovie(newMovie);

        addedMovie.setMovie_id(0);
        //assertEquals(moviesCountBeforeTest + 1, moviesCountAfterTest);
        assertEquals(newMovie, addedMovie);
    }

    @Test
    void testAddNewMovieWithoutName_BadRequest() {
//        List<Movie> moviesBeforeAdd = moviesRestClient.retrieveAllMovies();
        LocalDate date = LocalDate.parse("2024-06-18");
        Movie newMovie = new Movie(0, "Christian Bale, Heath Ledger , Michael Caine", null, date, 2024);
        stubFor(post(urlEqualTo(MoviesAppConstants.POST_MOVIE)).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withBodyFile("400_invalid_input.json")));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));

//        List<Movie> moviesAfterAdd = moviesRestClient.retrieveAllMovies();
//        assertEquals(moviesBeforeAdd.size(), moviesAfterAdd.size());
//        assertEquals(moviesBeforeAdd, moviesAfterAdd);
    }

    @Test
    void testRetrieveAllMovies() {
        //given
        stubFor(get(urlEqualTo(MoviesAppConstants.GET_ALL_MOVIES))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBodyFile("all-movies.json")));

        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("Movies: " + movieList);
        assertFalse(movieList.isEmpty());
    }

    @Test
    void testRetrieveMovieById() {
        LocalDate date = LocalDate.parse("2008-07-18");
        Movie movieExpected = new Movie(2, "Christian Bale, Heath Ledger , Michael Caine", "Dark Knight", date, 2008);
        String cleared_GET_MOVIE_BY_ID = MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "") + "[0-9]";
        stubFor(get(urlPathMatching(cleared_GET_MOVIE_BY_ID))
                //stubFor(get(urlPathMatching(MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "")  + movieExpected.getMovie_id()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie_2_dynamic_response_templat.json")));

        Movie movieActual = moviesRestClient.retrieveMovieById(movieExpected.getMovie_id());
        assertEquals(movieExpected, movieActual);
    }

    @Test
    void testRetrieveMovieById_NotFound() {
        // finding the greatest id:
        stubFor(get(urlEqualTo(MoviesAppConstants.GET_ALL_MOVIES))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        int maxId = moviesRestClient.retrieveAllMovies().stream().mapToInt(Movie::getMovie_id).max().getAsInt();
        // sending request with not existed id:
        String endpoint = MoviesAppConstants.GET_MOVIE_BY_ID.replaceAll("\\{movieId\\}", "");
        stubFor(get(urlEqualTo(endpoint + (maxId + 1))).willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withBodyFile("404_movieId.json")));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById((maxId + 1)));
    }

    @Test
    void testRetrieveMovieByName() {
        String desirableMovieName = "White";
        LocalDate date = LocalDate.parse("2008-07-18");
        Movie movieExpected = new Movie(2, "Christian Bale, Heath Ledger , Michael Caine", desirableMovieName + " Knight", date, 2008);

        //stubFor(get(urlEqualTo(MoviesAppConstants.GET_MOVIE_BY_NAME + "?movie_name=" + "Dark"))
        stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_MOVIE_BY_NAME)).withQueryParam("movie_name", equalTo(desirableMovieName))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        //.withBodyFile("movies_by_name_Dark.json")));
                        .withBodyFile("movie_by_name_template.json")));
        List<Movie> movieActual = moviesRestClient.retrieveMovieByName(desirableMovieName);

        assertTrue(movieActual.stream().allMatch(item -> item.getName().contains(desirableMovieName)));
        assertEquals(2, movieActual.size());
    }

    @Test
    void testRetrieveMovieByName_NotFound() {
        String notExistedMovieName = "White";
        stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_MOVIE_BY_NAME)).withQueryParam("movie_name", equalTo(notExistedMovieName))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBodyFile("404_movies_by_name.json")));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(notExistedMovieName));
    }

    @Test
    void testRetrieveMovieByYear() {
        String desirableYear = "2012";
        stubFor(get(urlEqualTo(MoviesAppConstants.GET_MOVIE_BY_YEAR + "?year=" + desirableYear))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .withBodyFile("movies_by_year_response.json")));

        List<Movie> moviesByYearActual = moviesRestClient.retrieveMovieByYear(Integer.parseInt(desirableYear));

        assertTrue(moviesByYearActual.stream().allMatch(item -> item.getYear() == (Integer.parseInt(desirableYear))));
        assertEquals(2, moviesByYearActual.size());
    }

    @Test
    void testRetrieveMoviesByYear_NotFound() {
        String desirableYear = "2013";
        stubFor(get(urlEqualTo(MoviesAppConstants.GET_MOVIE_BY_YEAR + "?year=" + desirableYear))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404_movies_by_year.json")));

        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByYear(Integer.parseInt(desirableYear)));
    }
}
