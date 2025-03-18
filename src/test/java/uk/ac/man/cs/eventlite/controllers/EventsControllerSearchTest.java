package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerSearchTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    @Test
    public void testSearchWithResults() throws Exception {
        // Create test events
        Event event1 = new Event();
        event1.setId(1);
        event1.setName("Spring Workshop");
        event1.setDate(LocalDate.now().plusDays(1));
        event1.setTime(LocalTime.of(10, 0));
        
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Summer Conference");
        event2.setDate(LocalDate.now().plusDays(2));
        event2.setTime(LocalTime.of(14, 0));
        
        // Create a test venue
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        
        // Set venues for events
        event1.setVenue(venue);
        event2.setVenue(venue);
        
        // Mock data to return
        List<Event> allEvents = Arrays.asList(event1, event2);
        List<Event> filteredEvents = Arrays.asList(event1);
        
        when(eventService.findAll()).thenReturn(allEvents);
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        
        mvc.perform(get("/events/search").param("query", "spring")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("events/index"))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(model().attribute("events", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Spring Workshop"))
                        )
                )));
    }

    @Test
    public void testSearchWithNoResults() throws Exception {
        // Create test events
        Event event1 = new Event();
        event1.setId(1);
        event1.setName("Spring Workshop");
        event1.setDate(LocalDate.now().plusDays(1));
        event1.setTime(LocalTime.of(10, 0));
        
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Summer Conference");
        event2.setDate(LocalDate.now().plusDays(2));
        event2.setTime(LocalTime.of(14, 0));
        
        // Create a test venue
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        
        // Set venues for events
        event1.setVenue(venue);
        event2.setVenue(venue);
        
        // Mock data to return
        List<Event> allEvents = Arrays.asList(event1, event2);
        
        when(eventService.findAll()).thenReturn(allEvents);
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        
        mvc.perform(get("/events/search").param("query", "nonexistent")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("events/index"))
                .andExpect(model().attribute("events", hasSize(0)));
    }

    @Test
    public void testSearchCaseInsensitive() throws Exception {
        // Create test events
        Event event1 = new Event();
        event1.setId(1);
        event1.setName("Spring Workshop");
        event1.setDate(LocalDate.now().plusDays(1));
        event1.setTime(LocalTime.of(10, 0));
        
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Summer Conference");
        event2.setDate(LocalDate.now().plusDays(2));
        event2.setTime(LocalTime.of(14, 0));
        
        // Create a test venue
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        
        // Set venues for events
        event1.setVenue(venue);
        event2.setVenue(venue);
        
        // Mock data to return
        List<Event> allEvents = Arrays.asList(event1, event2);
        
        when(eventService.findAll()).thenReturn(allEvents);
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        
        mvc.perform(get("/events/search").param("query", "SPRING")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("events/index"))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(model().attribute("events", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Spring Workshop"))
                        )
                )));
    }
    
    @Test
    public void testSearchPartialMatch() throws Exception {
        // Create test events
        Event event1 = new Event();
        event1.setId(1);
        event1.setName("Spring Workshop");
        event1.setDate(LocalDate.now().plusDays(1));
        event1.setTime(LocalTime.of(10, 0));
        
        Event event2 = new Event();
        event2.setId(2);
        event2.setName("Summer Conference");
        event2.setDate(LocalDate.now().plusDays(2));
        event2.setTime(LocalTime.of(14, 0));
        
        // Create a test venue
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        
        // Set venues for events
        event1.setVenue(venue);
        event2.setVenue(venue);
        
        // Mock data to return
        List<Event> allEvents = Arrays.asList(event1, event2);
        
        when(eventService.findAll()).thenReturn(allEvents);
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        
        mvc.perform(get("/events/search").param("query", "spr")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("events/index"))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(model().attribute("events", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Spring Workshop"))
                        )
                )));
    }
}