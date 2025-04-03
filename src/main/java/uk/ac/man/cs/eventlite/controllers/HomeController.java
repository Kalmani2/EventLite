package uk.ac.man.cs.eventlite.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HomeController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @GetMapping("/")
    public String home(Model model) {
        // You can add any model attributes here if needed
		Iterable<Event> allEvents = eventService.findAll();
		Iterable<Venue> allVenues = venueService.findAll();
		
		Map<Venue, Integer> venueEvents = new HashMap<>();
		for (Venue venue : allVenues) {
			Integer eventNumber = 0;
	        for (Event event : allEvents) {
	            if (event.getVenue() != null && event.getVenue().getId() == venue.getId()) {
	                eventNumber += 1;
	            }
	        }
	        venueEvents.put(venue, eventNumber);
		}
		
		List<Map.Entry<Venue, Integer>> sortedVenueEvents = new ArrayList<>(venueEvents.entrySet());

        // Sort by values (descending order)
        sortedVenueEvents.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    
        model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", sortedVenueEvents);
		
        return "index"; // This should match the name of your index.html in templates
    }
    
} 