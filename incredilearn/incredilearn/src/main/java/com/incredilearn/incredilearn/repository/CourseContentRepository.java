package com.incredilearn.incredilearn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.incredilearn.incredilearn.model.CourseContent;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByCourseIdOrderBySortOrderAsc(Long courseId);
    long countByCourseIdAndContentType(Long courseId, String contentType);

    @Query("SELECT COALESCE(SUM(c.durationSeconds), 0) FROM CourseContent c WHERE c.course.id = :courseId AND c.contentType = 'Video'")
    Double sumDurationSecondsByCourseId(@Param("courseId") Long courseId);
}
