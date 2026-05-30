package com.campconnect.userservice.dto;

import lombok.Data;

@Data
public class CampSiteResponse {
    private Long id;
    private String nom;
    private String description;
    private String localisation;
    private Integer capacite;
    private Double prixParNuit;
    private String statut;
}