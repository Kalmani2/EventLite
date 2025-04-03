package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class EventsControllerApiIntegrationTest {
	
	@Autowired
	private WebTestClient client;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	// Credentials for authenticated requests
	private final String USERNAME = "Markel";
	private final String PASSWORD = "Vigo";

	@BeforeEach
	public void setup(WebTestClient webTestClient) {
		client = webTestClient;
	}

	// ---------- GET Endpoints ----------

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$._links.self.href").value(endsWith("/api/events"));
	}

	@Test
	public void testGetEventNotFound() {
		client.get().uri("/events/99")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.error").value(containsString("event 99"))
				.jsonPath("$.id").isEqualTo(99);
	}

	// ---------- POST Endpoints (Event Creation) ----------

	// Sensible Data
	@Test
	public void testCreateEventSensibleData() {
		// Prepare a valid venue and dummy event response.
		Venue venue = new Venue();
		venue.setId(1);
		venue.setName("Test Venue");
		when(venueService.findById(1L)).thenReturn(venue);

		Event event = new Event();
		event.setId(1);
		event.setName("New Event");
		event.setDate(LocalDate.now().plusDays(10));
		event.setTime(LocalTime.of(12, 0));
		event.setDescription("A sensible new event");
		event.setVenue(venue);
		when(eventService.addEvent(any(Event.class))).thenReturn(event);

		client.post().uri("/events")
				.headers(headers -> headers.setBasicAuth(USERNAME, PASSWORD))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{\"venueId\":1, \"name\":\"New Event\", \"date\":\"" + event.getDate().toString()
						+ "\", \"time\":\"12:00:00\", \"description\":\"A sensible new event\"}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.CREATED)
				.expectHeader().value("Location", value -> value.contains("/api/events/"));
	}

	// Missing Data (e.g., missing required 'name')
	@Test
	public void testCreateEventMissingData() {
		// Missing the "name" field
		client.post().uri("/events")
				.headers(headers -> headers.setBasicAuth(USERNAME, PASSWORD))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(
						"{\"venueId\":1, \"date\":\"2025-12-31\", \"time\":\"12:00:00\", \"description\":\"Missing name\"}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isBadRequest();
	}

	// Invalid Data (e.g., date in the past)
	@Test
	public void testCreateEventInvalidData() {
		// A past date should be invalid.
		client.post().uri("/events")
				.headers(headers -> headers.setBasicAuth(USERNAME, PASSWORD))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(
						"{\"venueId\":1, \"name\":\"Event with Past Date\", \"date\":\"2000-01-01\", \"time\":\"12:00:00\", \"description\":\"Invalid date\"}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isBadRequest();
	}

	// Unauthorised Use (assuming the endpoint requires authentication)
	@Test
	public void testCreateEventUnauthorised() {
		// If the endpoint requires authentication and our test profile enforces it,
		// then a POST without credentials should return 401 Unauthorized.
		client.post().uri("/events")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(
						"{\"venueId\":1, \"name\":\"Unauthorised Event\", \"date\":\"2025-12-31\", \"time\":\"12:00:00\", \"description\":\"Should not be allowed\"}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}
}