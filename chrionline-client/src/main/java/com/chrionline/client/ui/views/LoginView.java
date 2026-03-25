package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.session.AppSession;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

public class LoginView extends HBox {
    private final AuthService authService = new AuthService();

    public LoginView() {
        setSpacing(0);
        setStyle("-fx-background-color: linear-gradient(to right, #144d43, #d97706);");

        VBox marketing = new VBox(16);
        marketing.setPadding(new Insets(48));
        marketing.setAlignment(Pos.CENTER_LEFT);
        marketing.setPrefWidth(390);

        Label brand = new Label("ChriOnline");
        brand.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label headline = new Label("Connectez-vous ou recuperez l'acces a votre compte.");
        headline.setWrapText(true);
        headline.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #fff7ed;");
        Label text = new Label("Le client supporte maintenant le mot de passe oublie et la verification OTP cote serveur.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #fde7cf;");

        StackPane art = new StackPane();
        Rectangle card = new Rectangle(220, 220);
        card.setArcWidth(36);
        card.setArcHeight(36);
        card.setFill(Color.rgb(255, 255, 255, 0.18));
        Circle circle = new Circle(58, Color.rgb(255, 216, 181, 0.85));
        art.getChildren().addAll(card, circle);

        marketing.getChildren().addAll(brand, headline, text, art);

        VBox form = new VBox(16);
        form.setPadding(new Insets(42));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: #fffaf5;");
        HBox.setHgrow(form, Priority.ALWAYS);

        Label title = new Label("Connexion");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(340);
        emailField.setStyle("-fx-background-radius: 14; -fx-padding: 12;");

        PasswordToggleControl passwordField = new PasswordToggleControl("Mot de passe");

        Button loginButton = ViewFactory.createPrimaryButton("Se connecter");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> {
            try {
                var user = authService.login(emailField.getText().trim(), passwordField.getValue());
                AppSession.setCurrentUser(user);
                if (user.isAdmin()) {
                    NavigationManager.navigateTo(new AdminView());
                } else {
                    NavigationManager.navigateTo(new ProductListView());
                }
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Hyperlink registerLink = new Hyperlink("Creer un compte");
        registerLink.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));
        Hyperlink catalogLink = new Hyperlink("Voir le catalogue sans connexion");
        catalogLink.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        Hyperlink forgotLink = new Hyperlink("Mot de passe oublie ?");
        forgotLink.setOnAction(event -> showForgotPasswordFlow());
        Hyperlink homeLink = new Hyperlink("Retour a l'accueil");
        homeLink.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        form.getChildren().addAll(title, emailField, passwordField, loginButton, registerLink, catalogLink, forgotLink, homeLink);
        getChildren().addAll(marketing, form);
    }

    private void showForgotPasswordFlow() {
        Dialog<ButtonType> emailDialog = new Dialog<>();
        emailDialog.setTitle("Mot de passe oublie");
        DialogPane pane = emailDialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        TextField emailField = new TextField();
        emailField.setPromptText("Votre email");
        pane.setContent(new VBox(12, new Label("Saisissez votre email pour recevoir un code OTP."), emailField));

        Optional<ButtonType> firstStep = emailDialog.showAndWait();
        if (firstStep.isEmpty() || firstStep.get() != ButtonType.OK) {
            return;
        }

        try {
            UIUtils.showInfo(authService.forgotPassword(emailField.getText().trim()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
            return;
        }

        Dialog<ButtonType> otpDialog = new Dialog<>();
        otpDialog.setTitle("Verification OTP");
        DialogPane otpPane = otpDialog.getDialogPane();
        otpPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        otpPane.setStyle("-fx-background-color: #fffaf5;");

        TextField otpField = new TextField();
        otpField.setPromptText("Code OTP");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");
        otpPane.setContent(new VBox(12,
                new Label("Entrez le code recu puis choisissez un nouveau mot de passe."),
                otpField,
                newPasswordField));

        Optional<ButtonType> secondStep = otpDialog.showAndWait();
        if (secondStep.isEmpty() || secondStep.get() != ButtonType.OK) {
            return;
        }

        try {
            authService.verifyResetOtp(emailField.getText().trim(), otpField.getText().trim());
            UIUtils.showSuccess(authService.resetPassword(emailField.getText().trim(), newPasswordField.getText()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private static final class PasswordToggleControl extends StackPane {
        private final PasswordField passwordField = new PasswordField();
        private final TextField visibleField = new TextField();

        private PasswordToggleControl(String prompt) {
            passwordField.setPromptText(prompt);
            visibleField.setPromptText(prompt);
            passwordField.setMaxWidth(340);
            visibleField.setMaxWidth(340);
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
