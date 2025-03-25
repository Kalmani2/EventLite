package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class HomeControllerApi {

    @GetMapping
    public RepresentationModel<?> api() {
        RepresentationModel<?> model = new RepresentationModel<>();
        
        // Add links to events and venues endpoints
        model.add(linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withRel("events"));
        model.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withRel("venues"));
        
        // Add link to profile if you have one
        model.add(Link.of("http://localhost:8000/api/profile").withRel("profile"));
        
        return model;
    }
}