package com.incredilearn.incredilearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.incredilearn.incredilearn.model.CourseQuestionReply;

@Repository
public interface CourseQuestionReplyRepository extends JpaRepository<CourseQuestionReply, Long> {
}
