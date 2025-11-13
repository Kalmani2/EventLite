# EventLite

A full-stack event management system built with Spring Boot, featuring RESTful APIs, interactive maps, and social media integration.

## Overview

EventLite is a comprehensive web application for managing events and venues. It demonstrates modern Java web development practices with a complete MVC architecture, HATEOAS-compliant REST APIs, and integration with external services. The application supports both traditional server-side rendered views and JSON API responses, making it suitable for multiple client types.

## Key Features

### Event & Venue Management
- Full CRUD operations for events and venues
- Event details including date, time, venue, and description
- Search functionality with case-insensitive filtering
- Relationship management between events and venues (ManyToOne)

### Interactive Mapping
- **Mapbox Geocoding API** integration for automatic address validation
- Real-time coordinate retrieval (latitude/longitude) for venue locations
- Interactive maps in event detail pages using **Mapbox GL JS**
- Visual display of venue locations on embedded maps

### Social Media Integration
- **Mastodon API** integration for fetching timeline posts
- Display of social media content on the events homepage
- Demonstrates consumption of external REST APIs

### Dual Interface Architecture
- **Web Interface**: Server-side rendered HTML using Thymeleaf templates
- **REST API**: JSON responses with HATEOAS links for programmatic access
- Content negotiation supporting both `text/html` and `application/hal+json`

### Security & Authorization
- Spring Security with role-based access control
- Public read access (GET requests)
- Admin-only write operations (POST, PUT, DELETE)
- CSRF protection and authentication mechanisms
- Custom login page

## Technical Architecture

### Backend Technologies
- **Java** - Core programming language
- **Spring Boot** - Application framework
- **Spring MVC** - Web layer and request handling
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM implementation
- **Spring Security** - Authentication and authorization
- **Spring HATEOAS** - Hypermedia-driven REST API support

### Frontend Technologies
- **Thymeleaf** - Server-side HTML templating engine
- **Mapbox GL JS** - Interactive map visualization
- **Bootstrap** (implied from layouts) - UI styling

### External APIs
- **Mapbox Geocoding API** - Address validation and geocoding
- **Mapbox GL JS** - Map rendering and interaction
- **Mastodon API** - Social media timeline integration

### Data Persistence
- **JPA/Hibernate** - Object-relational mapping
- **Spring Data JPA Repositories** - Data access abstraction
- Profile-based database configuration (development/test environments)

### Build & Testing
- **Maven** - Build automation and dependency management
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework for unit tests
- **Spring Test** - Integration testing support
- **@WebMvcTest** - Controller layer testing
- Achieved 90%+ test coverage across unit and integration tests

## Project Structure

```
src/
├── main/
│   ├── java/uk/ac/man/cs/eventlite/
│   │   ├── assemblers/          # HATEOAS resource assemblers
│   │   ├── config/              # Application configuration
│   │   ├── controllers/         # Web and API controllers
│   │   ├── dao/                 # Data access (repositories & services)
│   │   ├── entities/            # JPA entities (Event, Venue)
│   │   ├── exceptions/          # Custom exception classes
│   │   └── validator/           # Custom validation logic
│   └── resources/
│       └── templates/           # Thymeleaf HTML templates
└── test/
    └── java/uk/ac/man/cs/eventlite/
        ├── controllers/         # Controller tests
        ├── dao/                 # Repository tests
        └── testutil/            # Test utilities
```

## Design Patterns & Best Practices

### MVC Architecture
- Clear separation of concerns with Controller, Service, and Repository layers
- `EventsController` for HTML views, `EventsControllerApi` for JSON responses
- Service layer (`EventServiceImpl`, `VenueServiceImpl`) for business logic
- Repository pattern with Spring Data JPA interfaces

### REST API Design
- HATEOAS Level 3 maturity model implementation
- Custom `EventModelAssembler` for dynamic link generation
- Self-describing API responses with navigable resource links
- Consistent URI design (`/events/{id}`, `/venues/{id}`)

### Exception Handling
- Custom exceptions (`EventNotFoundException`, `VenueNotFoundException`)
- Global exception handlers with `@ExceptionHandler`
- User-friendly error pages (404 pages for missing resources)
- Flash messages for user feedback

### Data Validation
- Custom validators (`AddressValidator`, `@ValidAddress` annotation)
- Integration with Mapbox API for real-world address verification
- Bean validation annotations on entity classes

### Testing Strategy
- Comprehensive unit tests with Mockito for mocked dependencies
- Integration tests using Spring's test context
- API tests for RESTful endpoints
- Profile-based test configurations (`@ActiveProfiles("test")`)
- Test data isolation with in-memory database

## API Endpoints

### Events API
- `GET /events` - List all events
- `GET /events/{id}` - Get event details
- `POST /events` - Create new event (admin only)
- `PUT /events/{id}` - Update event (admin only)
- `DELETE /events/{id}` - Delete event (admin only)
- `GET /events/search?query={term}` - Search events

### Venues API
- `GET /venues` - List all venues
- `GET /venues/{id}` - Get venue details
- `POST /venues` - Create new venue (admin only)
- `PUT /venues/{id}` - Update venue (admin only)
- `DELETE /venues/{id}` - Delete venue (admin only)

## Development Setup

Launch configurations are provided in the `launchers/` directory for Eclipse IDE:
- `EventLite run.launch` - Run the application
- `EventLite test.launch` - Run all tests
- `EventLite package.launch` - Build Maven package

## License

Academic project developed for COMP23412 - Software Engineering 2 at the University of Manchester.