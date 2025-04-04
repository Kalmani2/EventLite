package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerAddTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;
    
    @MockBean 
    private MastodonController mastodonController;
    
    private final String USERNAME = "Markel";
	private final String PASSWORD = "Vigo";

    @Test
    public void testAddEventSuccess() throws Exception {
        // Prepare a valid venue
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        // Assume venue exists:
        when(venueService.findById(1L)).thenReturn(venue);

        // Prepare valid event data
        String eventName = "Test Event";
        String eventDate = "2025-12-25";
        String eventTime = "10:30";
        String description = "This is a test event";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setDescription(description);
        event.setVenue(venue);

        // Assume the service returns the event (with an auto-generated ID)
        when(eventService.addEvent(any(Event.class))).thenReturn(event);
        // Also assume findById works:
        when(eventService.findById(any(Long.class))).thenReturn(Optional.of(event));

        mvc.perform(post("/events")
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE)) // Authenticate
        		.with(csrf()) 
                .param("venueId", "1")
                .param("name", eventName)
                .param("date", eventDate)
                .param("time", eventTime)
                .param("description", description)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).addEvent(any(Event.class));
    }

    @Test
    public void testAddEventWithoutOptionalDescription() throws Exception {
        // Description is optional; test by not providing it.
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        String eventName = "Event Without Description";
        String eventDate = "2025-12-31";
        String eventTime = "09:00";

        Event event = new Event();
        event.setName(eventName);
        event.setDate(LocalDate.parse(eventDate));
        event.setTime(LocalTime.parse(eventTime));
        event.setVenue(venue);
        // Description remains null.
        when(eventService.addEvent(any(Event.class))).thenReturn(event);
        when(eventService.findById(any(Long.class))).thenReturn(Optional.of(event));

        mvc.perform(post("/events")
                .with(user(USERNAME).password(PASSWORD).roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("venueId", "1")
                .param("name", eventName)
                .param("date", eventDate)
                .param("time", eventTime)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).addEvent(any(Event.class));
    }

    @Test
    public void testAddEventWithNonexistentVenue() throws Exception {
        // Simulate that the provided venueId doesn't exist.
        when(venueService.findById(999L)).thenReturn(null);

        mvc.perform(post("/events")
                .param("venueId", "999")
                .param("name", "Event with Bad Venue")
                .param("date", "2025-12-31")
                .param("time", "09:00")
                .param("description", "Testing invalid venue")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        		.andExpect(status().is4xxClientError());
    }

    // Additional tests for missing or invalid data:

    @Test
    public void testAddEventMissingName() throws Exception {
        // Test missing required field 'name'
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(post("/events")
                .param("venueId", "1")
                // Missing 'name'
                .param("date", "2025-12-31")
                .param("time", "09:00")
                .param("description", "Missing name")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAddEventMissingDate() throws Exception {
        // Test missing required field 'date'
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(post("/events")
                .param("venueId", "1")
                .param("name", "Missing Date Event")
                // Missing 'date'
                .param("time", "09:00")
                .param("description", "Missing date")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAddEventMissingTime() throws Exception {
        // Test missing required field 'time'
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(post("/events")
                .param("venueId", "1")
                .param("name", "Missing Time Event")
                .param("date", "2025-12-31")
                // Missing 'time'
                .param("description", "Missing time")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAddEventInvalidDateFormat() throws Exception {
        // Test invalid date format
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(post("/events")
                .param("venueId", "1")
                .param("name", "Invalid Date Event")
                .param("date", "invalid-date") // invalid
                .param("time", "09:00")
                .param("description", "Invalid date format")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAddEventInvalidTimeFormat() throws Exception {
        // Test invalid time format
        Venue venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        when(venueService.findById(1L)).thenReturn(venue);

        mvc.perform(post("/events")
                .param("venueId", "1")
                .param("name", "Invalid Time Event")
                .param("date", "2025-12-31")
                .param("time", "invalid-time") // invalid
                .param("description", "Invalid time format")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is4xxClientError());
    }
}
