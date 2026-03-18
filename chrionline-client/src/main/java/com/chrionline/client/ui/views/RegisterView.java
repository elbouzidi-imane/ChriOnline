package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RegisterView extends HBox {
    private final AuthService authService = new AuthService();

    public RegisterView() {
        setStyle("-fx-background-color: linear-gradient(to right, #fff5e6, #ffe0bf);");

        VBox intro = new VBox(14);
        intro.setPadding(new Insets(42));
        intro.setAlignment(Pos.CENTER_LEFT);
        intro.setPrefWidth(360);
        intro.setStyle("-fx-background-color: #1f6f5f;");

        Label brand = new Label("ChriOnline");
        brand.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label headline = new Label("Creez votre espace client mode.");
        headline.setWrapText(true);
        headline.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #fff7ed;");
        Label text = new Label("Inscrivez-vous pour suivre vos commandes, gerer votre panier et decouvrir toutes les collections.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #dff5ef;");

        intro.getChildren().addAll(brand, headline, text);

        VBox form = new VBox(12);
        form.setPadding(new Insets(34));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setStyle("-fx-background-color: #fffaf5;");
        HBox.setHgrow(form, Priority.ALWAYS);

        Label title = new Label("Creer un compte");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        TextField nomField = createField("Nom");
        TextField prenomField = createField("Prenom");
        TextField emailField = createField("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(380);
        passwordField.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        TextField telField = createField("Telephone");
        TextField adresseField = createField("Adresse");

        Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
        registerButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                    || email.isBlank() || passwordField.getText().isBlank()) {
                UIUtils.showError("Tous les champs obligatoires doivent etre remplis.");
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                UIUtils.showError("Format d'email invalide.");
                return;
            }
            try {
                authService.register(
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        email,
                        passwordField.getText(),
                        telField.getText().trim(),
                        adresseField.getText().trim()
                );
                UIUtils.showInfo("Inscription reussie. Connectez-vous.");
                NavigationManager.navigateTo(new LoginView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Hyperlink backLink = new Hyperlink("Retour a la connexion");
        backLink.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Hyperlink homeLink = new Hyperlink("Retour a l'accueil");
        homeLink.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        form.getChildren().addAll(title, nomField, prenomField, emailField, passwordField, telField, adresseField, registerButton, backLink, homeLink);
        getChildren().addAll(intro, form);
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(380);
        field.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        return field;
    }
}
