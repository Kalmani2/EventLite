package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class VenuesControllerApiIntegrationTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService;

    @Test
    public void testGetAllVenues() {
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Venue 1");

        Venue venue2 = new Venue();
        venue2.setId(2);
        venue2.setName("Venue 2");

        List<Venue> venues = Arrays.asList(venue1, venue2);
        when(venueService.findAll()).thenReturn(venues);

        client.get().uri("/api/venues")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$._embedded.venues[0].name").isEqualTo("Venue 1")
                .jsonPath("$._embedded.venues[0]._links.self.href").value(endsWith("/api/venues/1"))
                .jsonPath("$._embedded.venues[1].name").isEqualTo("Venue 2")
                .jsonPath("$._embedded.venues[1]._links.self.href").value(endsWith("/api/venues/2"))
                .jsonPath("$._links.self.href").value(endsWith("/api/venues"));
    }

    @Test
    public void testGetVenue() {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Venue 1");

        when(venueService.findById(1L)).thenReturn(venue);

        client.get().uri("/api/venues/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("Venue 1")
                .jsonPath("$._links.self.href").value(endsWith("/api/venues/1"))
                .jsonPath("$._links.venue.href").value(endsWith("/api/venues/1"));
    }
    
    @Test
    public void testGetVenueEvents() {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");

        Event event1 = new Event();
        event1.setId(1);
        event1.setName("Event One");
        event1.setVenue(venue);

        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Event Two");
        event2.setVenue(venue);

        List<Event> events = Arrays.asList(event1, event2);
        when(venueService.findById(1L)).thenReturn(venue);
        when(eventService.findAll()).thenReturn(events);

        client.get().uri("/api/venues/1/events")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$._embedded.events[0].name").isEqualTo("Event One")
                .jsonPath("$._embedded.events[0]._links.self.href").value(endsWith("/api/events/1"))
                .jsonPath("$._embedded.events[1].name").isEqualTo("Event Two")
                .jsonPath("$._embedded.events[1]._links.self.href").value(endsWith("/api/events/2"))
                .jsonPath("$._links.self.href").value(endsWith("/api/venues/1/events"));
    }

    @Test
    public void testGetVenueNext3Events() {
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Event pastEvent = new Event();
        pastEvent.setId(1);
        pastEvent.setName("Past Event");
        pastEvent.setDate(today.minusDays(1));
        pastEvent.setTime(now);
        pastEvent.setVenue(venue);

        Event futureEvent1 = new Event();
        futureEvent1.setId(2);
        futureEvent1.setName("Future Event 1");
        futureEvent1.setDate(today.plusDays(1));
        futureEvent1.setTime(now);
        futureEvent1.setVenue(venue);

        Event futureEvent2 = new Event();
        futureEvent2.setId(3);
        futureEvent2.setName("Future Event 2");
        futureEvent2.setDate(today.plusDays(2));
        futureEvent2.setTime(now);
        futureEvent2.setVenue(venue);

        Event futureEvent3 = new Event();
        futureEvent3.setId(4);
        futureEvent3.setName("Future Event 3");
        futureEvent3.setDate(today.plusDays(3));
        futureEvent3.setTime(now);
        futureEvent3.setVenue(venue);

        List<Event> events = Arrays.asList(pastEvent, futureEvent1, futureEvent2, futureEvent3);
        when(venueService.findById(1L)).thenReturn(venue);
        when(eventService.findAll()).thenReturn(events);

        client.get().uri("/api/venues/1/next3events")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$._embedded.events.length()").isEqualTo(3)
                .jsonPath("$._embedded.events[0].name").isEqualTo("Future Event 1")
                .jsonPath("$._embedded.events[1].name").isEqualTo("Future Event 2")
                .jsonPath("$._embedded.events[2].name").isEqualTo("Future Event 3")
                .jsonPath("$._links.self.href").value(endsWith("/api/venues/1/next3events"));
    }

}