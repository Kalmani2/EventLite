package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
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
        model.addAttribute("venue", venue);
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
}
