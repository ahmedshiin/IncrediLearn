package com.incredilearn.incredilearn.dto;

import java.util.List;

public class CreateQuizRequest {
    private String titre;
    private String description;
    private Long courseId;
    private List<QuestionRequest> questions;

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public List<QuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }

    public static class QuestionRequest {
        private String texte;
        private List<OptionRequest> options;

        public String getTexte() { return texte; }
        public void setTexte(String texte) { this.texte = texte; }

        public List<OptionRequest> getOptions() { return options; }
        public void setOptions(List<OptionRequest> options) { this.options = options; }
    }

    public static class OptionRequest {
        private String texte;
        private boolean correct;

        public String getTexte() { return texte; }
        public void setTexte(String texte) { this.texte = texte; }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }
}
