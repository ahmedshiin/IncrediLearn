package com.incredilearn.incredilearn.dto;

import java.util.List;

public class SummarizeRequest {

    private Long courseId;
    private List<Long> contentIds;        // IDs of uploaded Document contents
    private List<String> lessonDescriptions; // "Title: description" strings for selected lessons

    public SummarizeRequest() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public List<Long> getContentIds() { return contentIds; }
    public void setContentIds(List<Long> contentIds) { this.contentIds = contentIds; }

    public List<String> getLessonDescriptions() { return lessonDescriptions; }
    public void setLessonDescriptions(List<String> lessonDescriptions) { this.lessonDescriptions = lessonDescriptions; }
}
