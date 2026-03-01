package com.incredilearn.incredilearn.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incredilearn.incredilearn.dto.UserAdminDTO;
import com.incredilearn.incredilearn.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserAdminDTO>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<UserAdminDTO> verifyUser(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean verified = Boolean.TRUE.equals(body.get("verified"));
        return ResponseEntity.ok(adminService.verifyUser(id, verified));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<UserAdminDTO> blockUser(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean blocked = Boolean.TRUE.equals(body.get("blocked"));
        return ResponseEntity.ok(adminService.blockUser(id, blocked));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
