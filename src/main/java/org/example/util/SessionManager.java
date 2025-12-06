package org.example.util;

import org.example.model.Directeur;

public class SessionManager {
    private static SessionManager instance;
    private Directeur currentDirecteur;

    private SessionManager() {
        // Constructeur privé pour singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Directeur getCurrentDirecteur() {
        return currentDirecteur;
    }

    public void setCurrentDirecteur(Directeur directeur) {
        this.currentDirecteur = directeur;
        System.out.println("✅ Session démarrée pour: " + directeur.getNomDirecteur());
    }

    public void clearSession() {
        if (currentDirecteur != null) {
            System.out.println("👋 Session terminée pour: " + currentDirecteur.getNomDirecteur());
        }
        currentDirecteur = null;
    }

    public boolean isLoggedIn() {
        return currentDirecteur != null;
    }

    public String getNomDirecteur() {
        return currentDirecteur != null ? currentDirecteur.getNomDirecteur() : "Invité";
    }

    public String getNomEcole() {
        return currentDirecteur != null ? currentDirecteur.getNomEcole() : "Non connecté";
    }

    public int getDirecteurId() {
        return currentDirecteur != null ? currentDirecteur.getId() : -1;
    }
}