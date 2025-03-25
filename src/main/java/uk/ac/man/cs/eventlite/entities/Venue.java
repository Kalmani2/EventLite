package uk.ac.man.cs.eventlite.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    @Column(name = "name", nullable = false)
    private String name;

    private int capacity;

    @Column(name = "address", nullable = true)
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
