package com.celine.onlineticketmanagementserver.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Role;
import com.celine.onlineticketmanagementserver.service.RoleService;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;


    @PostMapping("/save")
    public ResponseEntity<String> saveRole(@RequestBody Role role) {
        String result = roleService.saveRole(role);

        if (result.contains("already exists")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
    

    @GetMapping("/all/ordered")
    public ResponseEntity<List<Role>> getAllRolesOrdered() {
        List<Role> roles = roleService.getAllRolesOrdered();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
    

    @GetMapping("/all/paginated")
    public ResponseEntity<Page<Role>> getAllRolesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> roles = roleService.getAllRolesWithPagination(pageable);
        
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }


    @GetMapping("/id/{id}")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        
        if (role.isPresent()) {
            return new ResponseEntity<>(role.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Role not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/type/{roleType}")
    public ResponseEntity<?> getRoleByType(@PathVariable String roleType) {
        try {
            RoleType type = RoleType.valueOf(roleType.toUpperCase());
            Optional<Role> role = roleService.getRoleByType(type);
            
            if (role.isPresent()) {
                return new ResponseEntity<>(role.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Role not found with type: " + roleType, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid role type. Must be one of: ADMIN, USER, VENUE_MANAGER, ORGANIZER, TICKET_SCANNER, CUSTOMER_SUPPORT", 
                                       HttpStatus.BAD_REQUEST);
        }
    }
    

    @GetMapping("/count/persons/{roleId}")
    public ResponseEntity<Long> countPersonsWithRole(@PathVariable Long roleId) {
        Long count = roleService.countPersonsWithRole(roleId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
    

    @GetMapping("/count/persons/type/{roleType}")
    public ResponseEntity<?> countPersonsWithRoleType(@PathVariable String roleType) {
        try {
            Long count = roleService.countPersonsWithRoleTypeString(roleType);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid role type. Must be one of: ADMIN, USER, VENUE_MANAGER, ORGANIZER, TICKET_SCANNER, CUSTOMER_SUPPORT", 
                                       HttpStatus.BAD_REQUEST);
        }
    }
    

    @GetMapping("/{roleId}/is-assigned")
    public ResponseEntity<Boolean> isRoleAssigned(@PathVariable Long roleId) {
        Boolean isAssigned = roleService.isRoleAssigned(roleId);
        return new ResponseEntity<>(isAssigned, HttpStatus.OK);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateRole(@PathVariable Long id, @RequestBody Role updatedRole) {
        String message = roleService.updateRole(id, updatedRole);

        if (message.equals("Role updated successfully")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else if (message.contains("already exists")) {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
    }
    

    @PutMapping("/update/type/{roleType}")
    public ResponseEntity<?> updateRoleByType(@PathVariable String roleType, @RequestBody Role updatedRole) {
        try {
            RoleType type = RoleType.valueOf(roleType.toUpperCase());
            String message = roleService.updateRoleByType(type, updatedRole);

            if (message.equals("Role updated successfully")) {
                return new ResponseEntity<>(message, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid role type. Must be one of: ADMIN, USER, VENUE_MANAGER, ORGANIZER, TICKET_SCANNER, CUSTOMER_SUPPORT", 
                                       HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        String result = roleService.deleteRole(id);
        
        if (result.startsWith("Role deleted")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (result.contains("assigned to")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
    }
    

    @DeleteMapping("/delete/type/{roleType}")
    public ResponseEntity<?> deleteRoleByType(@PathVariable String roleType) {
        try {
            RoleType type = RoleType.valueOf(roleType.toUpperCase());
            String result = roleService.deleteRoleByType(type);
            
            if (result.startsWith("Role deleted")) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else if (result.contains("assigned to")) {
                return new ResponseEntity<>(result, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid role type. Must be one of: ADMIN, USER, VENUE_MANAGER, ORGANIZER, TICKET_SCANNER, CUSTOMER_SUPPORT", 
                                       HttpStatus.BAD_REQUEST);
        }
    }
}