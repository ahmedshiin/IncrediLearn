package com.incredilearn.incredilearn.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "domain")
    private String domain;
    
    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private User enseignant;
    
    @ManyToMany(mappedBy = "coursesInscrits")
    private Set<User> etudiants = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "duree_heures")
    private Integer dureeHeures;
    
    @Column(name = "niveau")
    private String niveau; // Débutant, Intermédiaire, Avancé
    
    @Column(name = "image_url")
    private String imageUrl;
    
    // Constructeurs
    public Course() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Course(String titre, String description, User enseignant) {
        this.titre = titre;
        this.description = description;
        this.enseignant = enseignant;
        this.createdAt = LocalDateTime.now();
    }
    
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
    
    public User getEnseignant() {
        return enseignant;
    }
    
    public void setEnseignant(User enseignant) {
        this.enseignant = enseignant;
    }
    
    public Set<User> getEtudiants() {
        return etudiants;
    }
    
    public void setEtudiants(Set<User> etudiants) {
        this.etudiants = etudiants;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
