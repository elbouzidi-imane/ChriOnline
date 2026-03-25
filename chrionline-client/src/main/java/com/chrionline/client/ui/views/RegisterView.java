package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

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
        Label text = new Label("Inscrivez-vous puis confirmez votre email avec un code OTP envoye par le serveur.");
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
        PasswordToggleControl passwordControl = new PasswordToggleControl("Mot de passe");
        PasswordToggleControl confirmControl = new PasswordToggleControl("Confirmer le mot de passe");
        TextField telField = createField("Telephone");
        TextField adresseField = createField("Adresse");
        TextField birthField = createField("Date de naissance (YYYY-MM-DD)");

        Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
        registerButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String password = passwordControl.getValue();
            String confirmation = confirmControl.getValue();

            if (nomField.getText().isBlank() || prenomField.getText().isBlank()
                    || email.isBlank() || password.isBlank() || confirmation.isBlank()) {
                UIUtils.showError("Tous les champs obligatoires doivent etre remplis.");
                return;
            }
            if (!password.equals(confirmation)) {
                UIUtils.showError("La confirmation du mot de passe ne correspond pas.");
                return;
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                UIUtils.showError("Format d'email invalide.");
                return;
            }
            try {
                String message = authService.register(
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        email,
                        password,
                        telField.getText().trim(),
                        adresseField.getText().trim(),
                        birthField.getText().trim()
                );
                UIUtils.showInfo(message);
                showEmailVerificationDialog(email);
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Hyperlink backLink = new Hyperlink("Retour a la connexion");
        backLink.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Hyperlink homeLink = new Hyperlink("Retour a l'accueil");
        homeLink.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        form.getChildren().addAll(
                title, nomField, prenomField, emailField,
                passwordControl, confirmControl,
                telField, adresseField, birthField,
                registerButton, backLink, homeLink
        );
        getChildren().addAll(intro, form);
    }

    private void showEmailVerificationDialog(String email) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmation email");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        Label info = new Label("Entrez le code OTP envoye a " + email);
        TextField codeField = createField("Code OTP");
        Hyperlink resendLink = new Hyperlink("Renvoyer le code");
        resendLink.setOnAction(event -> {
            try {
                UIUtils.showInfo(authService.resendOtp(email, "REGISTER"));
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        VBox content = new VBox(12, info, codeField, resendLink);
        pane.setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UIUtils.showSuccess(authService.verifyEmail(email, codeField.getText().trim()));
                NavigationManager.navigateTo(new LoginView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        }
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(380);
        field.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        return field;
    }

    private static final class PasswordToggleControl extends StackPane {
        private final PasswordField passwordField = new PasswordField();
        private final TextField visibleField = new TextField();

        private PasswordToggleControl(String prompt) {
            passwordField.setPromptText(prompt);
            visibleField.setPromptText(prompt);
            passwordField.setMaxWidth(380);
            visibleField.setMaxWidth(380);
            passwordField.setStyle("-fx-background-radius: 14; -fx-padding: 12 52 12 12;");
            visibleField.setStyle("-fx-background-radius: 14; -fx-padding: 12 52 12 12;");
            visibleField.setManaged(false);
            visibleField.setVisible(false);

            Button eyeButton = ViewFactory.createSecondaryButton("\uD83D\uDC41");
            eyeButton.setStyle("-fx-background-color: white; -fx-text-fill: #1f6f5f; -fx-font-weight: bold; "
                    + "-fx-background-radius: 12; -fx-padding: 6 11 6 11; -fx-font-size: 15px;");
            eyeButton.setOnAction(event -> toggleVisibility(eyeButton));

            StackPane.setAlignment(eyeButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(eyeButton, new Insets(0, 8, 0, 0));
            getChildren().addAll(passwordField, visibleField, eyeButton);
        }

        private void toggleVisibility(Button eyeButton) {
            if (visibleField.isVisible()) {
                passwordField.setText(visibleField.getText());
                visibleField.setVisible(false);
                visibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                eyeButton.setStyle("-fx-background-color: white; -fx-text-fill: #1f6f5f; -fx-font-weight: bold; "
                        + "-fx-background-radius: 12; -fx-padding: 6 11 6 11; -fx-font-size: 15px;");
            } else {
                visibleField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visibleField.setVisible(true);
                visibleField.setManaged(true);
                eyeButton.setStyle("-fx-background-color: #daf1e9; -fx-text-fill: #1f6f5f; -fx-font-weight: bold; "
                        + "-fx-background-radius: 12; -fx-padding: 6 11 6 11; -fx-font-size: 15px;");
            }
        }

        private String getValue() {
            return visibleField.isVisible() ? visibleField.getText() : passwordField.getText();
        }
    }
}
