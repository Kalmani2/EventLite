package uk.ac.man.cs.eventlite.controllers;


import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class })
public class EventsControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;
	
//	@MockBean
//    private EventModelAssembler eventAssembler;
	
	// creds for authenticated requests
    private final String USERNAME = "Markel";
    private final String PASSWORD = "Vigo";

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/api/events")
		.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
		.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		Venue venue = new Venue();
		venue.setId(1L);
		venue.setName("Old Trafford");
		e.setVenue(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0]._links.venue.href", not(empty())));

		verify(eventService).findAll();
	}
	
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
		
		mvc.perform(get("/api/events/{id}", 1L)
		.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id", equalTo(1)))
        .andExpect(jsonPath("$.name").value("Test Event"))
		.andExpect(handler().methodName("getEvent"));
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error", containsString("event 99")))
		.andExpect(jsonPath("$.id", equalTo(99)))
		.andExpect(handler().methodName("getEvent"));
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
	   

	    when(eventService.existsById(1L)).thenReturn(true);
		when(eventService.update(any(Event.class), eq(1L))).thenReturn(eventUpdated);
        
		mvc.perform(put("/api/events/{id}", 1L)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .flashAttr("event", eventUpdated))
		.andExpect(status().is3xxRedirection()); 
		
	}
	
	@Test
	public void getVenueForEventWhenEventExistsAndHasVenue() throws Exception {
	    Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);

	    Event event = new Event();
	    event.setId(1L);
	    event.setName("Test Event");
	    event.setVenue(venue);

	    when(eventService.findById(1L)).thenReturn(Optional.of(event));

	    // Act & Assert: Perform GET request and verify response
	    mvc.perform(get("/api/events/1/venue").accept(MediaType.APPLICATION_JSON))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("getVenueForEvent"))
	        .andExpect(jsonPath("$.name", equalTo("Test Venue")))
	        .andExpect(jsonPath("$.capacity", equalTo(100)))
	        .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1")));
	}
	
	@Test
	public void getVenueForEventWhenEventDoesNotExist() throws Exception {
	    Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);

	    Event event = new Event();
	    event.setId(1L);
	    event.setName("Test Event");
	    event.setVenue(null);

	    when(eventService.findById(1L)).thenReturn(Optional.of(event));

	    // Act & Assert: Perform GET request and verify response
	    mvc.perform(get("/api/events/1/venue").accept(MediaType.APPLICATION_JSON))
	        .andExpect(status().isNotFound());
	}
}
