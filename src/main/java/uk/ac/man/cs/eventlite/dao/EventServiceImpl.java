package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	private EventRepository eventRepository;

	private final static String DATA = "data/events.json";

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		try {
            return eventRepository.findAll();
        } catch (Exception e) {
            log.error("database has not been populated");
            return new ArrayList<>(); // return empty list if errors out
        }
	}
	
	@Override 
	public Event save(Event event) {
		return eventRepository.save(event);
	}

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

}