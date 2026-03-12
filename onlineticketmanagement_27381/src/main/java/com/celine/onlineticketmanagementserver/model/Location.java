package com.celine.onlineticketmanagementserver.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.celine.onlineticketmanagementserver.enums.LocationType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String code;
    
    @Column(nullable = false)
    @ToString.Include
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private LocationType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_location_id")
    @JsonIgnore
    private Location parentLocation;
    
    @OneToMany(mappedBy = "parentLocation", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Location> childLocations = new HashSet<>();
    
    @OneToMany(mappedBy = "livesIn")
    @JsonIgnore
    private Set<Person> residents = new HashSet<>();
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return id != null && id.equals(location.id);
    }
    

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}