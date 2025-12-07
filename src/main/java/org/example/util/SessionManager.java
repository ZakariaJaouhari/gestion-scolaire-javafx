package org.example.util;

import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Formateur;

public class SessionManager {
    private static SessionManager instance;
    private Object currentUser;
    private UserType userType;

    public String getNomDirecteur() {
        Directeur directeur = getCurrentDirecteur();
        return directeur != null ? directeur.getNomDirecteur() : "";
    }



    public enum UserType {
        DIRECTEUR,
        FORMATEUR,
        ETUDIANT
    }

    private SessionManager() {
        // Constructeur privé pour singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getters et Setters
    public Object getCurrentUser() {
        return currentUser;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setCurrentDirecteur(Directeur directeur) {
        this.currentUser = directeur;
        this.userType = UserType.DIRECTEUR;
        System.out.println("✅ Session directeur démarrée pour: " + directeur.getNomDirecteur());
    }

    public void setCurrentFormateur(Formateur formateur) {
        this.currentUser = formateur;
        this.userType = UserType.FORMATEUR;
        System.out.println("✅ Session formateur démarrée pour: " + formateur.getNomComplet());
    }

    public void setCurrentEtudiant(Etudiant etudiant) {
        this.currentUser = etudiant;
        this.userType = UserType.ETUDIANT;
        System.out.println("✅ Session etudiant démarrée pour: " + etudiant.getNomComplet());
    }

    public void clearSession() {
        if (currentUser != null) {
            if (userType == UserType.DIRECTEUR) {
                System.out.println("👋 Session directeur terminée pour: " + ((Directeur) currentUser).getNomDirecteur());
            } else if (userType == UserType.FORMATEUR) {
                System.out.println("👋 Session formateur terminée pour: " + ((Formateur) currentUser).getNomComplet());
            } else if (userType == UserType.ETUDIANT) {
                System.out.println("👋 Session ETUDIANT terminée pour: " + ((Etudiant) currentUser).getNomComplet());
            }
        }
        currentUser = null;
        userType = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isDirecteur() {
        return isLoggedIn() && userType == UserType.DIRECTEUR;
    }

    public boolean isFormateur() {
        return isLoggedIn() && userType == UserType.FORMATEUR;
    }

    public boolean isEtudiant() {
        return isLoggedIn() && userType == UserType.ETUDIANT;
    }

    // Méthodes pour obtenir les informations
    public String getNomUtilisateur() {
        if (currentUser == null) return "Invité";

        if (userType == UserType.DIRECTEUR) {
            return ((Directeur) currentUser).getNomDirecteur();
        } else if (userType == UserType.FORMATEUR) {
            return ((Formateur) currentUser).getNomComplet();
        } else if (userType == UserType.ETUDIANT) {
            return ((Etudiant) currentUser).getNomComplet();
        }
        return "Invité";
    }

    public String getNomEcole() {
        if (currentUser == null) return "Non connecté";

        if (userType == UserType.DIRECTEUR) {
            return ((Directeur) currentUser).getNomEcole();
        } else if (userType == UserType.FORMATEUR) {
            // Pour un formateur, vous pourriez récupérer le nom de l'école
            // via une requête SQL supplémentaire
            return "École de formation";
        }
        return "Non connecté";
    }

    public int getUserId() {
        if (currentUser == null) return -1;

        if (userType == UserType.DIRECTEUR) {
            return ((Directeur) currentUser).getId();
        } else if (userType == UserType.FORMATEUR) {
            return ((Formateur) currentUser).getId();
        } else if (userType == UserType.ETUDIANT) {
            return ((Etudiant) currentUser).getId();
        }
        return -1;
    }

    // Méthodes spécifiques pour obtenir l'utilisateur typé
    public Directeur getCurrentDirecteur() {
        return isDirecteur() ? (Directeur) currentUser : null;
    }

    public Formateur getCurrentFormateur() {
        return isFormateur() ? (Formateur) currentUser : null;
    }

    public Etudiant getCurrentEtudiant() {
        return isEtudiant() ? (Etudiant) currentUser : null;
    }


}