package com.celine.onlineticketmanagementserver.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "person_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nationalId;

    private String gender;

    private String addressLine;

    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;
    
}