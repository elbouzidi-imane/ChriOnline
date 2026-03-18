package com.chrionline.client.network;

import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

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
            throw new ConnectException("Connexion refusée. Démarrez le serveur ChriOnline sur "
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
        out.writeObject(request);
        out.flush();
        Object response = in.readObject();
        if (!(response instanceof Message message)) {
            throw new IOException("Réponse serveur invalide");
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

    private void closeQuietly() {
        try {
            disconnect();
        } catch (IOException ignored) {
        }
    }
}
