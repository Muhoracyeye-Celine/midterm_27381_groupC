package com.celine.onlineticketmanagementserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.exception.ResourceNotFoundException;
import com.celine.onlineticketmanagementserver.exception.ValidationException;
import com.celine.onlineticketmanagementserver.model.Location;
import com.celine.onlineticketmanagementserver.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepo;

    public String saveLocation(Location location) {
        if (locationRepo.existsByCode(location.getCode())) {
            return "Location with this code already exists";
        }
        if (locationRepo.existsByName(location.getName())) {
            return "Location with this name already exists";
        }

        validateParentRelationship(location);
        locationRepo.save(location);
        return "Location saved successfully";
    }

    public Location createLocation(Location location) {
        if (locationRepo.existsByCode(location.getCode())) {
            throw new ValidationException("Location with this code already exists");
        }
        validateParentRelationship(location);
        return locationRepo.save(location);
    }

    public List<Location> getAllLocations() {
        return locationRepo.findAll();
    }

    public Page<Location> getAllLocationsWithPagination(Pageable pageable) {
        return locationRepo.findAll(pageable);
    }

    public Optional<Location> getLocationById(Long id) {
        return locationRepo.findById(id);
    }

    public Optional<Location> getLocationByCode(String code) {
        return locationRepo.findByCode(code);
    }

    public Optional<Location> getLocationByCodeIgnoreCase(String code) {
        return locationRepo.findByCodeIgnoreCase(code);
    }

    public Optional<Location> getLocationByName(String name) {
        return locationRepo.findByName(name);
    }

    public Optional<Location> getLocationByNameIgnoreCase(String name) {
        return locationRepo.findByNameIgnoreCase(name);
    }

    public List<Location> getLocationsByType(LocationType type) {
        return locationRepo.findByType(type);
    }

    public List<Location> getLocationsByTypeString(String typeString) {
        try {
            return locationRepo.findByType(LocationType.valueOf(typeString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    public Page<Location> getLocationsByTypeWithPagination(LocationType type, Pageable pageable) {
        return locationRepo.findByType(type, pageable);
    }

    public Page<Location> getLocationsByTypeStringWithPagination(String typeString, Pageable pageable) {
        try {
            return locationRepo.findByType(LocationType.valueOf(typeString.toUpperCase()), pageable);
        } catch (IllegalArgumentException e) {
            return Page.empty();
        }
    }

    public List<Location> getChildLocationsByParentId(Long parentId) {
        return locationRepo.findByParentLocationId(parentId);
    }

    public Page<Location> getChildLocationsByParentIdWithPagination(Long parentId, Pageable pageable) {
        return locationRepo.findByParentLocationId(parentId, pageable);
    }

    public List<Location> getAllProvinces() {
        return locationRepo.findAllProvinces();
    }

    public List<Location> getAllDistrictsByProvince(Long provinceId) {
        return locationRepo.findAllDistrictsByProvince(provinceId);
    }

    public List<Location> getAllSectorsByDistrict(Long districtId) {
        return locationRepo.findAllSectorsByDistrict(districtId);
    }

    public List<Location> getAllCellsBySector(Long sectorId) {
        return locationRepo.findAllCellsBySector(sectorId);
    }

    public List<Location> getAllVillagesByCell(Long cellId) {
        return locationRepo.findAllVillagesByCell(cellId);
    }

    public List<Location> searchLocationsByName(String name) {
        return locationRepo.findByNameContainingIgnoreCase(name);
    }

    public List<Location> searchLocationsByCode(String code) {
        return locationRepo.findByCodeContainingIgnoreCase(code);
    }

    public List<Location> getLocationHierarchy(Long locationId) {
        List<Location> hierarchy = new ArrayList<>();
        Optional<Location> current = locationRepo.findById(locationId);
        while (current.isPresent()) {
            hierarchy.add(0, current.get());
            current = Optional.ofNullable(current.get().getParentLocation());
        }
        return hierarchy;
    }

    public Long countLocationsByType(LocationType type) {
        return locationRepo.countByType(type);
    }

    public Long countLocationsByTypeString(String typeString) {
        try {
            return locationRepo.countByType(LocationType.valueOf(typeString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    public Long countChildLocations(Long parentId) {
        return locationRepo.countByParentLocationId(parentId);
    }

    public Boolean hasChildren(Long locationId) {
        return locationRepo.hasChildren(locationId);
    }

    public String updateLocation(Long id, Location updatedLocation) {
        Location existing = locationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));

        if (!existing.getCode().equals(updatedLocation.getCode()) && locationRepo.existsByCode(updatedLocation.getCode())) {
            return "Location code already exists";
        }
        if (!existing.getName().equals(updatedLocation.getName()) && locationRepo.existsByName(updatedLocation.getName())) {
            return "Location name already exists";
        }

        existing.setCode(updatedLocation.getCode());
        existing.setName(updatedLocation.getName());
        existing.setType(updatedLocation.getType());
        existing.setParentLocation(updatedLocation.getParentLocation());
        validateParentRelationship(existing);
        locationRepo.save(existing);
        return "Location updated successfully";
    }

    public String updateLocationByCode(String code, Location updatedLocation) {
        Location existing = locationRepo.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with code: " + code));
        existing.setName(updatedLocation.getName());
        existing.setType(updatedLocation.getType());
        existing.setParentLocation(updatedLocation.getParentLocation());
        validateParentRelationship(existing);
        locationRepo.save(existing);
        return "Location updated successfully";
    }

    public String deleteLocation(Long id) {
        Location existing = locationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
        if (locationRepo.hasChildren(id)) {
            return "Cannot delete location: it has child locations";
        }
        locationRepo.delete(existing);
        return "Location deleted successfully with ID: " + id;
    }

    public String deleteLocationByCode(String code) {
        Location existing = locationRepo.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with code: " + code));
        if (locationRepo.hasChildren(existing.getId())) {
            return "Cannot delete location: it has child locations";
        }
        locationRepo.delete(existing);
        return "Location deleted successfully with code: " + code;
    }

    private void validateParentRelationship(Location location) {
        if (location.getType() == null) {
            throw new ValidationException("Location type is required");
        }

        if (location.getType() == LocationType.PROVINCE) {
            location.setParentLocation(null);
            return;
        }

        if (location.getParentLocation() == null || location.getParentLocation().getId() == null) {
            throw new ValidationException("Parent location is required for type " + location.getType());
        }

        Location parent = locationRepo.findById(location.getParentLocation().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent location not found with ID: " + location.getParentLocation().getId()));

        boolean valid = switch (location.getType()) {
            case DISTRICT -> parent.getType() == LocationType.PROVINCE;
            case SECTOR -> parent.getType() == LocationType.DISTRICT;
            case CELL -> parent.getType() == LocationType.SECTOR;
            case VILLAGE -> parent.getType() == LocationType.CELL;
            case PROVINCE -> true;
        };

        if (!valid) {
            throw new ValidationException("Invalid parent relationship for " + location.getType());
        }

        location.setParentLocation(parent);
    }
}
