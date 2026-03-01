package com.incredilearn.incredilearn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incredilearn.incredilearn.dto.UserAdminDTO;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public AdminService(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCourses", courseRepository.count());
        long verifiedTeachers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ENSEIGNANT") && u.isVerified())
                .count();
        long pendingTeachers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ENSEIGNANT") && !u.isVerified())
                .count();
        stats.put("verifiedTeachers", verifiedTeachers);
        stats.put("pendingTeachers", pendingTeachers);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserAdminDTO verifyUser(Long id, boolean verified) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setVerified(verified);
        return toDTO(userRepository.save(user));
    }

    public UserAdminDTO blockUser(Long id, boolean blocked) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setBlocked(blocked);
        return toDTO(userRepository.save(user));
    }

    private UserAdminDTO toDTO(User user) {
        UserAdminDTO dto = new UserAdminDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setDomain(user.getDomain());
        dto.setVerified(user.isVerified());
        dto.setBlocked(user.isBlocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setCourseCount(user.getCoursesInscrits() != null ? user.getCoursesInscrits().size() : 0);
        return dto;
    }
}
