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

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(Security.class)
@AutoConfigureMockMvc(addFilters = false)
public class HomeControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;
	
	@Test
	public void getAllEventsValid() throws Exception {
		Venue venue = new Venue();
	    venue.setId(1L);
	    venue.setName("Test Venue");
	    venue.setCapacity(100);
	    
	    Venue venueTwo = new Venue();
	    venueTwo.setId(2L);
	    venueTwo.setName("Test Venue Two");
	    venueTwo.setCapacity(100);

	    Event event = new Event();
	    event.setId(1L);
	    event.setName("Test Event");
	    event.setDate(LocalDate.parse("2025-12-25"));
	    event.setTime(LocalTime.parse("10:30"));
	    event.setVenue(venue);
	    
	    Event eventTwo = new Event();
	    eventTwo.setId(2L);
	    eventTwo.setName("Test Event Two");
	    eventTwo.setDate(LocalDate.parse("2025-12-25"));
	    eventTwo.setTime(LocalTime.parse("10:30"));
	    eventTwo.setVenue(venueTwo);
	    
		Iterable<Event> events = Arrays.asList(event, eventTwo);
		Iterable<Venue> venues = Arrays.asList(venue, venueTwo);
		
		when(eventService.findAll()).thenReturn(events);
		when(venueService.findAll()).thenReturn(venues);
		
		mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk()) 
        .andExpect(view().name("index"))
        .andExpect(model().attributeExists("events")) 
        .andExpect(model().attributeExists("venues")) 
        .andExpect(model().attribute("events", hasSize(2))) 
        .andExpect(model().attribute("venues", hasSize(2)));

		verify(eventService).findAll();
		verify(venueService).findAll();
	}
	
	@Test
	public void getAllEventsInvalid() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		
		mvc.perform(get("/").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));

		verify(eventService).findAll();
		verify(venueService).findAll();
	}	
}
