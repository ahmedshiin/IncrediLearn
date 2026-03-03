package com.incredilearn.incredilearn.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.incredilearn.incredilearn.dto.CourseContentDTO;
import com.incredilearn.incredilearn.model.Course;
import com.incredilearn.incredilearn.model.CourseContent;
import com.incredilearn.incredilearn.repository.CourseContentRepository;
import com.incredilearn.incredilearn.repository.CourseRepository;

@Service
public class CourseContentService {

    private final CourseContentRepository courseContentRepository;
    private final CourseRepository courseRepository;
    private final Path uploadDir;

    public CourseContentService(CourseContentRepository courseContentRepository, CourseRepository courseRepository) {
        this.courseContentRepository = courseContentRepository;
        this.courseRepository = courseRepository;
        this.uploadDir = Paths.get("uploads/course-content").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'uploads", e);
        }
    }

    public CourseContentDTO uploadFile(Long courseId, MultipartFile file, String contentType, Integer sortOrder, Double durationSeconds) throws IOException {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return null;
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String storedFileName = UUID.randomUUID().toString() + extension;

        Path targetPath = this.uploadDir.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        CourseContent content = new CourseContent();
        content.setCourse(course);
        content.setFileName(originalFileName != null ? originalFileName : "fichier");
        content.setStoredFileName(storedFileName);
        content.setContentType(contentType);
        content.setMimeType(file.getContentType());
        content.setFileSize(file.getSize());
        content.setSortOrder(sortOrder != null ? sortOrder : 0);
        if (durationSeconds != null && durationSeconds > 0) {
            content.setDurationSeconds(durationSeconds);
        }

        CourseContent saved = courseContentRepository.save(content);

        // If this is a cover image, update the course's imageUrl
        if ("Cover".equalsIgnoreCase(contentType)) {
            course.setImageUrl("/api/courses/content/download/" + saved.getId());
            courseRepository.save(course);
        }

        return convertToDTO(saved);
    }

    public List<CourseContentDTO> getContentsByCourse(Long courseId) {
        return courseContentRepository.findByCourseIdOrderBySortOrderAsc(courseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Resource downloadFile(Long contentId) throws IOException {
        CourseContent content = courseContentRepository.findById(contentId).orElse(null);
        if (content == null) {
            return null;
        }

        Path filePath = this.uploadDir.resolve(content.getStoredFileName()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        }
        return null;
    }

    public String getFileName(Long contentId) {
        CourseContent content = courseContentRepository.findById(contentId).orElse(null);
        return content != null ? content.getFileName() : null;
    }

    public String getMimeType(Long contentId) {
        CourseContent content = courseContentRepository.findById(contentId).orElse(null);
        return content != null ? content.getMimeType() : "application/octet-stream";
    }

    private CourseContentDTO convertToDTO(CourseContent content) {
        CourseContentDTO dto = new CourseContentDTO();
        dto.setId(content.getId());
        dto.setCourseId(content.getCourse().getId());
        dto.setFileName(content.getFileName());
        dto.setContentType(content.getContentType());
        dto.setMimeType(content.getMimeType());
        dto.setFileSize(content.getFileSize());
        dto.setSortOrder(content.getSortOrder());
        dto.setDownloadUrl("/api/courses/content/download/" + content.getId());
        dto.setDurationSeconds(content.getDurationSeconds());
        return dto;
    }
}
