package com.celine.onlineticketmanagementserver.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.exception.ResourceNotFoundException;
import com.celine.onlineticketmanagementserver.exception.ValidationException;
import com.celine.onlineticketmanagementserver.model.Location;
import com.celine.onlineticketmanagementserver.model.Person;
import com.celine.onlineticketmanagementserver.model.Role;
import com.celine.onlineticketmanagementserver.model.Venue;
import com.celine.onlineticketmanagementserver.repository.LocationRepository;
import com.celine.onlineticketmanagementserver.repository.PersonRepository;
import com.celine.onlineticketmanagementserver.repository.RoleRepository;
import com.celine.onlineticketmanagementserver.repository.VenueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;
    private final VenueRepository venueRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Person> getAllPersons() {
        log.info("Fetching all persons");
        return personRepository.findAllWithRelations();
    }

    public List<Person> getAllPersonsWithRelations() {
        log.info("Fetching all persons with relations");
        return personRepository.findAllWithRelations();
    }

    public Person getPersonById(Long id) {
        log.info("Fetching person with ID: {}", id);
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
    }

    public Person getPersonByIdWithRelationships(Long id) {
        log.info("Fetching person with relationships, ID: {}", id);
        return personRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with email: " + email));
    }

    public Person getPersonByEmailIgnoreCase(String email) {
        return personRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with email: " + email));
    }

    public Person getPersonByPhone(String phone) {
        return personRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with phone: " + phone));
    }

    public List<Person> searchByFirstName(String firstName) {
        return personRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    public List<Person> searchByLastName(String lastName) {
        return personRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    public List<Person> searchByName(String name) {
        return personRepository.searchByName(name);
    }

    public List<Person> getPersonsByLocation(Long locationId) {
        return personRepository.findByLivesInId(locationId);
    }

    public List<Person> getPersonsByLocationWithDetails(Long locationId) {
        return personRepository.findByLocationIdWithRolesAndVenues(locationId);
    }

    public List<Person> getPersonsByRoleType(RoleType roleType) {
        return personRepository.findByRoleType(roleType);
    }

    public List<Person> getAllVenueManagers() {
        return personRepository.findAllVenueManagers();
    }

    public List<Person> getAllVenueManagersWithVenues() {
        return personRepository.findAllVenueManagersWithVenues();
    }

    public List<Person> getManagersOfVenue(Long venueId) {
        return personRepository.findManagersOfVenue(venueId);
    }

    public List<Person> getRecentlyRegistered(Date afterDate) {
        return personRepository.findRecentlyRegistered(afterDate);
    }

    public List<Person> getAllPersonsOrderedByRegistrationDate() {
        return personRepository.findAllByOrderByRegisteredAtDesc();
    }

    public List<Person> getAllPersonsOrderedByName() {
        return personRepository.findAllByOrderByFirstNameAscLastNameAsc();
    }

    @Transactional
    public Person createPerson(Person person) {
        log.info("Creating new person with email: {}", person.getEmail());
        validateNewPerson(person);

        Location village = resolveVillage(person.getLivesIn());
        person.setLivesIn(village);

        if (person.getPassword() != null && !person.getPassword().isBlank()) {
            person.setPassword(passwordEncoder.encode(person.getPassword()));
        }

        if (person.getRoles() != null && !person.getRoles().isEmpty()) {
            person.setRoles(resolveRoles(person.getRoles()));
        }

        Person savedPerson = personRepository.save(person);
        log.info("Person created successfully with ID: {}", savedPerson.getId());
        return savedPerson;
    }

    @Transactional
    public Person updatePerson(Long id, Person updatedPerson) {
        log.info("Updating person with ID: {}", id);

        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

        if (updatedPerson.getEmail() != null && !updatedPerson.getEmail().equalsIgnoreCase(existingPerson.getEmail())
                && Boolean.TRUE.equals(personRepository.existsByEmail(updatedPerson.getEmail()))) {
            throw new ValidationException("Email already exists");
        }

        if (updatedPerson.getUsername() != null && !updatedPerson.getUsername().equalsIgnoreCase(existingPerson.getUsername())
                && personRepository.existsByUsername(updatedPerson.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        if (updatedPerson.getPhone() != null
                && existingPerson.getPhone() != null
                && !updatedPerson.getPhone().equalsIgnoreCase(existingPerson.getPhone())
                && Boolean.TRUE.equals(personRepository.existsByPhone(updatedPerson.getPhone()))) {
            throw new ValidationException("Phone already exists");
        }

        existingPerson.setFirstName(updatedPerson.getFirstName());
        existingPerson.setLastName(updatedPerson.getLastName());
        existingPerson.setEmail(updatedPerson.getEmail());
        existingPerson.setPhone(updatedPerson.getPhone());
        existingPerson.setUsername(updatedPerson.getUsername());
        existingPerson.setEnabled(updatedPerson.getEnabled());
        existingPerson.setAccountNonExpired(updatedPerson.getAccountNonExpired());
        existingPerson.setAccountNonLocked(updatedPerson.getAccountNonLocked());
        existingPerson.setCredentialsNonExpired(updatedPerson.getCredentialsNonExpired());

        if (updatedPerson.getPassword() != null && !updatedPerson.getPassword().isBlank()) {
            existingPerson.setPassword(passwordEncoder.encode(updatedPerson.getPassword()));
        }

        if (updatedPerson.getLivesIn() != null) {
            existingPerson.setLivesIn(resolveVillage(updatedPerson.getLivesIn()));
        }

        if (updatedPerson.getRoles() != null) {
            existingPerson.setRoles(resolveRoles(updatedPerson.getRoles()));
        }

        if (updatedPerson.getProfile() != null) {
            updatedPerson.getProfile().setPerson(existingPerson);
            existingPerson.setProfile(updatedPerson.getProfile());
        }

        Person savedPerson = personRepository.save(existingPerson);
        log.info("Person updated successfully with ID: {}", savedPerson.getId());
        return savedPerson;
    }

    @Transactional
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with id: " + id);
        }
        personRepository.deleteById(id);
    }

    @Transactional
    public Person assignRoleToPerson(Long personId, Long roleId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        person.getRoles().add(role);
        return personRepository.save(person);
    }

    @Transactional
    public Person removeRoleFromPerson(Long personId, Long roleId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        person.getRoles().removeIf(role -> role.getId().equals(roleId));
        return personRepository.save(person);
    }

    @Transactional
    public Person addVenueManager(Long personId, Long venueId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
        Role venueManagerRole = roleRepository.findByName(RoleType.VENUE_MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("VENUE_MANAGER role not found"));

        person.getRoles().add(venueManagerRole);
        person.getManagesVenues().add(venue);
        venue.getManagers().add(person);

        personRepository.save(person);
        venueRepository.save(venue);
        return person;
    }

    @Transactional
    public Person updatePersonRoles(Long personId, Set<Long> roleIds) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        Set<Role> roles = roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id " + id)))
                .collect(Collectors.toSet());
        person.setRoles(roles);
        return personRepository.save(person);
    }

    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(personRepository.existsByEmail(email));
    }

    public boolean existsByPhone(String phone) {
        return Boolean.TRUE.equals(personRepository.existsByPhone(phone));
    }

    public boolean isVenueManager(Long personId) {
        return Boolean.TRUE.equals(personRepository.isVenueManager(personId));
    }

    public boolean managesVenue(Long personId, Long venueId) {
        return Boolean.TRUE.equals(personRepository.managesVenue(personId, venueId));
    }

    public boolean hasBookings(Long personId) {
        return Boolean.TRUE.equals(personRepository.hasBookings(personId));
    }

    public long countAllPersons() {
        return personRepository.count();
    }

    public long countPersonsByLocation(Long locationId) {
        return personRepository.countByLivesInId(locationId);
    }

    public long countPersonsByRoleType(RoleType roleType) {
        return personRepository.countByRoleType(roleType);
    }

    public long countBookingsByPerson(Long personId) {
        return personRepository.countBookingsByPerson(personId);
    }

    public List<Person> searchAll(String searchTerm) {
        return personRepository.searchAll(searchTerm);
    }

    public List<Person> searchByColumn(String searchTerm, String columnName) {
        return personRepository.searchByColumn(searchTerm, columnName);
    }

    public List<Person> getPersonsByProvinceCode(String provinceCode) {
        return personRepository.findByProvinceCode(provinceCode, LocationType.VILLAGE, LocationType.PROVINCE);
    }

    public List<Person> getPersonsByProvinceName(String provinceName) {
        return personRepository.findByProvinceName(provinceName, LocationType.VILLAGE, LocationType.PROVINCE);
    }

    public List<Person> getPersonsByProvinceCodeOrName(String value) {
        return personRepository.findByProvinceCodeOrProvinceName(value, LocationType.VILLAGE, LocationType.PROVINCE);
    }

    public Page<Person> getPersonsPage(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    private void validateNewPerson(Person person) {
        if (person.getEmail() == null || person.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (person.getUsername() == null || person.getUsername().isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (person.getPassword() == null || person.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }
        if (Boolean.TRUE.equals(personRepository.existsByEmail(person.getEmail()))) {
            throw new ValidationException("Email already exists");
        }
        if (personRepository.existsByUsername(person.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (person.getPhone() != null && !person.getPhone().isBlank()
                && Boolean.TRUE.equals(personRepository.existsByPhone(person.getPhone()))) {
            throw new ValidationException("Phone already exists");
        }
    }

    private Location resolveVillage(Location locationReference) {
        if (locationReference == null || locationReference.getId() == null) {
            throw new ValidationException("Village is required when creating or updating a person");
        }

        Location village = locationRepository.findById(locationReference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationReference.getId()));

        if (village.getType() != LocationType.VILLAGE) {
            throw new ValidationException("Person must be linked to a village only");
        }
        return village;
    }

    private Set<Role> resolveRoles(Set<Role> roles) {
        Set<Role> validatedRoles = new HashSet<>();
        for (Role role : roles) {
            if (role.getId() == null) {
                throw new ValidationException("Role id is required");
            }
            Role existingRole = roleRepository.findById(role.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + role.getId()));
            validatedRoles.add(existingRole);
        }
        return validatedRoles;
    }
}
