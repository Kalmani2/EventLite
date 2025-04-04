package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

/**
 * REST API integration tests for events and venues.
 * 
 * This test class loads the full Spring Boot context with a RANDOM_PORT,
 * but excludes DataSource and Hibernate JPA auto‑configuration to avoid persistence
 * issues. The services (EventService and VenueService) are mocked so that
 * the REST controllers work without a real database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class RestApiTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    // ----------------------- API Home -----------------------

    @Test
    public void testApiHome() {
        client.get().uri("/api")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("_links.events.href").value(endsWith("/api/events"))
            .jsonPath("_links.venues.href").value(endsWith("/api/venues"))
            .jsonPath("_links.profile.href").value(endsWith("/api/profile"));
    }

    // ----------------------- Events Tests -----------------------

    @Test
    public void testGetEventsList() {
        // Create dummy venue and event.
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");

        Event event = new Event();
        event.setId(1);
        event.setName("Test Event");
        event.setDate(LocalDate.of(2025, 12, 25));
        event.setTime(LocalTime.of(10, 30));
        event.setDescription("Test description");
        event.setVenue(venue);

        when(eventService.findAll()).thenReturn(Arrays.asList(event));

        client.get().uri("/api/events")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("_embedded.events").value(hasSize(1))
            .jsonPath("_embedded.events[0].name").value(is("Test Event"))
            .jsonPath("_links.self.href").value(endsWith("/api/events"));
    }

    @Test
    public void testGetSingleEvent() {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");

        Event event = new Event();
        event.setId(1);
        event.setName("Test Event");
        event.setDate(LocalDate.of(2025, 12, 25));
        event.setTime(LocalTime.of(10, 30));
        event.setDescription("Test description");
        event.setVenue(venue);

        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        client.get().uri("/api/events/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("name").value(is("Test Event"))
            .jsonPath("date").value(is("2025-12-25"))
            .jsonPath("time").value(is("10:30:00"))
            .jsonPath("_links.self.href").value(endsWith("/api/events/1"))
            .jsonPath("_links.venue.href").value(endsWith("/api/events/1/venue"));
    }

    // ----------------------- Venues Tests -----------------------

    @Test
    public void testGetVenuesList() {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");
        venue.setCapacity(500);

        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

        client.get().uri("/api/venues")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("_embedded.venues").value(hasSize(1))
            .jsonPath("_embedded.venues[0].name").value(is("Test Venue"))
            .jsonPath("_links.self.href").value(endsWith("/api/venues"));
    }

    @Test
    public void testGetSingleVenue() {
        Venue venue = new Venue();
        venue.setId(10);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");
        venue.setCapacity(500);

        when(venueService.findById(1L)).thenReturn(venue);

        client.get().uri("/api/venues/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("name").value(is("Test Venue"))
            .jsonPath("capacity").value(is(500))
            .jsonPath("_links.self.href").value(endsWith("/api/venues/1"))
            .jsonPath("_links.venue.href").value(endsWith("/api/venues/1"))
            .jsonPath("_links.events.href").value(endsWith("/api/venues/1/events"))
            .jsonPath("_links.next3events.href").value(endsWith("/api/venues/1/next3events"));
    }
}