package com.celine.onlineticketmanagementserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Role;
import com.celine.onlineticketmanagementserver.repository.RoleRepository;

@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepo;

    @Transactional
    public String saveRole(Role role) {
        try {
            if (roleRepo.existsByName(role.getName())) {
                return "Role with this name already exists";
            }
            
            roleRepo.save(role);
            return "Role saved successfully";
            
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMostSpecificCause().getMessage();
            
            if (errorMessage.contains("name") || errorMessage.contains("unique")) {
                return "Role name already exists (database constraint)";
            } else {
                return "Database constraint violation: " + errorMessage;
            }
        } catch (Exception e) {
            return "Error saving role: " + e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Role> getAllRolesOrdered() {
        return roleRepo.findAllByOrderByNameAsc();
    }
    
    @Transactional(readOnly = true)
    public Page<Role> getAllRolesWithPagination(Pageable pageable) {
        return roleRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Long id) {
        return roleRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Role> getRoleByType(RoleType roleType) {
        return roleRepo.findByName(roleType);
    }
    
    @Transactional(readOnly = true)
    public Optional<Role> getRoleByTypeString(String roleTypeString) {
        try {
            RoleType roleType = RoleType.valueOf(roleTypeString.toUpperCase());
            return roleRepo.findByName(roleType);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    @Transactional(readOnly = true)
    public Long countPersonsWithRole(Long roleId) {
        return roleRepo.countPersonsByRole(roleId);
    }
    
    @Transactional(readOnly = true)
    public Long countPersonsWithRoleType(RoleType roleType) {
        return roleRepo.countPersonsByRoleType(roleType);
    }
    
    @Transactional(readOnly = true)
    public Long countPersonsWithRoleTypeString(String roleTypeString) {
        try {
            RoleType roleType = RoleType.valueOf(roleTypeString.toUpperCase());
            return roleRepo.countPersonsByRoleType(roleType);
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }
    
    @Transactional(readOnly = true)
    public Boolean isRoleAssigned(Long roleId) {
        return roleRepo.isRoleAssigned(roleId);
    }

    @Transactional
    public String updateRole(Long id, Role updatedRole) {
        try {
            Optional<Role> existingRole = roleRepo.findById(id);

            if (existingRole.isPresent()) {
                Role role = existingRole.get();


                if (!role.getName().equals(updatedRole.getName()) && 
                    roleRepo.existsByName(updatedRole.getName())) {
                    return "Role name already exists";
                }

                role.setName(updatedRole.getName());
                role.setDescription(updatedRole.getDescription());

                roleRepo.save(role);
                return "Role updated successfully";
            } else {
                return "Role not found with ID: " + id;
            }
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMostSpecificCause().getMessage();
            
            if (errorMessage.contains("name") || errorMessage.contains("unique")) {
                return "Role name already exists (database constraint)";
            } else {
                return "Database constraint violation: " + errorMessage;
            }
        } catch (Exception e) {
            return "Error updating role: " + e.getMessage();
        }
    }
    
    @Transactional
    public String updateRoleByType(RoleType roleType, Role updatedRole) {
        try {
            Optional<Role> existingRole = roleRepo.findByName(roleType);

            if (existingRole.isPresent()) {
                Role role = existingRole.get();
                role.setDescription(updatedRole.getDescription());
                roleRepo.save(role);
                return "Role updated successfully";
            } else {
                return "Role not found with type: " + roleType;
            }
        } catch (Exception e) {
            return "Error updating role: " + e.getMessage();
        }
    }

    @Transactional
    public String deleteRole(Long id) {
        try {
            Optional<Role> existingRole = roleRepo.findById(id);

            if (existingRole.isPresent()) {
                if (roleRepo.isRoleAssigned(id)) {
                    return "Cannot delete role: it is assigned to one or more persons";
                }
                
                roleRepo.delete(existingRole.get());
                return "Role deleted successfully with ID: " + id;
            } else {
                return "Role not found with ID: " + id;
            }
        } catch (Exception e) {
            return "Error deleting role: " + e.getMessage();
        }
    }
    
    @Transactional
    public String deleteRoleByType(RoleType roleType) {
        try {
            Optional<Role> existingRole = roleRepo.findByName(roleType);

            if (existingRole.isPresent()) {
                Role role = existingRole.get();
                
                if (roleRepo.isRoleAssigned(role.getId())) {
                    return "Cannot delete role: it is assigned to one or more persons";
                }
                
                roleRepo.delete(role);
                return "Role deleted successfully with type: " + roleType;
            } else {
                return "Role not found with type: " + roleType;
            }
        } catch (Exception e) {
            return "Error deleting role: " + e.getMessage();
        }
    }
}