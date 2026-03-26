package com.chrionline.client.ui.views;

import com.chrionline.client.service.AuthService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegisterView extends ScrollPane {
    private static final Map<String, List<String>> REGION_CITIES = createRegionCities();

    private final AuthService authService = new AuthService();

    public RegisterView() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: linear-gradient(to right, #efe2d0, #f6eee6);");

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: #efe2d0; -fx-background-color: #efe2d0;");
        setContent(root);

        VBox intro = new VBox(18);
        intro.setPadding(new Insets(44));
        intro.setAlignment(Pos.CENTER_LEFT);
        intro.setPrefWidth(380);
        intro.setStyle("-fx-background-color: linear-gradient(to bottom, #1a5a4d, #0f352e);");

        Label brand = new Label("ChriOnline");
        brand.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label overline = new Label("INSCRIPTION");
        overline.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 2px; -fx-text-fill: #f2d9bd;");
        Label headline = new Label("Creez un compte qui vous suit du catalogue jusqu'a la livraison.");
        headline.setWrapText(true);
        headline.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #fff7ed;");
        Label text = new Label("L'inscription a ete repensee pour etre plus claire, plus chic et plus simple a completer.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #d9efe6;");

        VBox bullets = new VBox(10,
                createIntroPoint("Verification OTP par email"),
                createIntroPoint("Adresse structuree par region et ville"),
                createIntroPoint("Acces direct au panier, profil et commandes"));

        Button loginIntroButton = ViewFactory.createSecondaryButton("J'ai deja un compte");
        loginIntroButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));

        intro.getChildren().addAll(brand, overline, headline, text, bullets, loginIntroButton);

        VBox formWrap = new VBox();
        formWrap.setPadding(new Insets(34));
        formWrap.setAlignment(Pos.TOP_CENTER);
        formWrap.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f1e8, #f4ece2);");
        HBox.setHgrow(formWrap, Priority.ALWAYS);

        VBox form = new VBox(12);
        form.setMaxWidth(520);
        form.setPadding(new Insets(30));
        form.setStyle(ViewFactory.elevatedCardStyle());

        Label title = new Label("Creer un compte");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        Label subtitle = new Label("Renseignez vos informations puis confirmez votre email pour activer le compte.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");
        subtitle.setWrapText(true);

        TextField nomField = createField("Nom");
        TextField prenomField = createField("Prenom");
        TextField emailField = createField("Email");
        TextField telField = createField("Telephone");
        PasswordToggleControl passwordControl = new PasswordToggleControl("Mot de passe");
        PasswordToggleControl confirmControl = new PasswordToggleControl("Confirmer le mot de passe");

        ComboBox<Integer> dayBox = createNumberBox("Jour", 1, 31);
        ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12)
                        .mapToObj(month -> Month.of(month).getDisplayName(TextStyle.FULL, Locale.FRENCH))
                        .collect(Collectors.toList())
        ));
        monthBox.setPromptText("Mois");
        styleCombo(monthBox);

        ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(
                IntStream.iterate(LocalDate.now().getYear() - 18, year -> year - 1)
                        .limit(80)
                        .boxed()
                        .collect(Collectors.toList())
        ));
        yearBox.setPromptText("Annee");
        styleCombo(yearBox);

        HBox birthRow = new HBox(10, dayBox, monthBox, yearBox);
        birthRow.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> regionBox = new ComboBox<>(FXCollections.observableArrayList(REGION_CITIES.keySet()));
        regionBox.setPromptText("Region");
        styleCombo(regionBox);

        ComboBox<String> cityBox = new ComboBox<>();
        cityBox.setPromptText("Ville");
        styleCombo(cityBox);
        cityBox.setDisable(true);

        regionBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            cityBox.getItems().setAll(newValue == null ? List.of() : REGION_CITIES.getOrDefault(newValue, List.of()));
            cityBox.getSelectionModel().clearSelection();
            cityBox.setDisable(newValue == null);
        });

        GridPane locationRow = new GridPane();
        locationRow.setHgap(10);
        locationRow.setVgap(8);
        locationRow.setMaxWidth(Double.MAX_VALUE);
        locationRow.add(new Label("Region"), 0, 0);
        locationRow.add(new Label("Ville"), 1, 0);
        locationRow.add(regionBox, 0, 1);
        locationRow.add(cityBox, 1, 1);
        GridPane.setHgrow(regionBox, Priority.ALWAYS);
        GridPane.setHgrow(cityBox, Priority.ALWAYS);

        TextField addressLineField = createField("Adresse de maison, rue, appartement...");

        Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> {
            String email = normalizeEmail(emailField.getText());
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
            if (!isValidEmail(email)) {
                UIUtils.showError("Format d'email invalide.");
                return;
            }
            if (regionBox.getValue() == null || cityBox.getValue() == null || addressLineField.getText().isBlank()) {
                UIUtils.showError("Selectionnez une region, une ville et completez l'adresse.");
                return;
            }
            if (dayBox.getValue() == null || monthBox.getValue() == null || yearBox.getValue() == null) {
                UIUtils.showError("Selectionnez votre jour, mois et annee de naissance.");
                return;
            }

            String birthDate = formatBirthDate(dayBox.getValue(), monthBox.getValue(), yearBox.getValue());
            String address = cityBox.getValue() + ", " + regionBox.getValue() + " - " + addressLineField.getText().trim();

            try {
                String message = authService.register(
                        nomField.getText().trim(),
                        prenomField.getText().trim(),
                        email,
                        password,
                        telField.getText().trim(),
                        address,
                        birthDate
                );
                UIUtils.showInfo(message);
                showEmailVerificationDialog(email);
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        Button loginButton = ViewFactory.createSecondaryButton("Se connecter");
        loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));

        Hyperlink homeLink = new Hyperlink("Retour a l'accueil");
        homeLink.setStyle("-fx-text-fill: #1d5f52; -fx-font-size: 13px;");
        homeLink.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        form.getChildren().addAll(
                title, subtitle,
                nomField, prenomField, emailField, telField,
                passwordControl, confirmControl,
                createSectionLabel("Date de naissance"),
                birthRow,
                createSectionLabel("Adresse"),
                locationRow,
                addressLineField,
                registerButton, loginButton, homeLink
        );
        formWrap.getChildren().add(form);
        root.getChildren().addAll(intro, formWrap);
    }

    private Label createIntroPoint(String text) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #edf7f3;");
        return label;
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
        resendLink.setStyle("-fx-text-fill: #1d5f52;");
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

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #365048;");
        return label;
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(ViewFactory.inputStyle());
        return field;
    }

    private ComboBox<Integer> createNumberBox(String prompt, int min, int max) {
        ComboBox<Integer> box = new ComboBox<>(FXCollections.observableArrayList(
                IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList())
        ));
        box.setPromptText(prompt);
        styleCombo(box);
        return box;
    }

    private void styleCombo(ComboBox<?> comboBox) {
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle(ViewFactory.inputStyle());
        HBox.setHgrow(comboBox, Priority.ALWAYS);
    }

    private String formatBirthDate(int day, String monthLabel, int year) {
        int month = IntStream.rangeClosed(1, 12)
                .filter(value -> Month.of(value).getDisplayName(TextStyle.FULL, Locale.FRENCH).equals(monthLabel))
                .findFirst()
                .orElse(1);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\u00A0", "")
                .replace("\u2007", "")
                .replace("\u202F", "")
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace("\uFEFF", "")
                .trim();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.contains(" ")) {
            return false;
        }
        int atIndex = email.indexOf('@');
        int lastAtIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex != lastAtIndex || atIndex == email.length() - 1) {
            return false;
        }
        String domain = email.substring(atIndex + 1);
        int dotIndex = domain.indexOf('.');
        return dotIndex > 0 && dotIndex < domain.length() - 1;
    }

    private static Map<String, List<String>> createRegionCities() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("Tanger-Tetouan-Al Hoceima", List.of("Tanger", "Tetouan", "Al Hoceima", "Larache", "Chefchaouen"));
        map.put("Oriental", List.of("Oujda", "Nador", "Berkane", "Taourirt", "Jerada"));
        map.put("Fes-Meknes", List.of("Fes", "Meknes", "Taza", "Ifrane", "Sefrou"));
        map.put("Rabat-Sale-Kenitra", List.of("Rabat", "Sale", "Kenitra", "Temara", "Khemisset"));
        map.put("Beni Mellal-Khenifra", List.of("Beni Mellal", "Khenifra", "Khouribga", "Azilal", "Fquih Ben Salah"));
        map.put("Casablanca-Settat", List.of("Casablanca", "Mohammedia", "Settat", "El Jadida", "Berrechid"));
        map.put("Marrakech-Safi", List.of("Marrakech", "Safi", "Essaouira", "El Kelaa des Sraghna", "Youssoufia"));
        map.put("Draa-Tafilalet", List.of("Errachidia", "Ouarzazate", "Midelt", "Zagora", "Tinghir"));
        map.put("Souss-Massa", List.of("Agadir", "Inezgane", "Taroudant", "Tiznit", "Chtouka Ait Baha"));
        map.put("Guelmim-Oued Noun", List.of("Guelmim", "Tan-Tan", "Sidi Ifni", "Assa", "Plage Blanche"));
        map.put("Laayoune-Sakia El Hamra", List.of("Laayoune", "Boujdour", "Tarfaya", "Es-Semara"));
        map.put("Dakhla-Oued Ed-Dahab", List.of("Dakhla", "Aousserd"));
        return map;
    }

    private static final class PasswordToggleControl extends StackPane {
        private final PasswordField passwordField = new PasswordField();
        private final TextField visibleField = new TextField();

        private PasswordToggleControl(String prompt) {
            passwordField.setPromptText(prompt);
            visibleField.setPromptText(prompt);
            passwordField.setMaxWidth(Double.MAX_VALUE);
            visibleField.setMaxWidth(Double.MAX_VALUE);
            passwordField.setStyle(ViewFactory.inputStyle() + "-fx-padding: 13 64 13 14;");
            visibleField.setStyle(ViewFactory.inputStyle() + "-fx-padding: 13 64 13 14;");
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
