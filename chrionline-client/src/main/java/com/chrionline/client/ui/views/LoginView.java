package com.chrionline.client.ui.views;

import com.chrionline.client.model.NotificationDTO;
import com.chrionline.client.model.LoginCaptchaDTO;
import com.chrionline.client.model.LoginResponseDTO;
import com.chrionline.client.model.UserDTO;
import com.chrionline.client.service.AuthService;
import com.chrionline.client.service.NotificationService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
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

import java.util.List;
import java.util.Optional;

public class LoginView extends HBox {
    private static final int CAPTCHA_THRESHOLD = 3;
    private static final int MAX_LOGIN_ATTEMPTS = 6;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    public LoginView() {
        setSpacing(0);
        setStyle("-fx-background-color: linear-gradient(to right, #173d35, #d48636 74%, #f3ebdf);");

        VBox marketing = new VBox(18);
        marketing.setPadding(new Insets(52));
        marketing.setAlignment(Pos.CENTER_LEFT);
        marketing.setPrefWidth(420);

        Label brand = new Label("ChriOnline");
        brand.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label eyebrow = new Label("ESPACE CLIENT");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 2px; "
                + "-fx-text-fill: #f8dcc0;");
        Label headline = new Label("Reconnectez-vous a une experience plus nette et plus premium.");
        headline.setWrapText(true);
        headline.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #fff6eb;");
        Label text = new Label("Retrouvez votre panier, vos commandes et vos notifications dans une interface plus mode et plus structurée.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #f2dbc4;");

        StackPane art = new StackPane();
        art.setPrefSize(250, 250);
        Rectangle card = new Rectangle(240, 240);
        card.setArcWidth(42);
        card.setArcHeight(42);
        card.setFill(Color.rgb(255, 255, 255, 0.12));
        Circle circle = new Circle(62, Color.rgb(248, 207, 166, 0.94));
        circle.setTranslateX(42);
        circle.setTranslateY(14);
        Rectangle chip = new Rectangle(118, 118);
        chip.setArcWidth(30);
        chip.setArcHeight(30);
        chip.setFill(Color.rgb(255, 250, 244, 0.18));
        chip.setTranslateX(-44);
        chip.setTranslateY(-36);
        Label artLabel = new Label("SIGN\nIN");
        artLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: white;");
        art.getChildren().addAll(card, chip, circle, artLabel);

        VBox bulletCard = new VBox(8);
        bulletCard.setPadding(new Insets(18));
        bulletCard.setStyle(ViewFactory.glassPanelStyle());
        Label bulletTitle = new Label("Ce que vous retrouvez");
        bulletTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label bulletText = new Label("Commandes, profil, panier et suivi de statut reunis dans le meme parcours.");
        bulletText.setWrapText(true);
        bulletText.setStyle("-fx-font-size: 13px; -fx-text-fill: #f3e6d9;");
        bulletCard.getChildren().addAll(bulletTitle, bulletText);

        marketing.getChildren().addAll(brand, eyebrow, headline, text, art, bulletCard);

        VBox form = new VBox(18);
        form.setPadding(new Insets(44));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: linear-gradient(to bottom, #fffaf5, #f5ede4);");
        HBox.setHgrow(form, Priority.ALWAYS);

        VBox formCard = new VBox(16);
        formCard.setMaxWidth(390);
        formCard.setPadding(new Insets(28));
        formCard.setStyle(ViewFactory.elevatedCardStyle());

        Label title = new Label("Connexion");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        Label subtitle = new Label("Accedez a votre espace en quelques secondes.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");
        Label loginAttemptLabel = new Label();
        loginAttemptLabel.setWrapText(true);
        loginAttemptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b2226;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(340);
        emailField.setStyle(ViewFactory.inputStyle());

        PasswordToggleControl passwordField = new PasswordToggleControl("Mot de passe");
        final int[] loginAttempts = {0};
        final String[] lastAttemptEmail = {""};

        Button loginButton = ViewFactory.createPrimaryButton("Se connecter");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> {
            String currentEmail = emailField.getText().trim().toLowerCase();
            if (!currentEmail.equals(lastAttemptEmail[0])) {
                loginAttempts[0] = 0;
                loginAttemptLabel.setText("");
                loginButton.setDisable(false);
                lastAttemptEmail[0] = currentEmail;
            }
            if (loginAttempts[0] >= MAX_LOGIN_ATTEMPTS) {
                UIUtils.showError("Vous avez atteint la limite de tentatives d'authentification.");
                return;
            }
            try {
                String captchaId = "";
                String captchaAnswer = "";
                if (isCaptchaRequired(loginAttempts[0])) {
                    CaptchaResponse captchaResponse = promptForCaptcha(emailField.getText().trim());
                    if (captchaResponse == null) {
                        return;
                    }
                    captchaId = captchaResponse.captchaId();
                    captchaAnswer = captchaResponse.captchaAnswer();
                }

                LoginResponseDTO loginResponse = authService.login(
                        emailField.getText().trim(),
                        passwordField.getValue(),
                        captchaId,
                        captchaAnswer
                );
                UserDTO user = loginResponse.isRequiresOtp()
                        ? showLoginOtpFlow(loginResponse)
                        : loginResponse.getUser();
                if (user == null) {
                    return;
                }
                loginAttempts[0] = 0;
                lastAttemptEmail[0] = currentEmail;
                loginAttemptLabel.setText("");
                loginButton.setDisable(false);
                AppSession.setCurrentUser(user);
                showPendingNotifications();
                if (user.isAdmin()) {
                    NavigationManager.navigateTo(new AdminView());
                } else {
                    NavigationManager.navigateTo(new ProductListView());
                }
            } catch (Exception e) {
                loginAttempts[0]++;
                loginAttemptLabel.setText(formatAttemptMessage("authentification", loginAttempts[0]));
                if (loginAttempts[0] >= MAX_LOGIN_ATTEMPTS) {
                    loginButton.setDisable(true);
                    UIUtils.showError(e.getMessage() + " (" + formatAttemptMessage("authentification", loginAttempts[0]) + "). Limite de tentatives atteinte.");
                } else {
                    UIUtils.showError(e.getMessage() + " (" + formatAttemptMessage("authentification", loginAttempts[0]) + ")");
                }
            }
        });

        Hyperlink registerLink = createLink("Creer un compte", new RegisterView());
        Hyperlink catalogLink = createLink("Voir le catalogue sans connexion", new ProductListView());
        Hyperlink homeLink = createLink("Retour a l'accueil", new HomeView());
        Hyperlink forgotLink = new Hyperlink("Mot de passe oublie ?");
        forgotLink.setOnAction(event -> showForgotPasswordFlow());

        formCard.getChildren().addAll(title, subtitle, emailField, passwordField, loginAttemptLabel, loginButton, registerLink, catalogLink, forgotLink, homeLink);
        form.getChildren().add(formCard);
        getChildren().addAll(marketing, form);
    }

    private void showPendingNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getMyNotifications();
            int shown = 0;
            for (NotificationDTO notification : notifications) {
                if (notification.isLue()) {
                    continue;
                }
                UIUtils.showInfo(notification.getMessage());
                notificationService.markAsRead(notification.getId());
                shown++;
                if (shown >= 5) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("LoginView.showPendingNotifications : " + e.getMessage());
        }
    }

    private Hyperlink createLink(String text, Parent targetView) {
        Hyperlink link = new Hyperlink(text);
        link.setStyle("-fx-text-fill: #1d5f52; -fx-font-size: 13px;");
        link.setOnAction(event -> NavigationManager.navigateTo(targetView));
        return link;
    }

    private void showForgotPasswordFlow() {
        Dialog<ButtonType> emailDialog = new Dialog<>();
        emailDialog.setTitle("Mot de passe oublie");
        DialogPane pane = emailDialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        TextField emailField = new TextField();
        emailField.setPromptText("Votre email");
        emailField.setStyle(ViewFactory.inputStyle());
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
        otpField.setStyle(ViewFactory.inputStyle());
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");
        newPasswordField.setStyle(ViewFactory.inputStyle());
        Label otpAttemptLabel = new Label();
        otpAttemptLabel.setWrapText(true);
        otpAttemptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b2226;");
        otpPane.setContent(new VBox(12,
                new Label("Entrez le code recu puis choisissez un nouveau mot de passe."),
                otpField,
                newPasswordField,
                otpAttemptLabel));

        Button okButton = (Button) otpPane.lookupButton(ButtonType.OK);
        final int[] otpAttempts = {0};
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String passwordError = validatePasswordPolicy(newPasswordField.getText(), emailField.getText().trim());
            if (passwordError != null) {
                UIUtils.showError(passwordError);
                event.consume();
                return;
            }
            if (otpAttempts[0] >= MAX_OTP_ATTEMPTS) {
                UIUtils.showError("Vous avez atteint la limite de 5 essais pour la verification du code.");
                event.consume();
                return;
            }
            try {
                authService.verifyResetOtp(emailField.getText().trim(), otpField.getText().trim());
                UIUtils.showSuccess(authService.resetPassword(emailField.getText().trim(), newPasswordField.getText()));
                otpAttempts[0] = 0;
                otpAttemptLabel.setText("");
            } catch (Exception e) {
                otpAttempts[0]++;
                otpAttemptLabel.setText(formatAttemptMessage("verification du code", otpAttempts[0]));
                if (otpAttempts[0] >= MAX_OTP_ATTEMPTS) {
                    okButton.setDisable(true);
                    UIUtils.showError(e.getMessage() + " (" + formatAttemptMessage("verification du code", otpAttempts[0]) + "). Limite de 5 essais atteinte.");
                } else {
                    UIUtils.showError(e.getMessage() + " (" + formatAttemptMessage("verification du code", otpAttempts[0]) + ")");
                }
                event.consume();
            }
        });

        Optional<ButtonType> secondStep = otpDialog.showAndWait();
        if (secondStep.isEmpty() || secondStep.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
            return;
        }
    }

    private String formatAttemptMessage(String context, int attempts) {
        return "Nombre d'essais " + context + " : " + attempts;
    }

    private boolean isCaptchaRequired(int failedAttempts) {
        return failedAttempts >= CAPTCHA_THRESHOLD - 1;
    }

    private CaptchaResponse promptForCaptcha(String email) throws Exception {
        if (email == null || email.isBlank()) {
            UIUtils.showError("Saisissez l'email avant de valider le CAPTCHA.");
            return null;
        }

        LoginCaptchaDTO captcha = authService.getLoginCaptcha(email.trim());
        Dialog<ButtonType> captchaDialog = new Dialog<>();
        captchaDialog.setTitle("Verification CAPTCHA");
        DialogPane pane = captchaDialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        Label infoLabel = new Label("Pour continuer, resolvez ce challenge.");
        infoLabel.setWrapText(true);
        Label challengeLabel = new Label(captcha.getChallengeText());
        challengeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        TextField answerField = new TextField();
        answerField.setPromptText("Votre reponse");
        answerField.setStyle(ViewFactory.inputStyle());

        pane.setContent(new VBox(12, infoLabel, challengeLabel, answerField));

        Optional<ButtonType> result = captchaDialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return null;
        }
        return new CaptchaResponse(captcha.getCaptchaId(), answerField.getText().trim());
    }

    private UserDTO showLoginOtpFlow(LoginResponseDTO loginResponse) throws Exception {
        UIUtils.showInfo(loginResponse.getMessage());

        Dialog<ButtonType> otpDialog = new Dialog<>();
        otpDialog.setTitle("Double authentification");
        DialogPane pane = otpDialog.getDialogPane();
        ButtonType resendButtonType = new ButtonType("Renvoyer le code", ButtonBar.ButtonData.LEFT);
        pane.getButtonTypes().addAll(ButtonType.OK, resendButtonType, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        Label helpLabel = new Label("Saisissez le code OTP recu par email pour finaliser la connexion.");
        helpLabel.setWrapText(true);
        TextField otpField = new TextField();
        otpField.setPromptText("Code OTP");
        otpField.setStyle(ViewFactory.inputStyle());
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b2226;");
        pane.setContent(new VBox(12, helpLabel, otpField, statusLabel));

        int otpAttempts = 0;
        while (true) {
            Optional<ButtonType> result = otpDialog.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
                return null;
            }
            if (result.get() == resendButtonType) {
                UIUtils.showInfo(authService.resendLoginOtp(loginResponse.getPendingToken()));
                statusLabel.setText("");
                continue;
            }
            try {
                return authService.verifyLoginOtp(loginResponse.getPendingToken(), otpField.getText().trim());
            } catch (Exception e) {
                otpAttempts++;
                statusLabel.setText("Verification OTP echouee : " + otpAttempts);
                UIUtils.showError(e.getMessage());
                if (otpAttempts >= MAX_OTP_ATTEMPTS) {
                    return null;
                }
            }
        }
    }

    private String validatePasswordPolicy(String password, String... forbiddenTerms) {
        if (password == null || password.isBlank()) {
            return "Mot de passe obligatoire.\n\n" + passwordGuidance();
        }
        if (password.length() < 8) {
            return "Mot de passe trop faible: il doit contenir au moins 8 caracteres.\n\n" + passwordGuidance();
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            return "Mot de passe trop faible: ajoutez au moins une lettre majuscule.\n\n" + passwordGuidance();
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            return "Mot de passe trop faible: ajoutez au moins une lettre minuscule.\n\n" + passwordGuidance();
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            return "Mot de passe trop faible: ajoutez au moins un chiffre.\n\n" + passwordGuidance();
        }
        if (password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            return "Mot de passe trop faible: ajoutez au moins un caractere special.\n\n" + passwordGuidance();
        }
        if (containsForbiddenTerm(password, forbiddenTerms)) {
            return "Mot de passe refuse: il ne doit pas contenir votre email, votre nom, votre prenom ou votre date de naissance.\n\n" + passwordGuidance();
        }
        return null;
    }

    private String passwordGuidance() {
        return "Pour corriger le mot de passe, utilisez au minimum 8 caracteres avec 1 majuscule, 1 minuscule, 1 chiffre et 1 caractere special, sans email, nom, prenom ni date de naissance.";
    }

    private boolean containsForbiddenTerm(String password, String... forbiddenTerms) {
        String normalizedPassword = password.toLowerCase();
        for (String term : forbiddenTerms) {
            if (term == null || term.isBlank()) {
                continue;
            }
            String normalizedTerm = term.trim().toLowerCase();
            if (normalizedPassword.contains(normalizedTerm)) {
                return true;
            }
            int atIndex = normalizedTerm.indexOf('@');
            if (atIndex > 0) {
                String localPart = normalizedTerm.substring(0, atIndex);
                if (localPart.length() >= 3 && normalizedPassword.contains(localPart)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class PasswordToggleControl extends StackPane {
        private final PasswordField passwordField = new PasswordField();
        private final TextField visibleField = new TextField();

        private PasswordToggleControl(String prompt) {
            passwordField.setPromptText(prompt);
            visibleField.setPromptText(prompt);
            passwordField.setMaxWidth(340);
            visibleField.setMaxWidth(340);
            passwordField.setStyle(ViewFactory.inputStyle() + "-fx-padding: 13 54 13 14;");
            visibleField.setStyle(ViewFactory.inputStyle() + "-fx-padding: 13 54 13 14;");
            visibleField.setManaged(false);
            visibleField.setVisible(false);

            Button eyeButton = ViewFactory.createSecondaryButton("Voir");
            eyeButton.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-text-fill: #1f6f5f; "
                    + "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 7 12 7 12; -fx-font-size: 12px;");
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
                eyeButton.setText("Voir");
            } else {
                visibleField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visibleField.setVisible(true);
                visibleField.setManaged(true);
                eyeButton.setText("Masquer");
            }
        }

        private String getValue() {
            return visibleField.isVisible() ? visibleField.getText() : passwordField.getText();
        }
    }

    private record CaptchaResponse(String captchaId, String captchaAnswer) {
    }
}
