package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {
	
	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

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
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public Event addEvent(Event event) {
		return save(event);
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