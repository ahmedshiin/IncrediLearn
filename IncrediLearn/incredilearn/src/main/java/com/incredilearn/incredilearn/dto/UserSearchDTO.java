package com.incredilearn.incredilearn.dto;

public class UserSearchDTO {
    private Long id;
    private String nom;
    private String role;

    // Constructeurs
    public UserSearchDTO() {}

    public UserSearchDTO(Long id, String nom, String role) {
        this.id = id;
        this.nom = nom;
        this.role = role;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
