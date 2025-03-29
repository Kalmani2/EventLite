package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import uk.ac.man.cs.eventlite.validator.ValidAddress;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "name", nullable = false)
    @Size(max = 256)
    private String name;

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
