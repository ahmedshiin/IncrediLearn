package com.incredilearn.incredilearn.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.incredilearn.incredilearn.dto.CourseContentDTO;
import com.incredilearn.incredilearn.dto.CourseDTO;
import com.incredilearn.incredilearn.dto.CreateCourseRequest;
import com.incredilearn.incredilearn.service.CourseContentService;
import com.incredilearn.incredilearn.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CourseController {
    
    private final CourseService courseService;
    private final CourseContentService courseContentService;
    
    public CourseController(CourseService courseService, CourseContentService courseContentService) {
        this.courseService = courseService;
        this.courseContentService = courseContentService;
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
    
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        if (course != null) {
            return ResponseEntity.ok(course);
        } else {
            return ResponseEntity.notFound().build();
        }
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
            return ResponseEntity.ok("Désinscription réussie");
        } else {
            return ResponseEntity.badRequest().body("Erreur lors de la désinscription");
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CreateCourseRequest request) {
        CourseDTO created = courseService.createCourse(request);
        if (created != null) {
            return ResponseEntity.ok(created);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/content/upload/{courseId}")
    public ResponseEntity<CourseContentDTO> uploadContent(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String contentType,
            @RequestParam(value = "order", defaultValue = "0") Integer sortOrder,
            @RequestParam(value = "duration", required = false) Double durationSeconds) {
        try {
            CourseContentDTO dto = courseContentService.uploadFile(courseId, file, contentType, sortOrder, durationSeconds);
            if (dto != null) {
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/content/{courseId}")
    public ResponseEntity<List<CourseContentDTO>> getCourseContents(@PathVariable Long courseId) {
        List<CourseContentDTO> contents = courseContentService.getContentsByCourse(courseId);
        return ResponseEntity.ok(contents);
    }
    
    @GetMapping("/content/download/{contentId}")
    public ResponseEntity<Resource> downloadContent(@PathVariable Long contentId) {
        try {
            Resource resource = courseContentService.downloadFile(contentId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            String fileName = courseContentService.getFileName(contentId);
            String mimeType = courseContentService.getMimeType(contentId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
