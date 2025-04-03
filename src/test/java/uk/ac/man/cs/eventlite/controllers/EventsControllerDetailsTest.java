package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

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
public class EventsControllerDetailsTest {

    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private EventService eventService;
    
    @MockBean
    private VenueService venueService;
    
    @Test
    public void testGetEventDetailsFound() throws Exception {
        long eventId = 1L;
        long venueId = 10L;
        
        Venue venue = new Venue();
        venue.setId(venueId);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");
        venue.setCapacity(100);
        
        Event event = new Event();
        event.setId(eventId);
        event.setName("Test Event");
        event.setDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(12, 0));
        event.setVenue(venue);
        
        when(eventService.findAll()).thenReturn(Arrays.asList(event));
        when(venueService.findById(venueId)).thenReturn(venue); // Mock findById
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        
        mvc.perform(get("/events/{id}/details", eventId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("events/event_details"))
           .andExpect(model().attribute("event", hasProperty("id", is(eventId))))
           .andExpect(model().attribute("venue", is(venue)))
           .andExpect(model().attribute("venues", hasItem(venue)));
        
        verify(eventService).findAll();
        verify(venueService).findById(venueId); 
        verify(venueService).findAll();
    }
    
    @Test
    public void testGetEventDetailsNotFound() throws Exception {
        long eventId = 999L;
        
        when(eventService.findAll()).thenReturn(Collections.emptyList());
        
        mvc.perform(get("/events/{id}/details", eventId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isNotFound());
        
        verify(eventService).findAll();
        verifyNoInteractions(venueService);
    }
    
    @Test
    public void testGetEventDetailsWithInvalidIdFormat() throws Exception {
        mvc.perform(get("/events/abc/details")
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
        verifyNoInteractions(venueService);
    }
    
    @Test
    public void testGetEventDetailsWithNoVenue() throws Exception {
        long eventId = 3L;

        Event event = new Event();
        event.setId(eventId);
        event.setName("Event Without Venue");
        event.setDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(12, 0));
        event.setVenue(null); // No venue

        Venue venue = new Venue();
        venue.setId(10L);
        venue.setName("Test Venue");

        when(eventService.findAll()).thenReturn(Arrays.asList(event));
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));

        mvc.perform(get("/events/{id}/details", eventId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("events/event_details"))
           .andExpect(model().attribute("event", hasProperty("id", is(eventId))))
           .andExpect(model().attribute("event", hasProperty("venue", nullValue())))
           .andExpect(model().attribute("venue", nullValue()))
           .andExpect(model().attribute("venues", hasItem(venue)));

        verify(eventService).findAll();
        verify(venueService).findAll();
        verifyNoMoreInteractions(venueService); // findById not called since venue is null
    }
}
