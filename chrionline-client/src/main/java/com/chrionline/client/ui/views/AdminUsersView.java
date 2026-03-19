package com.chrionline.client.ui.views;

import com.chrionline.client.model.UserDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdminUsersView extends VBox {
    private final AdminService adminService = new AdminService();
    private final TableView<UserDTO> usersTable = new TableView<>();

    public AdminUsersView() {
        if (!AdminViewSupport.ensureAdmin()) {
            return;
        }

        VBox content = new VBox(18);

        HBox actions = new HBox(10);
        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadUsers());
        Button suspend = ViewFactory.createPrimaryButton("Suspendre");
        suspend.setOnAction(event -> changeUserStatus(true));
        Button activate = ViewFactory.createSecondaryButton("Activer");
        activate.setOnAction(event -> changeUserStatus(false));
        actions.getChildren().addAll(refresh, suspend, activate);

        Label hint = new Label("Le serveur permet d'afficher, suspendre et reactiver les utilisateurs. Pas de modification de profil admin cote serveur actuellement.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        usersTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<UserDTO, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPrenom() + " " + data.getValue().getNom()));

        TableColumn<UserDTO, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));

        TableColumn<UserDTO, String> phoneCol = new TableColumn<>("Telephone");
        phoneCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getTelephone())));

        TableColumn<UserDTO, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getRole())));

        TableColumn<UserDTO, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getStatut())));

        TableColumn<UserDTO, String> dateCol = new TableColumn<>("Inscription");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDateInscription() == null ? "-" : format.format(data.getValue().getDateInscription())
        ));

        usersTable.getColumns().setAll(nameCol, emailCol, phoneCol, roleCol, statusCol, dateCol);

        VBox card = new VBox(14, actions, hint, usersTable);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        VBox.setVgrow(usersTable, Priority.ALWAYS);

        content.getChildren().add(card);
        getChildren().add(AdminViewSupport.createAdminPage(
                "Gestion des utilisateurs",
                "Suivez les comptes et controlez leur acces a l'application.",
                "users",
                content
        ));

        loadUsers();
    }

    private void loadUsers() {
        try {
            List<UserDTO> users = adminService.getUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void changeUserStatus(boolean suspend) {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un utilisateur.");
            return;
        }
        try {
            if (suspend) {
                adminService.suspendUser(selected.getId());
                UIUtils.showSuccess("Utilisateur suspendu.");
            } else {
                adminService.activateUser(selected.getId());
                UIUtils.showSuccess("Utilisateur active.");
            }
            loadUsers();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
