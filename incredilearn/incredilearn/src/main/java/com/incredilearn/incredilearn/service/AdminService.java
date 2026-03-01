package com.incredilearn.incredilearn.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incredilearn.incredilearn.dto.UserAdminDTO;
import com.incredilearn.incredilearn.model.Role;
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

        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.ETUDIANT)
            .count();
        long totalTeachers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.ENSEIGNANT)
            .count();
        long totalCourses = courseRepository.count();
        long pendingVerifications = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.ENSEIGNANT && !u.isVerified())
            .count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalCourses", totalCourses);
        stats.put("pendingVerifications", pendingVerifications);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public UserAdminDTO verifyUser(Long id, boolean verified) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
        user.setVerified(verified);
        return convertToDTO(userRepository.save(user));
    }

    public UserAdminDTO blockUser(Long id, boolean blocked) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
        user.setBlocked(blocked);
        return convertToDTO(userRepository.save(user));
    }

    private UserAdminDTO convertToDTO(User user) {
        UserAdminDTO dto = new UserAdminDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name().toLowerCase() : null);
        dto.setDomain(user.getDomain());
        dto.setVerified(user.isVerified());
        dto.setBlocked(user.isBlocked());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() == Role.ENSEIGNANT) {
            dto.setCourseCount(courseRepository.findByEnseignantId(user.getId()).size());
        } else {
            dto.setCourseCount(user.getCoursesInscrits() != null ? user.getCoursesInscrits().size() : 0);
        }

        return dto;
    }
}
