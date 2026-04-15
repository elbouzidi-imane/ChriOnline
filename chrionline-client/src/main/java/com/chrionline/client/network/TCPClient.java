package com.chrionline.client.network;

import com.chrionline.client.session.AppSession;
import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.util.UUID;

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
        hydrateSecurityMetadata(request);
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

    private void hydrateSecurityMetadata(Message request) {
        if (request == null) {
            return;
        }
        String requestId = readStringMethod(request, "getRequestId");
        if (requestId == null || requestId.isBlank()) {
            invokeIfPresent(request, "setRequestId", String.class, UUID.randomUUID().toString());
        }
        long timestamp = readLongMethod(request, "getTimestamp");
        if (timestamp <= 0L) {
            invokeIfPresent(request, "setTimestamp", long.class, System.currentTimeMillis());
        }
        String sessionToken = readStringMethod(request, "getSessionToken");
        if ((sessionToken == null || sessionToken.isBlank())
                && AppSession.getSessionToken() != null
                && !AppSession.getSessionToken().isBlank()) {
            invokeIfPresent(request, "setSessionToken", String.class, AppSession.getSessionToken());
        }
    }

    private String readStringMethod(Message request, String methodName) {
        try {
            Method method = request.getClass().getMethod(methodName);
            Object value = method.invoke(request);
            return value instanceof String ? (String) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private long readLongMethod(Message request, String methodName) {
        try {
            Method method = request.getClass().getMethod(methodName);
            Object value = method.invoke(request);
            return value instanceof Number ? ((Number) value).longValue() : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private void invokeIfPresent(Message request, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = request.getClass().getMethod(methodName, parameterType);
            method.invoke(request, value);
        } catch (Exception ignored) {
        }
    }
}
