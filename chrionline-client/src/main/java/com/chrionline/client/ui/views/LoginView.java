package com.chrionline.client.ui.views;

import com.chrionline.client.model.NotificationDTO;
import com.chrionline.client.service.AuthService;
import com.chrionline.client.service.NotificationService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(340);
        emailField.setStyle(ViewFactory.inputStyle());

        PasswordToggleControl passwordField = new PasswordToggleControl("Mot de passe");

        Button loginButton = ViewFactory.createPrimaryButton("Se connecter");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> {
            try {
                var user = authService.login(emailField.getText().trim(), passwordField.getValue());
                AppSession.setCurrentUser(user);
                showPendingNotifications();
                if (user.isAdmin()) {
                    NavigationManager.navigateTo(new AdminView());
                } else {
                    NavigationManager.navigateTo(new ProductListView());
                }
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Hyperlink registerLink = createLink("Creer un compte", new RegisterView());
        Hyperlink catalogLink = createLink("Voir le catalogue sans connexion", new ProductListView());
        Hyperlink homeLink = createLink("Retour a l'accueil", new HomeView());
        Hyperlink forgotLink = new Hyperlink("Mot de passe oublie ?");
        forgotLink.setOnAction(event -> showForgotPasswordFlow());

        formCard.getChildren().addAll(title, subtitle, emailField, passwordField, loginButton, registerLink, catalogLink, forgotLink, homeLink);
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
}
