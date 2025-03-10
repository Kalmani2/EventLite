package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {
		Event event = null;
		for (Event e : eventService.findAll()) {
			if (e.getId() == id) {
				event = e;
				break;
			}
		}
		if (event == null) {
			throw new EventNotFoundException(id);
		}

		Venue linkedVenue = event.getVenue();

		// Venue linkedVenue = null;
		// for (Venue v : venueService.findAll()) {
		// if (v.getId() == event.getVenue().getId()) {
		// linkedVenue = v;
		// break;
		// }
		// }

		model.addAttribute("event", event);
		model.addAttribute("venue", linkedVenue);

		return "events/event";
	}

	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}
	
	@PutMapping("/{id}")
	public String updateEvent(@RequestBody Event event, @PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		eventService.update(event, id);

		redirectAttrs.addFlashAttribute("ok_message", "Event edited.");

		return "redirect:/events";
	}

	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {

		if (!eventService.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
		}

		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");

		return "redirect:/events";

	}

	@DeleteMapping
	public String deleteAllEvents(RedirectAttributes redirectAttrs) {
		eventService.deleteAll();
		redirectAttrs.addFlashAttribute("ok_message", "All events deleted.");

		return "redirect:/events";
	}

	@GetMapping("/new_event")
	public String addEventForm(Model model) {
		model.addAttribute("event", new Event());
		model.addAttribute("venues", venueService.findAll());
		return "events/new_event";
	}

	@PostMapping
	public String createEvent(@RequestParam("venueId") long venueId, @ModelAttribute("event") Event event) {
		Venue venue = venueService.findById(venueId);
		event.setVenue(venue);

		eventService.addEvent(event);
		return "redirect:/events";
	}

	@GetMapping("/search")
	public String searchEvents(@RequestParam("query") String query, Model model) {
		Iterable<Event> events = eventService.findAll();
		List<Event> filteredEvents = new ArrayList<>();

		for (Event event : events) {
			if (event.getName().toLowerCase().contains(query.toLowerCase())) {
				filteredEvents.add(event);
			}
		}

		model.addAttribute("events", filteredEvents);
		model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}

	@GetMapping("/{id}/details")
	public String getEventDetails(@PathVariable("id") long id, Model model) {
		Event event = null;
		for (Event e : eventService.findAll()) {
			if (e.getId() == id) {
				event = e;
				break;
			}
		}
		if (event == null) {
			throw new EventNotFoundException(id);
		}

		Venue venue = null;
		for (Venue v : venueService.findAll()) {
			if (v == event.getVenue()) {
				venue = v;
				break;
			}
		}

		model.addAttribute("event", event);
		model.addAttribute("venue", venue);

		return "events/event_details";
	}

}
