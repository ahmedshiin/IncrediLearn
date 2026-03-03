package com.incredilearn.incredilearn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.incredilearn.incredilearn.model.CourseQuestion;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, Long> {

    List<CourseQuestion> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}
