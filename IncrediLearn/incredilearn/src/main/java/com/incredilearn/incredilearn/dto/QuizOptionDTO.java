package com.incredilearn.incredilearn.dto;

public class QuizOptionDTO {
    private Long id;
    private String texte;
    private boolean correct;

    public QuizOptionDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
