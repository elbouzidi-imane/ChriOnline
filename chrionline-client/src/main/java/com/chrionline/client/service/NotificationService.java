package com.chrionline.client.service;

import com.chrionline.client.model.NotificationDTO;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class NotificationService {
    private final TCPClient tcp = TCPClient.getInstance();

    public List<NotificationDTO> getMyNotifications() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_NOTIFICATIONS, String.valueOf(AppSession.getCurrentUser().getId())));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        Type listType = new TypeToken<List<NotificationDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public void markAsRead(int notificationId) throws Exception {
        Message response = tcp.send(new Message(
                Protocol.MARK_NOTIFICATION_READ,
                AppSession.getCurrentUser().getId() + "|" + notificationId
        ));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
    }
}
