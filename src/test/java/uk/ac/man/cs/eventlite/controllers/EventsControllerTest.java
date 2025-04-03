package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.controllers.EventsController;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.controllers.MastodonController;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;
	
	@MockBean
	private MastodonController mastodonController;

	@Test
	public void getEventValid() throws Exception {
		
	    Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);

	    Event event = new Event();
	    event.setId(1L);
	    event.setName("Test Event");
	    event.setDate(LocalDate.parse("2025-12-25"));
	    event.setTime(LocalTime.parse("10:30"));
	    event.setVenue(venue);
	    
	    when(eventService.findById(1L)).thenReturn(Optional.of(event));

	    mvc.perform(get("/events/{id}", 1L)
	            .with(csrf()))  
	        .andExpect(status().isOk())  
	        .andExpect(view().name("events/event_details")) 
	        .andExpect(handler().methodName("getEvent"))  
	        .andExpect(model().attributeExists("event"))  
	        .andExpect(model().attributeExists("venue"))  
	        .andExpect(model().attribute("event", event)) 
	        .andExpect(model().attribute("venue", venue)); 

	    verify(eventService).findById(1L);
	}
	
	@Test
	public void getEventInvalid() throws Exception {
	    when(eventService.findById(1L)).thenReturn(Optional.empty());

	    mvc.perform(get("/events/{id}", 1L))
	        .andExpect(status().isNotFound())
	        .andExpect(view().name("events/not_found"))
	        .andExpect(handler().methodName("getEvent"));

	    verify(eventService).findById(1L);
	}
	
	@Test
	public void getAllEventsValid() throws Exception {
		List<Event> events = Arrays.asList(new Event(), new Event());
		List<Venue> venues = Arrays.asList(new Venue(), new Venue());
		
		when(eventService.findAll()).thenReturn(events);
		when(venueService.findAll()).thenReturn(venues);
		when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
		
		mvc.perform(get("/events").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk()) 
        .andExpect(view().name("events/index"))
        .andExpect(model().attributeExists("events")) 
        .andExpect(model().attributeExists("venues")) 
        .andExpect(model().attribute("events", events)) 
        .andExpect(model().attribute("venues", venues));

		verify(eventService).findAll();
		verify(venueService).findAll();
		verify(mastodonController).getTimelinePosts();
	}
	
	@Test
	public void getAllEventsInvalid() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
		
		mvc.perform(get("/events").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("events/index"))
        .andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
		verify(venueService).findAll();
		verify(mastodonController).getTimelinePosts();
	}
	
	@Test
	public void updateEventValid() throws Exception {
		Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);

	    Event event = new Event();
	    event.setId(1L);
	    event.setName("Test Event");
	    event.setDate(LocalDate.parse("2025-12-25"));
	    event.setTime(LocalTime.parse("10:30"));
	    event.setVenue(venue);
	    
	    Event eventUpdated = new Event();
	    eventUpdated.setId(1L);
	    eventUpdated.setName("Test Event Updated");
	    eventUpdated.setDate(LocalDate.parse("2025-12-25"));
	    eventUpdated.setTime(LocalTime.parse("10:30"));
	    eventUpdated.setVenue(venue);
		
		when(eventService.findById(1L)).thenReturn(Optional.of(event));
	
		when(eventService.update(any(Event.class), eq(1L))).thenReturn(eventUpdated);

		mvc.perform(put("/events/{id}", 1L)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .flashAttr("event", eventUpdated)) 
        .andExpect(status().is3xxRedirection()) 
        .andExpect(redirectedUrl("/events")); 
		
		

	    verify(eventService).findById(1L);
	    verify(eventService).update(any(Event.class), eq(1L));
	}
	
	@Test
	public void updateEventInvalid() throws Exception {

	    Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);

	    Event eventUpdated = new Event();
	    eventUpdated.setId(1L);
	    eventUpdated.setName("Test Event");
	    eventUpdated.setDate(LocalDate.parse("2025-12-25"));
	    eventUpdated.setTime(LocalTime.parse("10:30"));
	    eventUpdated.setVenue(venue);
	    
	    when(eventService.findById(1L)).thenReturn(Optional.empty());

	    mvc.perform(put("/events/{id}", 1L)
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .flashAttr("event", eventUpdated))
	            .andExpect(status().isNotFound()); 

	}
	
	
	@Test
	public void deleteEventValid() throws Exception {
		long eventId = 1L;
		when(eventService.existsById(eventId)).thenReturn(true);

		mvc.perform(delete("/events/{id}", eventId)
				.with(csrf())
				.with(user("admin").roles("ADMIN")))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/events"));

		verify(eventService).deleteById(eventId);
	}

	@Test
	public void deleteEventInvalid() throws Exception {
		long eventId = 99L;
		// Simulate that eventService.existsById returns false for eventId.
		when(eventService.existsById(eventId)).thenReturn(false);

		mvc.perform(delete("/events/{id}", eventId).with(csrf()))
			.andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteAllEvent() throws Exception {
		doNothing().when(eventService).deleteAll();

		mvc.perform(delete("/events"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/events"));

		verify(eventService).deleteAll();
	}
	
	@Test
	public void addEventForm() throws Exception {
		mvc.perform(get("/events/new_event"))
        .andExpect(status().isOk())
        .andExpect(view().name("events/new_event"));
		
		verify(venueService).findAll();
	}
	
	@Test
	public void createEvent() throws Exception {
	    Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");

	    Event event = new Event();
	    event.setName("Test Event");
	    event.setDate(LocalDate.parse("2025-12-25"));
	    event.setTime(LocalTime.parse("10:30"));
	    event.setVenue(venue);

	    when(venueService.findById(1L)).thenReturn(venue); 
	    when(eventService.addEvent(any(Event.class))).thenReturn(event);

	    mvc.perform(post("/events")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("venueId", "1") // Pass the venueId as a parameter
        .param("name", "Test Event") // Pass event fields as parameters
        .param("date", "2025-12-25")
        .param("time", "10:30")
        .flashAttr("event", event)) 
        .andExpect(status().is3xxRedirection()) 
        .andExpect(redirectedUrl("/events")); 

	    verify(eventService).addEvent(any(Event.class));
	}
	
	
	
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
        when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
        
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
        when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
        
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
        when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
        
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
        when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
        
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
    
    @Test
    public void testGetEventDetailsFound() throws Exception {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Test Venue");
        venue.setAddress("Test Address");
        venue.setCapacity(100);
        
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setDate(LocalDate.now().plusDays(1));
        event.setTime(LocalTime.of(12, 0));
        event.setVenue(venue);
        
        when(eventService.findById(1L)).thenReturn(Optional.of(event));
        when(venueService.findById(1L)).thenReturn(venue);
        when(venueService.findAll()).thenReturn(Arrays.asList(venue));
        when(mastodonController.getTimelinePosts()).thenReturn(Collections.emptyList());
        
        mvc.perform(get("/events/{id}/details", 1L)
           .with(csrf())
	       .accept(MediaType.TEXT_HTML))
	       .andExpect(status().isOk())
	       .andExpect(view().name("events/event_details"))
	       .andExpect(model().attribute("event", hasProperty("id", is(1L))))
	       .andExpect(model().attribute("venue", is(venue)))
	       .andExpect(model().attribute("venues", hasItem(venue)));
        
        verify(eventService).findById(1L);
        verify(venueService, atLeastOnce()).findById(1L);
        verify(venueService, atLeastOnce()).findAll();
    }
    
    @Test
    public void testGetEventDetailsNotFound() throws Exception {
        long eventId = 999L;
        
        when(eventService.findAll()).thenReturn(Collections.emptyList());
        
        mvc.perform(get("/events/{id}/details", eventId)
                .accept(MediaType.TEXT_HTML))
           .andExpect(status().isNotFound());
        
        verify(eventService).findById(999L);
        verifyNoInteractions(venueService);
    }
	
	
}
