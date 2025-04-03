package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Security;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest
@ActiveProfiles("default")
@Import(Security.class)
@AutoConfigureMockMvc
public class RestApiTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    // Test the Homepage JSON: GET /api
    @Test
    public void testHomepageJson() throws Exception {
        mvc.perform(get("/api").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("_links.venues.href", startsWith("http://localhost:8080/api/venues")))
                .andExpect(jsonPath("_links.events.href", startsWith("http://localhost:8080/api/events")))
                .andExpect(jsonPath("_links.profile.href", startsWith("http://localhost:8080/api/profile")));
    }

    // Test Events List JSON: GET /api/events
    @Test
    public void testEventsListJson() throws Exception {
        Venue venue = new Venue();
        venue.setId(111);
        venue.setName("Test Venue");

        Event event = new Event();
        event.setId(111);
        event.setName("Test Event");
        event.setDate(LocalDate.of(2025, 12, 25));
        event.setTime(LocalTime.of(10, 30));
        event.setDescription("Test description");
        event.setVenue(venue);

        when(eventService.findAll()).thenReturn(Arrays.asList(event));

        mvc.perform(get("/api/events").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("_embedded.events", hasSize(1)))
                .andExpect(jsonPath("_embedded.events[0].name", is("Test Event")))
                .andExpect(jsonPath("_links.self.href", startsWith("http://localhost:8080/api/events")));
    }

    // Test Single Event JSON: GET /api/events/1
    @Test
    public void testSingleEventJson() throws Exception {
        Venue venue = new Venue();
        venue.setId(111);
        venue.setName("Test Venue");

        Event event = new Event();
        event.setId(111);
        event.setName("Test Event");
        event.setDate(LocalDate.of(2025, 12, 25));
        event.setTime(LocalTime.of(10, 30));
        event.setDescription("Test description");
        event.setVenue(venue);

        when(eventService.findById(1L)).thenReturn(Optional.of(event));

        mvc.perform(get("/api/events/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Event")))
                .andExpect(jsonPath("date", is("2025-12-25")))
                .andExpect(jsonPath("time", is("10:30:00")))
                .andExpect(jsonPath("_links.self.href", startsWith("http://localhost:8080/api/events/1")))
                .andExpect(jsonPath("_links.event.href", startsWith("http://localhost:8080/api/events/1")))
                .andExpect(jsonPath("_links.venue.href", startsWith("http://localhost:8080/api/events/1/venue")));
    }

    // Test Venues List JSON: GET /api/venues
    @Test
    public void testVenuesListJson() throws Exception {
        Venue venue = new Venue();
        venue.setId(111);
        venue.setName("Test Venue");
        venue.setCapacity(500);

        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));

        mvc.perform(get("/api/venues").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("_embedded.venues", hasSize(1)))
                .andExpect(jsonPath("_embedded.venues[0].name", is("Test Venue")))
                .andExpect(jsonPath("_links.self.href", startsWith("http://localhost:8080/api/venues")))
                .andExpect(jsonPath("_links.profile.href", startsWith("http://localhost:8080/api/profile/venues")));
    }

    // Test Single Venue JSON: GET /api/venues/1
    @Test
    public void testSingleVenueJson() throws Exception {
        Venue venue = new Venue();
        venue.setId(111);
        venue.setName("Test Venue");
        venue.setCapacity(500);

        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(get("/api/venues/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name", is("Test Venue")))
                .andExpect(jsonPath("capacity", is(500)))
                .andExpect(jsonPath("_links.self.href", startsWith("http://localhost:8080/api/venues/1")))
                .andExpect(jsonPath("_links.venue.href", startsWith("http://localhost:8080/api/venues/1")))
                .andExpect(jsonPath("_links.events.href", startsWith("http://localhost:8080/api/venues/1/events")))
                .andExpect(jsonPath("_links.next3events.href",
                        startsWith("http://localhost:8080/api/venues/1/next3events")));
    }
}