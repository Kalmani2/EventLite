package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import uk.ac.man.cs.eventlite.validator.ValidAddress;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "name", nullable = false)
    @Size(max = 256, message = "Name must be less than 256 characters")
    private String name;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    // Expects <road address, postcode> as input
    @Column(name = "address", nullable = false)
    @Size(max = 300, message = "Road address must be less than 300 characters")
    @ValidAddress
    private String address;

    @Column(name = "latitude", nullable = true)
    private Double latitude;

    @Column(name = "longitude", nullable = true)
    private Double longitude;

    public Venue() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
