package com.incredilearn.incredilearn.dto;

public class RegisterRequest {
    private String nom;
    private String email;
    private String password;
    private String role; // "etudiant" ou "enseignant"
    private String domain; // Pour les enseignants
    
    // Constructeurs
    public RegisterRequest() {}
    
    public RegisterRequest(String nom, String email, String password, String role) {
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    // Getters et Setters
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
}
