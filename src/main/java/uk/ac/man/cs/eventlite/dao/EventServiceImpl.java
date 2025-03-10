package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private EventRepository eventRepository;

//	private final static String DATA = "data/events.json";

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
	public Event addEvent(Event event) {
		return save(event);
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
    
    @Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public void deleteAll() {
		eventRepository.deleteAll();
	}

	@Override
	public void deleteAll(Iterable<Event> event) {
		eventRepository.deleteAll(event);
	}

	@Override
	public void deleteAllById(Iterable<Long> ids) {
		eventRepository.deleteAllById(ids);
	}
	
    @Override
    public List<Event> findAllOrderedByDateAndName() {
        return eventRepository.findAllByOrderByDateAscTimeAsc();
    }
    
}