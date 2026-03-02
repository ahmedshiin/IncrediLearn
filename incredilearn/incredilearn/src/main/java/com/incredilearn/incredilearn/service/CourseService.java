package com.incredilearn.incredilearn.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.incredilearn.incredilearn.dto.CourseDTO;
import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }
    
    public List<CourseDTO> getMesCoursForUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        
        return user.getCoursesInscrits().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public boolean enrollUserInCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);
        
        if (user == null || course == null) {
            return false;
        }
        
        user.getCoursesInscrits().add(course);
        userRepository.save(user);
        return true;
    }

    public boolean unenrollUserFromCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            return false;
        }

        user.getCoursesInscrits().remove(course);
        userRepository.save(user);
        return true;
    }
    
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitre(course.getTitre());
        dto.setDescription(course.getDescription());
        dto.setDomain(course.getDomain());
        dto.setDureeHeures(course.getDureeHeures());
        dto.setNiveau(course.getNiveau());
        dto.setNombreEtudiants(course.getEtudiants().size());
        
        if (course.getEnseignant() != null) {
            dto.setEnseignantNom(course.getEnseignant().getNom());
            dto.setEnseignantId(course.getEnseignant().getId());
        }
        
        return dto;
    }
}
