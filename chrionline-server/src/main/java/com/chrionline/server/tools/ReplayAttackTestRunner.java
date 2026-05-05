package com.chrionline.server.tools;

import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.common.RequestSignature;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public final class ReplayAttackTestRunner {

    private ReplayAttackTestRunner() {
    }

    public static void main(String[] args) throws Exception {
        Message replayedRequest = new Message(Protocol.GET_PRODUCTS, "");
        RequestSignature.sign(replayedRequest);

        Message firstResponse = send(replayedRequest);
        Message secondResponse = send(replayedRequest);

        System.out.println("=== Replay attack test ===");
        System.out.println("Nonce reutilise : " + replayedRequest.getNonce());
        System.out.println("Premier envoi  : " + summarize(firstResponse));
        System.out.println("Deuxieme envoi : " + summarize(secondResponse));
    }

    private static Message send(Message request) throws Exception {
        try (Socket socket = new Socket(AppConstants.HOST, AppConstants.PORT_TCP);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            Object response = in.readObject();
            if (!(response instanceof Message message)) {
                throw new IllegalStateException("Reponse serveur invalide");
            }
            return message;
        }
    }

    private static String summarize(Message response) {
        if (response == null) {
            return "null";
        }
        return response.getStatus() + " | " + response.getPayload();
    }
}
