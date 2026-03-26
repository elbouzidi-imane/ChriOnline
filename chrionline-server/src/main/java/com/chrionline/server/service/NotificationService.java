package com.chrionline.server.service;

import com.chrionline.server.model.User;
import com.chrionline.server.model.NotificationEntry;
import com.chrionline.server.network.ConnectedClientRegistry;
import com.chrionline.server.repository.NotificationDAO;
import com.chrionline.server.repository.UserDAO;

import java.util.List;

public class NotificationService {
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public void notifyUser(int userId, String message) {
        NotificationEntry entry = notificationDAO.save(userId, message);
        System.out.println("NotificationService.notifyUser : userId=" + userId
                + " saved=" + (entry != null) + " message=" + message);
        ConnectedClientRegistry.notifyUser(userId, message);
    }

    public void notifyAdmins(String message) {
        for (Integer adminId : userDAO.findAdminIds()) {
            notifyUser(adminId, message);
        }
    }

    public void notifyAdminsAboutClient(int userId, String action) {
        User user = userDAO.findById(userId);
        String identity = user == null
                ? "Client #" + userId
                : (safeName(user.getPrenom()) + " " + safeName(user.getNom())).trim() + " (#" + userId + ")";
        notifyAdmins(identity + " " + action);
    }

    public void notifyClientAboutAdminAction(int userId, String action) {
        notifyUser(userId, action);
    }

    public List<NotificationEntry> getNotifications(int userId) {
        return notificationDAO.findByUser(userId);
    }

    public boolean markAsRead(int userId, int notificationId) {
        return notificationDAO.markAsRead(notificationId, userId);
    }

    private String safeName(String value) {
        return value == null ? "" : value.trim();
    }
}
