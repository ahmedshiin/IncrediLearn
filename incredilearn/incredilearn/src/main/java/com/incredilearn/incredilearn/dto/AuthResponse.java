package com.incredilearn.incredilearn.dto;

public class AuthResponse {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private String domain;
    private boolean isVerified;
    private String message;
    
    // Constructeurs
    public AuthResponse() {}
    
    public AuthResponse(Long id, String nom, String email, String role, String message) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.message = message;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
