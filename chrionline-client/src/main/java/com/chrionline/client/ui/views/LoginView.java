package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

public class LoginView extends HBox {

    private final AuthService authService = new AuthService();

    public LoginView() {
        setSpacing(0);
        setStyle("-fx-background-color: linear-gradient(to right, #144d43, #d97706);");

        // ───── LEFT (marketing) ─────
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

        // ───── RIGHT (form) ─────
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

        // ───── PASSWORD AVEC OEIL 👁 ─────
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Mot de passe");

        // Synchronisation
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Bouton oeil
        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        final boolean[] showing = {false};

        toggleBtn.setOnAction(e -> {
            showing[0] = !showing[0];

            if (showing[0]) {
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);

                passwordField.setVisible(false);
                passwordField.setManaged(false);

                toggleBtn.setText("🙈");
            } else {
                passwordField.setVisible(true);
                passwordField.setManaged(true);

                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                toggleBtn.setText("👁");
            }
        });

        passwordField.setMaxWidth(300);
        visiblePasswordField.setMaxWidth(300);

        passwordField.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        visiblePasswordField.setStyle("-fx-background-radius: 14; -fx-padding: 12;");

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        HBox passwordBox = new HBox(8, passwordField, visiblePasswordField, toggleBtn);
        passwordBox.setAlignment(Pos.CENTER);

        // ───── LOGIN BUTTON ─────
        Button loginButton = ViewFactory.createPrimaryButton("Se connecter");
        loginButton.setDefaultButton(true);

        loginButton.setOnAction(event -> {
            try {
                String password = showing[0]
                        ? visiblePasswordField.getText()
                        : passwordField.getText();

                var user = authService.login(
                        emailField.getText().trim(),
                        password
                );

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

        // ───── LINKS ─────
        Hyperlink registerLink = new Hyperlink("Creer un compte");
        registerLink.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

        Hyperlink catalogLink = new Hyperlink("Voir le catalogue sans connexion");
        catalogLink.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));

        Hyperlink forgotLink = new Hyperlink("Mot de passe oublie ?");
        forgotLink.setOnAction(event -> showForgotPasswordFlow());

        Hyperlink homeLink = new Hyperlink("Retour a l'accueil");
        homeLink.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        form.getChildren().addAll(
                title,
                emailField,
                passwordBox,
                loginButton,
                registerLink,
                catalogLink,
                forgotLink,
                homeLink
        );

        getChildren().addAll(marketing, form);
    }

    // ───── FORGOT PASSWORD FLOW ─────
    private void showForgotPasswordFlow() {
        Dialog<ButtonType> emailDialog = new Dialog<>();
        emailDialog.setTitle("Mot de passe oublie");

        DialogPane pane = emailDialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        TextField emailField = new TextField();
        emailField.setPromptText("Votre email");

        pane.setContent(new VBox(12,
                new Label("Saisissez votre email pour recevoir un code OTP."),
                emailField));

        Optional<ButtonType> firstStep = emailDialog.showAndWait();
        if (firstStep.isEmpty() || firstStep.get() != ButtonType.OK) return;

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
        if (secondStep.isEmpty() || secondStep.get() != ButtonType.OK) return;

        try {
            authService.verifyResetOtp(emailField.getText().trim(), otpField.getText().trim());
            UIUtils.showSuccess(authService.resetPassword(
                    emailField.getText().trim(),
                    newPasswordField.getText()
            ));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }
}