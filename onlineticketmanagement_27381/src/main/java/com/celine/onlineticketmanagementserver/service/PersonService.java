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

import com.celine.onlineticketmanagementserver.dto.PersonRequest;
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
    public Person createPerson(PersonRequest request) {
        log.info("Creating new person with email: {}", request.getEmail());
        validateNewPerson(request);

        Location village = resolveVillage(request.getVillageId());

        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        person.setPhone(request.getPhone());
        person.setUsername(request.getUsername());
        person.setPassword(passwordEncoder.encode(request.getPassword()));
        person.setLivesIn(village);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            person.setRoles(resolveRolesByIds(request.getRoleIds()));
        }

        Person savedPerson = personRepository.save(person);
        log.info("Person created successfully with ID: {}", savedPerson.getId());
        return savedPerson;
    }

    @Transactional
    public Person updatePerson(Long id, PersonRequest request) {
        log.info("Updating person with ID: {}", id);

        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(existingPerson.getEmail())
                && Boolean.TRUE.equals(personRepository.existsByEmail(request.getEmail()))) {
            throw new ValidationException("Email already exists");
        }

        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(existingPerson.getUsername())
                && personRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        if (request.getPhone() != null
                && existingPerson.getPhone() != null
                && !request.getPhone().equalsIgnoreCase(existingPerson.getPhone())
                && Boolean.TRUE.equals(personRepository.existsByPhone(request.getPhone()))) {
            throw new ValidationException("Phone already exists");
        }

        existingPerson.setFirstName(request.getFirstName());
        existingPerson.setLastName(request.getLastName());
        existingPerson.setEmail(request.getEmail());
        existingPerson.setPhone(request.getPhone());
        existingPerson.setUsername(request.getUsername());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingPerson.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        existingPerson.setLivesIn(resolveVillage(request.getVillageId()));

        if (request.getRoleIds() != null) {
            existingPerson.setRoles(resolveRolesByIds(request.getRoleIds()));
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

    private void validateNewPerson(PersonRequest request) {
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new ValidationException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new ValidationException("Last name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ValidationException("Password is required");
        }
        if (Boolean.TRUE.equals(personRepository.existsByEmail(request.getEmail()))) {
            throw new ValidationException("Email already exists");
        }
        if (personRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && Boolean.TRUE.equals(personRepository.existsByPhone(request.getPhone()))) {
            throw new ValidationException("Phone already exists");
        }
    }

    private Location resolveVillage(Long villageId) {
        if (villageId == null) {
            throw new ValidationException("Village id is required when creating or updating a person");
        }

        Location village = locationRepository.findById(villageId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + villageId));

        if (village.getType() != LocationType.VILLAGE) {
            throw new ValidationException("Person must be linked to a village only");
        }
        return village;
    }

    private Set<Role> resolveRolesByIds(Set<Long> roleIds) {
        Set<Role> validatedRoles = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null) {
                throw new ValidationException("Role id is required");
            }
            Role existingRole = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
            validatedRoles.add(existingRole);
        }
        return validatedRoles;
    }
}
