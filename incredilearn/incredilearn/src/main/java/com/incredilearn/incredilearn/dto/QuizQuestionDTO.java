package com.incredilearn.incredilearn.dto;

import java.util.List;

public class QuizQuestionDTO {
    private Long id;
    private String texte;
    private Integer sortOrder;
    private List<QuizOptionDTO> options;

    public QuizQuestionDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public List<QuizOptionDTO> getOptions() { return options; }
    public void setOptions(List<QuizOptionDTO> options) { this.options = options; }
}
