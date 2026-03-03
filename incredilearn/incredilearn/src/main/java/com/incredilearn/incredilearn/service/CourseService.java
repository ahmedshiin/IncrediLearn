package com.incredilearn.incredilearn.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.incredilearn.incredilearn.dto.CourseDTO;
import com.incredilearn.incredilearn.dto.CreateCourseRequest;
import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.User;
import com.incredilearn.incredilearn.repository.CourseContentRepository;
import com.incredilearn.incredilearn.repository.CourseRepository;
import com.incredilearn.incredilearn.repository.UserRepository;

@Service
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseContentRepository courseContentRepository;
    
    public CourseService(CourseRepository courseRepository, UserRepository userRepository, CourseContentRepository courseContentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseContentRepository = courseContentRepository;
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
    
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }
        return convertToDTO(course);
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
    
    public CourseDTO createCourse(CreateCourseRequest request) {
        User enseignant = userRepository.findById(request.getEnseignantId()).orElse(null);
        if (enseignant == null) {
            return null;
        }
        
        Course course = new Course();
        course.setTitre(request.getTitre());
        course.setDescription(request.getDescription());
        course.setDomain(request.getDomain());
        course.setNiveau(request.getNiveau());
        course.setDureeHeures(request.getDureeHeures());
        course.setEnseignant(enseignant);
        
        Course saved = courseRepository.save(course);
        return convertToDTO(saved);
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
        dto.setCreatedAt(course.getCreatedAt());
        dto.setImageUrl(course.getImageUrl());
        // Count only Video content items
        long lecons = courseContentRepository.countByCourseIdAndContentType(course.getId(), "Video");
        dto.setNombreLecons((int) lecons);
        // Sum video durations
        Double totalSeconds = courseContentRepository.sumDurationSecondsByCourseId(course.getId());
        dto.setDureeTotaleSecondes(totalSeconds != null ? totalSeconds.longValue() : 0L);
        
        if (course.getEnseignant() != null) {
            dto.setEnseignantNom(course.getEnseignant().getNom());
            dto.setEnseignantId(course.getEnseignant().getId());
        }
        
        return dto;
    }
}
