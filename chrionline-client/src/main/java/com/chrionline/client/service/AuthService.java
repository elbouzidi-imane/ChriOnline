package com.chrionline.client.service;

import com.chrionline.client.model.UserDTO;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;

public class AuthService {
    private final TCPClient tcp = TCPClient.getInstance();

    public UserDTO login(String email, String mdp) throws Exception {
        Message response = tcp.send(new Message(Protocol.LOGIN, email + ":" + mdp));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), UserDTO.class);
    }

    public UserDTO register(String nom, String prenom, String email, String mdp, String tel, String adresse) throws Exception {
        String payload = String.join("|", nom, prenom, email, mdp, tel, adresse);
        Message response = tcp.send(new Message(Protocol.REGISTER, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), UserDTO.class);
    }

    public void logout() throws Exception {
        tcp.send(new Message(Protocol.LOGOUT, ""));
    }
}
