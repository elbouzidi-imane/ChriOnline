package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;

public class ProfileView extends VBox {
    private final AuthService authService = new AuthService();

    public ProfileView() {
        if (!AppSession.isLoggedIn()) {
            NavigationManager.navigateTo(new LoginView());
            return;
        }

        setSpacing(18);
        setPadding(new Insets(22));
        setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe5ca 70%, #edf8f3);");

        Label title = new Label("Mon profil");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
        Label subtitle = new Label("Mettez a jour vos informations ou gerez votre compte.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");

        Button backButton = ViewFactory.createSecondaryButton("Retour au catalogue");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));

        TextField nomField = createField("Nom", AppSession.getCurrentUser().getNom());
        TextField prenomField = createField("Prenom", AppSession.getCurrentUser().getPrenom());
        TextField telephoneField = createField("Telephone", AppSession.getCurrentUser().getTelephone());
        TextField adresseField = createField("Adresse", AppSession.getCurrentUser().getAdresse());
        String dateNaissance = AppSession.getCurrentUser().getDateNaissance() == null
                ? ""
                : new SimpleDateFormat("yyyy-MM-dd").format(AppSession.getCurrentUser().getDateNaissance());
        TextField birthField = createField("Date de naissance (YYYY-MM-DD)", dateNaissance);
        CheckBox notificationsBox = new CheckBox("Recevoir un email quand le statut de mes commandes change");
        notificationsBox.setSelected(AppSession.getCurrentUser().isNotificationsActivees());

        Button updateButton = ViewFactory.createPrimaryButton("Mettre a jour");
        updateButton.setOnAction(event -> {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String telephone = telephoneField.getText().trim();
                String adresse = adresseField.getText().trim();
                String dateNaissanceValue = birthField.getText().trim();

                UIUtils.showSuccess(authService.updateProfile(
                        AppSession.getCurrentUser().getId(),
                        nom,
                        prenom,
                        telephone,
                        adresse,
                        dateNaissanceValue
                ));
                authService.updateNotificationPreference(
                        AppSession.getCurrentUser().getId(),
                        notificationsBox.isSelected()
                );
                AppSession.getCurrentUser().setNom(nom);
                AppSession.getCurrentUser().setPrenom(prenom);
                AppSession.getCurrentUser().setTelephone(telephone);
                AppSession.getCurrentUser().setAdresse(adresse);
                if (dateNaissanceValue.isBlank()) {
                    AppSession.getCurrentUser().setDateNaissance(null);
                } else {
                    AppSession.getCurrentUser().setDateNaissance(java.sql.Date.valueOf(dateNaissanceValue));
                }
                AppSession.getCurrentUser().setNotificationsActivees(notificationsBox.isSelected());
                UIUtils.showInfo("Le profil local a ete mis a jour.");
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Button deactivateButton = ViewFactory.createSecondaryButton("Desactiver le compte");
        deactivateButton.setOnAction(event -> {
            try {
                if (!UIUtils.confirm("Confirmation", "Desactiver votre compte ?")) {
                    return;
                }
                UIUtils.showSuccess(authService.deactivateAccount(AppSession.getCurrentUser().getId()));
                AppSession.clear();
                NavigationManager.navigateTo(new HomeView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Button deleteButton = ViewFactory.createSecondaryButton("Supprimer definitivement");
        deleteButton.setOnAction(event -> {
            try {
                if (!UIUtils.confirm("Confirmation", "Supprimer definitivement votre compte ?")) {
                    return;
                }
                UIUtils.showSuccess(authService.deleteAccount(AppSession.getCurrentUser().getId()));
                AppSession.clear();
                NavigationManager.navigateTo(new HomeView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        HBox actions = new HBox(10, updateButton, deactivateButton, deleteButton);

        VBox card = new VBox(12,
                new Label("Informations personnelles"),
                nomField, prenomField, telephoneField, adresseField, birthField, notificationsBox, actions);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());

        getChildren().addAll(title, subtitle, backButton, card);
    }

    private TextField createField(String prompt, String value) {
        TextField field = new TextField(value == null ? "" : value);
        field.setPromptText(prompt);
        field.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        return field;
    }
}
