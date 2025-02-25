package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {
	
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override 
	public Event save(Event event) {
		return eventRepository.save(event);
	}

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

	@Override
	public Event update(Event event, long id) {
		Event eventDB = eventRepository.findById(id).get();
		
		if (Objects.nonNull(event.getName()) && !"".equalsIgnoreCase(event.getName())) {
			eventDB.setName(event.getName());
		}	
		
		if (Objects.nonNull(event.getDate())) {
			eventDB.setDate(event.getDate());
		}
		
		if (Objects.nonNull(event.getTime())) {
			eventDB.setTime(event.getTime());
		}
		
		if (Objects.nonNull(event.getVenue())) {
			eventDB.setVenue(event.getVenue());
		}
		
		return eventRepository.save(event);
	}

}