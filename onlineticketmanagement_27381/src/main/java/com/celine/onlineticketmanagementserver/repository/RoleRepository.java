package com.celine.onlineticketmanagementserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Check if role exists by name (using RoleType enum)
    Boolean existsByName(RoleType name);
    
    // Find role by name (using RoleType enum)
    Optional<Role> findByName(RoleType name);
    
    // Get all roles with pagination
    Page<Role> findAll(Pageable pageable);
    
    // Count persons who have a specific role
    @Query("SELECT COUNT(p) FROM Person p JOIN p.roles r WHERE r.id = :roleId")
    Long countPersonsByRole(@Param("roleId") Long roleId);
    
    // Count persons who have a specific role type
    @Query("SELECT COUNT(p) FROM Person p JOIN p.roles r WHERE r.name = :roleType")
    Long countPersonsByRoleType(@Param("roleType") RoleType roleType);
    
    // Check if role is assigned to any person
    @Query("SELECT COUNT(p) > 0 FROM Person p JOIN p.roles r WHERE r.id = :roleId")
    Boolean isRoleAssigned(@Param("roleId") Long roleId);
    
    // Get all roles ordered by name
    List<Role> findAllByOrderByNameAsc();
    
    // Find all persons with a specific role type
    @Query("SELECT p FROM Person p JOIN p.roles r WHERE r.name = :roleType")
    List<Object> findPersonsByRoleType(@Param("roleType") RoleType roleType);
}