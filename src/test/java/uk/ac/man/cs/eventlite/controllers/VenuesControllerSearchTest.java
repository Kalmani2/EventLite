package uk.ac.man.cs.eventlite.controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerSearchTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VenueService venueService;
    
    @MockBean
    private EventService eventService;

    @Test
    public void testSearchWithResults() throws Exception {
        // Create test venues
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Kilburn Building");
        venue1.setCapacity(200);
        venue1.setRoadAddress("Oxford Road, Manchester");
        venue1.setPostcode("M13 9PL");
        
        Venue venue2 = new Venue();
        venue2.setId(2);
        venue2.setName("University Place");
        venue2.setCapacity(500);
        venue2.setRoadAddress("Oxford Road, Manchester");
        venue2.setPostcode("M13 9PL");
        
        // Mock the service method call
        when(venueService.findByNameContainingIgnoreCase("kilburn"))
            .thenReturn(Arrays.asList(venue1));
        
        mvc.perform(get("/venues/search").param("query", "kilburn")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attribute("venues", hasSize(1)))
                .andExpect(model().attribute("venues", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Kilburn Building")),
                            hasProperty("capacity", is(200)),
                            hasProperty("roadAddress", is("Oxford Road, Manchester")),
                            hasProperty("postcode", is("M13 9PL"))
                        )
                )));
        
        verify(venueService).findByNameContainingIgnoreCase("kilburn");
    }

    @Test
    public void testSearchWithNoResults() throws Exception {
        // Mock the service method call with empty results
        when(venueService.findByNameContainingIgnoreCase("nonexistent"))
            .thenReturn(Collections.emptyList());
        
        mvc.perform(get("/venues/search").param("query", "nonexistent")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attribute("venues", hasSize(0)));
        
        verify(venueService).findByNameContainingIgnoreCase("nonexistent");
    }

    @Test
    public void testSearchCaseInsensitive() throws Exception {
        // Create test venue
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Kilburn Building");
        venue1.setCapacity(200);
        venue1.setRoadAddress("Oxford Road, Manchester");
        venue1.setPostcode("M13 9PL");
        
        // Mock the service method call
        when(venueService.findByNameContainingIgnoreCase("KILBURN"))
            .thenReturn(Arrays.asList(venue1));
        
        mvc.perform(get("/venues/search").param("query", "KILBURN")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attribute("venues", hasSize(1)))
                .andExpect(model().attribute("venues", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Kilburn Building")),
                            hasProperty("capacity", is(200)),
                            hasProperty("roadAddress", is("Oxford Road, Manchester")),
                            hasProperty("postcode", is("M13 9PL"))
                        )
                )));
        
        verify(venueService).findByNameContainingIgnoreCase("KILBURN");
    }
    
    @Test
    public void testSearchPartialMatch() throws Exception {
        // Create test venue
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Kilburn Building");
        venue1.setCapacity(200);
        venue1.setRoadAddress("Oxford Road, Manchester");
        venue1.setPostcode("M13 9PL");
        
        // Mock the service method call
        when(venueService.findByNameContainingIgnoreCase("burn"))
            .thenReturn(Arrays.asList(venue1));
        
        mvc.perform(get("/venues/search").param("query", "burn")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attribute("venues", hasSize(1)))
                .andExpect(model().attribute("venues", hasItem(
                        allOf(
                            hasProperty("id", is(1L)),
                            hasProperty("name", is("Kilburn Building")),
                            hasProperty("capacity", is(200)),
                            hasProperty("roadAddress", is("Oxford Road, Manchester")),
                            hasProperty("postcode", is("M13 9PL"))
                        )
                )));
        
        verify(venueService).findByNameContainingIgnoreCase("burn");
    }
    
    @Test
    public void testSearchMultipleResults() throws Exception {
        // Create test venues
        Venue venue1 = new Venue();
        venue1.setId(1);
        venue1.setName("Manchester Hall");
        venue1.setCapacity(300);
        venue1.setRoadAddress("Oxford Road, Manchester");
        venue1.setPostcode("M13 9PL");
        
        Venue venue2 = new Venue();
        venue2.setId(2);
        venue2.setName("Manchester Conference Center");
        venue2.setCapacity(500);
        venue2.setRoadAddress("City Center, Manchester");
        venue2.setPostcode("M13 9PL");
        
        List<Venue> matchingVenues = Arrays.asList(venue1, venue2);
        
        // Mock the service method call
        when(venueService.findByNameContainingIgnoreCase("manchester"))
            .thenReturn(matchingVenues);
        
        mvc.perform(get("/venues/search").param("query", "manchester")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attribute("venues", hasSize(2)))
                .andExpect(model().attribute("venues", hasItems(
                        allOf(hasProperty("id", is(1L)), hasProperty("name", is("Manchester Hall")), hasProperty("roadAddress", is("Oxford Road, Manchester")), hasProperty("postcode", is("M13 9PL"))),
                        allOf(hasProperty("id", is(2L)), hasProperty("name", is("Manchester Conference Center")), hasProperty("roadAddress", is("City Center, Manchester")), hasProperty("postcode", is("M13 9PL")))
                )));
        
        verify(venueService).findByNameContainingIgnoreCase("manchester");
    }
}