package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

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
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerDetailsTest {

    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private VenueService venueService;
    
    @MockBean
    private EventService eventService;
    
    @Test
    public void testGetVenueDetailsWithUpcomingEvents() throws Exception {
        long venueId = 1L;
        Venue venue = new Venue();
        venue.setId(venueId);
        venue.setName("Kilburn Building");
        venue.setAddress("Oxford Road, Manchester");
        venue.setCapacity(200);
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("Event 1");
        event1.setDate(today);
        event1.setTime(now.plusMinutes(5));
        event1.setVenue(venue);
        
        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("Event 2");
        event2.setDate(today);
        event2.setTime(now.plusMinutes(10));
        event2.setVenue(venue);
        
        Event event3 = new Event();
        event3.setId(3L);
        event3.setName("Event 3");
        event3.setDate(today.plusDays(1));
        event3.setTime(LocalTime.of(10, 0));
        event3.setVenue(venue);
        
        Event pastEvent = new Event();
        pastEvent.setId(4L);
        pastEvent.setName("Past Event");
        pastEvent.setDate(today.minusDays(1));
        pastEvent.setTime(LocalTime.of(15, 0));
        pastEvent.setVenue(venue);
        
        Venue otherVenue = new Venue();
        otherVenue.setId(2L);
        otherVenue.setName("Other Venue");
        otherVenue.setCapacity(300);
        otherVenue.setAddress("Somewhere");
        
        Event otherVenueEvent = new Event();
        otherVenueEvent.setId(5L);
        otherVenueEvent.setName("Other Venue Event");
        otherVenueEvent.setDate(today.plusDays(2));
        otherVenueEvent.setTime(LocalTime.of(12, 0));
        otherVenueEvent.setVenue(otherVenue);
        
        when(venueService.findById(venueId)).thenReturn(venue);
        when(eventService.findAll()).thenReturn(Arrays.asList(event1, event2, event3, pastEvent, otherVenueEvent));
        
        mvc.perform(get("/venues/{id}/details", venueId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("venues/venue_details"))
           .andExpect(model().attribute("venue", hasProperty("id", is(venueId))))
           .andExpect(model().attribute("upcomingEvents", contains(
                   allOf(hasProperty("id", is(1L))),
                   allOf(hasProperty("id", is(2L))),
                   allOf(hasProperty("id", is(3L)))
           )));
        
        verify(venueService).findById(venueId);
        verify(eventService).findAll();
    }
    
    @Test
    public void testGetVenueDetailsWithNoUpcomingEvents() throws Exception {
        long venueId = 1L;
        Venue venue = new Venue();
        venue.setId(venueId);
        venue.setName("Kilburn Building");
        venue.setAddress("Oxford Road, Manchester");
        venue.setCapacity(200);
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        Event pastEvent = new Event();
        pastEvent.setId(1L);
        pastEvent.setName("Past Event");
        pastEvent.setDate(today.minusDays(1));
        pastEvent.setTime(LocalTime.of(15, 0));
        pastEvent.setVenue(venue);
        
        Event todayPastEvent = new Event();
        todayPastEvent.setId(2L);
        todayPastEvent.setName("Earlier Today Event");
        todayPastEvent.setDate(today);
        todayPastEvent.setTime(now.minusMinutes(10));
        todayPastEvent.setVenue(venue);
        
        when(venueService.findById(venueId)).thenReturn(venue);
        when(eventService.findAll()).thenReturn(Arrays.asList(pastEvent, todayPastEvent));
        
        mvc.perform(get("/venues/{id}/details", venueId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("venues/venue_details"))
           .andExpect(model().attribute("venue", hasProperty("id", is(venueId))))
           .andExpect(model().attribute("upcomingEvents", hasSize(0)));
        
        verify(venueService).findById(venueId);
        verify(eventService).findAll();
    }
    
    @Test
    public void testGetVenueDetailsVenueNotFound() throws Exception {
        long venueId = 999L;
        when(venueService.findById(venueId)).thenReturn(null);
        
        mvc.perform(get("/venues/{id}/details", venueId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isNotFound());
        
        verify(venueService).findById(venueId);
        verifyNoInteractions(eventService);
    }
}
