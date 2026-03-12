package com.celine.onlineticketmanagementserver.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.model.Location;
import com.celine.onlineticketmanagementserver.service.LocationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/save")
    public ResponseEntity<String> saveLocation(@RequestBody Location location) {
        String result = locationService.saveLocation(location);
        if (result.contains("already exists")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(location));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Location>> getAllLocations() {
        return new ResponseEntity<>(locationService.getAllLocations(), HttpStatus.OK);
    }

    @GetMapping("/all/paginated")
    public ResponseEntity<Page<Location>> getAllLocationsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(locationService.getAllLocationsWithPagination(pageable), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationService.getLocationById(id);
        return location.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Location not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getLocationByCode(@PathVariable String code) {
        Optional<Location> location = locationService.getLocationByCodeIgnoreCase(code);
        return location.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Location not found with code: " + code, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getLocationByName(@PathVariable String name) {
        Optional<Location> location = locationService.getLocationByNameIgnoreCase(name);
        return location.<ResponseEntity<?>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Location not found with name: " + name, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getLocationsByType(@PathVariable String type) {
        try {
            return new ResponseEntity<>(locationService.getLocationsByType(LocationType.valueOf(type.toUpperCase())), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid location type. Must be one of: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/type/{type}/paginated")
    public ResponseEntity<?> getLocationsByTypeWithPagination(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            return new ResponseEntity<>(locationService.getLocationsByTypeWithPagination(LocationType.valueOf(type.toUpperCase()), pageable), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid location type. Must be one of: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/parent/{parentId}/children")
    public ResponseEntity<List<Location>> getChildLocationsByParentId(@PathVariable Long parentId) {
        return new ResponseEntity<>(locationService.getChildLocationsByParentId(parentId), HttpStatus.OK);
    }

    @GetMapping("/parent/{parentId}/children/paginated")
    public ResponseEntity<Page<Location>> getChildLocationsByParentIdWithPagination(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(locationService.getChildLocationsByParentIdWithPagination(parentId, pageable), HttpStatus.OK);
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<Location>> getAllProvinces() {
        return new ResponseEntity<>(locationService.getAllProvinces(), HttpStatus.OK);
    }

    @GetMapping("/province/{provinceId}/districts")
    public ResponseEntity<List<Location>> getAllDistrictsByProvince(@PathVariable Long provinceId) {
        return new ResponseEntity<>(locationService.getAllDistrictsByProvince(provinceId), HttpStatus.OK);
    }

    @GetMapping("/district/{districtId}/sectors")
    public ResponseEntity<List<Location>> getAllSectorsByDistrict(@PathVariable Long districtId) {
        return new ResponseEntity<>(locationService.getAllSectorsByDistrict(districtId), HttpStatus.OK);
    }

    @GetMapping("/sector/{sectorId}/cells")
    public ResponseEntity<List<Location>> getAllCellsBySector(@PathVariable Long sectorId) {
        return new ResponseEntity<>(locationService.getAllCellsBySector(sectorId), HttpStatus.OK);
    }

    @GetMapping("/cell/{cellId}/villages")
    public ResponseEntity<List<Location>> getAllVillagesByCell(@PathVariable Long cellId) {
        return new ResponseEntity<>(locationService.getAllVillagesByCell(cellId), HttpStatus.OK);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Location>> searchLocationsByName(@RequestParam String name) {
        return new ResponseEntity<>(locationService.searchLocationsByName(name), HttpStatus.OK);
    }

    @GetMapping("/search/code")
    public ResponseEntity<List<Location>> searchLocationsByCode(@RequestParam String code) {
        return new ResponseEntity<>(locationService.searchLocationsByCode(code), HttpStatus.OK);
    }

    @GetMapping("/hierarchy/{locationId}")
    public ResponseEntity<List<Location>> getLocationHierarchy(@PathVariable Long locationId) {
        return new ResponseEntity<>(locationService.getLocationHierarchy(locationId), HttpStatus.OK);
    }

    @GetMapping("/count/type/{type}")
    public ResponseEntity<?> countLocationsByType(@PathVariable String type) {
        try {
            return new ResponseEntity<>(locationService.countLocationsByType(LocationType.valueOf(type.toUpperCase())), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid location type. Must be one of: PROVINCE, DISTRICT, SECTOR, CELL, VILLAGE", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/count/children/{parentId}")
    public ResponseEntity<Long> countChildLocations(@PathVariable Long parentId) {
        return new ResponseEntity<>(locationService.countChildLocations(parentId), HttpStatus.OK);
    }

    @GetMapping("/{locationId}/has-children")
    public ResponseEntity<Boolean> hasChildren(@PathVariable Long locationId) {
        return new ResponseEntity<>(locationService.hasChildren(locationId), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateLocation(@PathVariable Long id, @RequestBody Location updatedLocation) {
        String message = locationService.updateLocation(id, updatedLocation);
        if (message.equals("Location updated successfully")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if (message.contains("already exists")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/code/{code}")
    public ResponseEntity<String> updateLocationByCode(@PathVariable String code, @RequestBody Location updatedLocation) {
        String message = locationService.updateLocationByCode(code, updatedLocation);
        if (message.equals("Location updated successfully")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteLocation(@PathVariable Long id) {
        String result = locationService.deleteLocation(id);
        if (result.startsWith("Location deleted")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("child locations")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/code/{code}")
    public ResponseEntity<String> deleteLocationByCode(@PathVariable String code) {
        String result = locationService.deleteLocationByCode(code);
        if (result.startsWith("Location deleted")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("child locations")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }
}
