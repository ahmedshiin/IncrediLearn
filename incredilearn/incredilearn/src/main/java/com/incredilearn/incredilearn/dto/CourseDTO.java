package com.incredilearn.incredilearn.dto;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private String imageUrl;
    private Integer nombreLecons;
    private Long dureeTotaleSecondes;
    
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getNombreLecons() {
        return nombreLecons;
    }

    public void setNombreLecons(Integer nombreLecons) {
        this.nombreLecons = nombreLecons;
    }

    public Long getDureeTotaleSecondes() {
        return dureeTotaleSecondes;
    }

    public void setDureeTotaleSecondes(Long dureeTotaleSecondes) {
        this.dureeTotaleSecondes = dureeTotaleSecondes;
    }
}
