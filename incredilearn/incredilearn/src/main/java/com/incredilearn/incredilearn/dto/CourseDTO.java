package com.incredilearn.incredilearn.dto;

public class CourseDTO {
    private Long id;
    private String titre;
    private String description;
    private String domain;
    private String enseignantNom;
    private Long enseignantId;
    private Integer dureeHeures;
    private String niveau;
    private Integer nombreEtudiants;
    
    // Constructeurs
    public CourseDTO() {}
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getEnseignantNom() {
        return enseignantNom;
    }
    
    public void setEnseignantNom(String enseignantNom) {
        this.enseignantNom = enseignantNom;
    }
    
    public Long getEnseignantId() {
        return enseignantId;
    }
    
    public void setEnseignantId(Long enseignantId) {
        this.enseignantId = enseignantId;
    }
    
    public Integer getDureeHeures() {
        return dureeHeures;
    }
    
    public void setDureeHeures(Integer dureeHeures) {
        this.dureeHeures = dureeHeures;
    }
    
    public String getNiveau() {
        return niveau;
    }
    
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
    
    public Integer getNombreEtudiants() {
        return nombreEtudiants;
    }
    
    public void setNombreEtudiants(Integer nombreEtudiants) {
        this.nombreEtudiants = nombreEtudiants;
    }
}
