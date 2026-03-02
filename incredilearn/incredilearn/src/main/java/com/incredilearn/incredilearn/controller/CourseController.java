package com.incredilearn.incredilearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incredilearn.incredilearn.dto.CourseDTO;
import com.incredilearn.incredilearn.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CourseController {
    
    private final CourseService courseService;
    
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CourseDTO>> getMesCours(@PathVariable Long userId) {
        List<CourseDTO> courses = courseService.getMesCoursForUser(userId);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    @PostMapping("/enroll/{userId}/{courseId}")
    public ResponseEntity<String> enrollInCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        boolean success = courseService.enrollUserInCourse(userId, courseId);
        if (success) {
            return ResponseEntity.ok("Inscription réussie");
        } else {
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription");
        }
    }
    @DeleteMapping("/enroll/{userId}/{courseId}")
    public ResponseEntity<String> unenrollFromCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        boolean success = courseService.unenrollUserFromCourse(userId, courseId);
        if (success) {
            return ResponseEntity.ok("D\u00e9sinscription r\u00e9ussie");
        } else {
            return ResponseEntity.badRequest().body("Erreur lors de la d\u00e9sinscription");
        }
    }}
