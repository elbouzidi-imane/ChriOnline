package com.chrionline.client.network;

import com.chrionline.client.session.AppSession;
import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;
import com.chrionline.common.RequestSignature;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

public class TCPClient {
    private static final TCPClient INSTANCE = new TCPClient();

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private TCPClient() {
    }

    public static TCPClient getInstance() {
        return INSTANCE;
    }

    public synchronized void connect() throws IOException {
        if (isConnected()) {
            return;
        }
        try {
            socket = new Socket(AppConstants.HOST, AppConstants.PORT_TCP);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (ConnectException e) {
            closeQuietly();
            throw new ConnectException("Connexion refusee. Demarrez le serveur ChriOnline sur "
                    + AppConstants.HOST + ":" + AppConstants.PORT_TCP + ".");
        } catch (IOException e) {
            closeQuietly();
            throw e;
        }
    }

    public synchronized Message send(Message request) throws Exception {
        if (!isConnected()) {
            connect();
        }
        attachSessionToken(request);
        Message response = sendSigned(request);
        if (requiresSensitiveOtp(response)) {
            updateSessionToken(response);
            attachSessionToken(request);
            Optional<String> otp = askSensitiveOtp(response.getPayload());
            if (otp.isPresent()) {
                request.setSensitiveOtp(otp.get().trim());
                response = sendSigned(request);
            }
        }
        updateSessionToken(response);
        return response;
    }

    private Message sendSigned(Message request) throws Exception {
        RequestSignature.sign(request);
        out.reset();
        out.writeObject(request);
        out.flush();
        Object response = in.readObject();
        if (!(response instanceof Message message)) {
            throw new IOException("Reponse serveur invalide");
        }
        return message;
    }

    public synchronized void disconnect() throws IOException {
        IOException error = null;
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            error = e;
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            error = e;
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            error = e;
        } finally {
            socket = null;
            out = null;
            in = null;
        }
        if (error != null) {
            throw error;
        }
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void attachSessionToken(Message request) {
        request.setSessionToken(AppSession.getSessionToken());
    }

    private void updateSessionToken(Message response) {
        if (response != null && response.getSessionToken() != null && !response.getSessionToken().isBlank()) {
            AppSession.setSessionToken(response.getSessionToken());
        }
    }

    private boolean requiresSensitiveOtp(Message response) {
        return response != null
                && response.isError()
                && response.getPayload() != null
                && response.getPayload().startsWith("OTP_ACTION_REQUIRED");
    }

    private Optional<String> askSensitiveOtp(String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Verification action sensible");
        dialog.setHeaderText("Code OTP requis");
        dialog.setContentText(message.replace("OTP_ACTION_REQUIRED:", "").trim() + "\nCode OTP :");
        return dialog.showAndWait();
    }

    private void closeQuietly() {
        try {
            disconnect();
        } catch (IOException ignored) {
        }
    }
}
