package com.incredilearn.incredilearn.dto;

public class CreateCourseRequest {
    private String titre;
    private String description;
    private String domain;
    private String niveau;
    private Integer dureeHeures;
    private Long enseignantId;

    public CreateCourseRequest() {}

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public Integer getDureeHeures() { return dureeHeures; }
    public void setDureeHeures(Integer dureeHeures) { this.dureeHeures = dureeHeures; }

    public Long getEnseignantId() { return enseignantId; }
    public void setEnseignantId(Long enseignantId) { this.enseignantId = enseignantId; }
}
