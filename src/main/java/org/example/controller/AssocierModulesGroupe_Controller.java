package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Groupe;
import org.example.model.Module;
import org.example.util.SessionManager;

import java.util.List;

public class AssocierModulesGroupe_Controller {

    @FXML private ComboBox<Groupe> groupeComboBox;
    @FXML private ListView<Module> modulesDisponiblesList;
    @FXML private ListView<Module> modulesAssociesList;
    @FXML private Button ajouterButton;
    @FXML private Button retirerButton;
    @FXML private Button validerButton;

    private GroupeDAO groupeDAO;
    private ModuleDAO moduleDAO;
    private ObservableList<Module> modulesDisponibles = FXCollections.observableArrayList();
    private ObservableList<Module> modulesAssocies = FXCollections.observableArrayList();
    private Groupe groupeSelectionne;

    @FXML
    public void initialize() {
        groupeDAO = new GroupeDAO();
        moduleDAO = new ModuleDAO();

        // Charger les groupes du directeur
        chargerGroupes();

        // Configurer les événements
        configurerEvenements();
    }

    private void chargerGroupes() {
        int directeurId = SessionManager.getInstance().getUserId();
        List<Groupe> groupes = groupeDAO.findByDirecteurId(directeurId);

        groupeComboBox.getItems().setAll(groupes);
        groupeComboBox.setCellFactory(param -> new ListCell<Groupe>() {
            @Override
            protected void updateItem(Groupe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatricule() + " - " + item.getNiveau().getValeur());
                }
            }
        });

        groupeComboBox.setButtonCell(new ListCell<Groupe>() {
            @Override
            protected void updateItem(Groupe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionner un groupe");
                } else {
                    setText(item.getMatricule() + " - " + item.getNiveau().getValeur());
                }
            }
        });
    }

    private void configurerEvenements() {
        // Quand un groupe est sélectionné
        groupeComboBox.setOnAction(event -> {
            groupeSelectionne = groupeComboBox.getValue();
            if (groupeSelectionne != null) {
                chargerModules();
            }
        });

        // Bouton Ajouter
        ajouterButton.setOnAction(event -> {
            Module module = modulesDisponiblesList.getSelectionModel().getSelectedItem();
            if (module != null && groupeSelectionne != null) {
                modulesDisponibles.remove(module);
                modulesAssocies.add(module);
            }
        });

        // Bouton Retirer
        retirerButton.setOnAction(event -> {
            Module module = modulesAssociesList.getSelectionModel().getSelectedItem();
            if (module != null && groupeSelectionne != null) {
                modulesAssocies.remove(module);
                modulesDisponibles.add(module);
            }
        });

        // Bouton Valider
        validerButton.setOnAction(event -> {
            if (groupeSelectionne != null) {
                sauvegarderAssociations();
            }
        });
    }

    private void chargerModules() {
        if (groupeSelectionne == null) return;

        int directeurId = SessionManager.getInstance().getUserId();

        // Charger les modules disponibles
        List<Module> modulesDisponiblesListe = moduleDAO.findAvailableModulesForGroupe(
                directeurId, groupeSelectionne.getId()
        );

        // Charger les modules déjà associés
        List<Module> modulesAssociesListe = moduleDAO.findByGroupeId(groupeSelectionne.getId());

        modulesDisponibles.setAll(modulesDisponiblesListe);
        modulesAssocies.setAll(modulesAssociesListe);

        // Configurer l'affichage des modules
        modulesDisponiblesList.setCellFactory(param -> new ListCell<Module>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatricule() + " - " + item.getNom());
                }
            }
        });

        modulesAssociesList.setCellFactory(param -> new ListCell<Module>() {
            @Override
            protected void updateItem(Module item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMatricule() + " - " + item.getNom());
                }
            }
        });

        modulesDisponiblesList.setItems(modulesDisponibles);
        modulesAssociesList.setItems(modulesAssocies);
    }

    private void sauvegarderAssociations() {
        try {
            // Supprimer toutes les associations existantes
            List<Module> anciensModules = moduleDAO.findByGroupeId(groupeSelectionne.getId());
            for (Module module : anciensModules) {
                moduleDAO.removeModuleFromGroupe(groupeSelectionne.getId(), module.getId());
            }

            // Ajouter les nouvelles associations
            for (Module module : modulesAssocies) {
                moduleDAO.addModuleToGroupe(groupeSelectionne.getId(), module.getId());
            }

            showAlert("Succès", "Associations sauvegardées",
                    "Les modules ont été associés au groupe avec succès.",
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Sauvegarde échouée",
                    "Erreur lors de la sauvegarde des associations: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}