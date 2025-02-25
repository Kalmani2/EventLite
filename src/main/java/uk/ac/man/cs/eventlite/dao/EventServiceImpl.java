package uk.ac.man.cs.eventlite.dao;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
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
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override 
	public Event save(Event event) {
		return eventRepository.save(event);
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
	
	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}

}