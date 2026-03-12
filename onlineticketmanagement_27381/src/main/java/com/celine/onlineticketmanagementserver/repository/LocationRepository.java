package com.celine.onlineticketmanagementserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // Check if location exists by code
    Boolean existsByCode(String code);
    
    // Check if location exists by name
    Boolean existsByName(String name);
    
    // Find location by code
    Optional<Location> findByCode(String code);
    
    // Find location by name
    Optional<Location> findByName(String name);
    
    // Find location by code (case insensitive)
    Optional<Location> findByCodeIgnoreCase(String code);
    
    // Find location by name (case insensitive)
    Optional<Location> findByNameIgnoreCase(String name);
    
    // Find all locations by type (using LocationType enum)
    List<Location> findByType(LocationType type);
    
    // Find all locations by type with pagination (using LocationType enum)
    Page<Location> findByType(LocationType type, Pageable pageable);
    
    // Find all child locations of a parent location
    List<Location> findByParentLocationId(Long parentLocationId);
    
    // Find all child locations of a parent location with pagination
    Page<Location> findByParentLocationId(Long parentLocationId, Pageable pageable);
    
    // Find all locations by type and parent location (using LocationType enum)
    List<Location> findByTypeAndParentLocationId(LocationType type, Long parentLocationId);
    
    // Find locations by name containing (for search)
    List<Location> findByNameContainingIgnoreCase(String name);
    
    // Find locations by code containing (for search)
    List<Location> findByCodeContainingIgnoreCase(String code);
    
    // CORRECTED: Using enums package path instead of model package
    // Custom query to get all provinces (locations without parent)
    @Query("SELECT l FROM Location l WHERE l.parentLocation IS NULL AND l.type = com.celine.onlineticketmanagementserver.enums.LocationType.PROVINCE")
    List<Location> findAllProvinces();
    
    // Custom query to get all districts of a province
    @Query("SELECT l FROM Location l WHERE l.parentLocation.id = :provinceId AND l.type = com.celine.onlineticketmanagementserver.enums.LocationType.DISTRICT")
    List<Location> findAllDistrictsByProvince(@Param("provinceId") Long provinceId);
    
    // Custom query to get all sectors of a district
    @Query("SELECT l FROM Location l WHERE l.parentLocation.id = :districtId AND l.type = com.celine.onlineticketmanagementserver.enums.LocationType.SECTOR")
    List<Location> findAllSectorsByDistrict(@Param("districtId") Long districtId);
    
    // Custom query to get all cells of a sector
    @Query("SELECT l FROM Location l WHERE l.parentLocation.id = :sectorId AND l.type = com.celine.onlineticketmanagementserver.enums.LocationType.CELL")
    List<Location> findAllCellsBySector(@Param("sectorId") Long sectorId);
    
    // Custom query to get all villages of a cell
    @Query("SELECT l FROM Location l WHERE l.parentLocation.id = :cellId AND l.type = com.celine.onlineticketmanagementserver.enums.LocationType.VILLAGE")
    List<Location> findAllVillagesByCell(@Param("cellId") Long cellId);
    
    // Get the full hierarchy path for a location
    @Query("SELECT l FROM Location l WHERE l.id = :locationId")
    Optional<Location> findLocationWithHierarchy(@Param("locationId") Long locationId);
    
    // Count locations by type (using LocationType enum)
    Long countByType(LocationType type);
    
    // Count child locations of a parent
    Long countByParentLocationId(Long parentLocationId);
    
    // Find all root locations (provinces without parent)
    @Query("SELECT l FROM Location l WHERE l.parentLocation IS NULL ORDER BY l.name")
    List<Location> findAllRootLocations();
    
    // Check if a location has children
    @Query("SELECT COUNT(l) > 0 FROM Location l WHERE l.parentLocation.id = :locationId")
    Boolean hasChildren(@Param("locationId") Long locationId);
}