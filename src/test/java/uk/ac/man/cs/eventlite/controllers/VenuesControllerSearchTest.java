package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
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
        venue1.setAddress("Oxford Road, Manchester");
        
        Venue venue2 = new Venue();
        venue2.setId(2);
        venue2.setName("University Place");
        venue2.setCapacity(500);
        venue2.setAddress("Oxford Road, Manchester");
        
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
                            hasProperty("address", is("Oxford Road, Manchester"))
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
        venue1.setAddress("Oxford Road, Manchester");
        
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
                            hasProperty("name", is("Kilburn Building"))
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
        venue1.setAddress("Oxford Road, Manchester");
        
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
                            hasProperty("name", is("Kilburn Building"))
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
        venue1.setAddress("Oxford Road, Manchester");
        
        Venue venue2 = new Venue();
        venue2.setId(2);
        venue2.setName("Manchester Conference Center");
        venue2.setCapacity(500);
        venue2.setAddress("City Center, Manchester");
        
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
                        allOf(hasProperty("id", is(1L)), hasProperty("name", is("Manchester Hall"))),
                        allOf(hasProperty("id", is(2L)), hasProperty("name", is("Manchester Conference Center")))
                )));
        
        verify(venueService).findByNameContainingIgnoreCase("manchester");
    }
}