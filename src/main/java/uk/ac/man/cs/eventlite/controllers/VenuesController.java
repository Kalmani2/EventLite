package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping("/venues")
public class VenuesController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @GetMapping
    public String getAllVenues(Model model) {
        model.addAttribute("venues", venueService.findAll());
        return "venues/index";
    }

    @GetMapping("/search")
    public String searchVenues(@RequestParam("query") String query, Model model) {
        model.addAttribute("venues", venueService.findByNameContainingIgnoreCase(query));
        return "venues/index";
    }
    
    @GetMapping("/{id}/details")
    public String getVenueDetails(@PathVariable("id") long id, Model model) {
        Venue venue = venueService.findById(id);
        if (venue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        Iterable<Event> allEvents = eventService.findAll();

        List<Event> venueEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getVenue() != null && event.getVenue().getId() == id) {
                venueEvents.add(event);
            }
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Event> upcomingEvents = venueEvents.stream()
            .filter(event -> event.getDate().isAfter(today) || 
                             (event.getDate().isEqual(today) && 
                             event.getTime() == null || event.getTime().isAfter(now)))
            .collect(Collectors.toList());

        upcomingEvents.sort((e1, e2) -> {
            if (e1.getDate().equals(e2.getDate())) {
                if (e1.getTime() == null || e2.getTime() == null) {
                    return 0;
                }
                return e1.getTime().compareTo(e2.getTime());
            }
            return e1.getDate().compareTo(e2.getDate());
        });

        model.addAttribute("venue", venue);
        model.addAttribute("upcomingEvents", upcomingEvents);
        return "venues/venue_details";
    }

    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
        // Check if the venue exists
        if (!venueService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        // Check if there are any events associated with this venue
        if (eventService.existsByVenueId(id)) {
            redirectAttrs.addFlashAttribute("error_message", "Cannot delete venue with existing events.");
            return "redirect:/venues"; // Redirect back to venues index
        }

        // Proceed to delete the venue
        venueService.deleteById(id);
        redirectAttrs.addFlashAttribute("ok_message", "Venue deleted.");
        return "redirect:/venues"; // Redirect to the venues index page
    }

    @PutMapping("/{id}")
    public String updateVenue(@PathVariable("id") long id, @ModelAttribute Venue venue, RedirectAttributes redirectAttrs) {
        // Retrieve the existing venue
        Venue existingVenue = venueService.findById(id);
        if (existingVenue == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }

        // Update the existing venue's properties
        existingVenue.setName(venue.getName());
        existingVenue.setRoadAddress(venue.getRoadAddress());
        existingVenue.setPostcode(venue.getPostcode());
        existingVenue.setCapacity(venue.getCapacity());

        // Save the updated venue
        venueService.save(existingVenue);
        redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");
        return "redirect:/venues"; // Redirect to the venues index page
    }

    @GetMapping("/new_venue")
    public String addVenueForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/new_venue"; // Ensure this points to the correct Thymeleaf template
    }

    @PostMapping
    public String createVenue(@ModelAttribute Venue venue, RedirectAttributes redirectAttrs) {
        venueService.save(venue); // Save the new venue
        redirectAttrs.addFlashAttribute("ok_message", "Venue created successfully.");
        return "redirect:/venues"; // Redirect to the venues index page
    }
}
