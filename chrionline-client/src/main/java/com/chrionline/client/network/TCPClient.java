package com.chrionline.client.network;
import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;
import java.io.*;
import java.net.Socket;
public class TCPClient {
    private static TCPClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;
    private TCPClient() {}
    public static TCPClient getInstance() {
        if (instance == null) instance = new TCPClient(); return instance;
    }
    public void connect() throws Exception {
        socket = new Socket(AppConstants.HOST, AppConstants.PORT_TCP);
        out    = new ObjectOutputStream(socket.getOutputStream());
        in     = new ObjectInputStream(socket.getInputStream());
        System.out.println("Connecté à ChriOnline Server");
    }
    public Message send(Message request) throws Exception {
        out.writeObject(request); out.flush();
        return (Message) in.readObject();
    }
    public void disconnect() throws Exception { if (socket != null) socket.close(); }
}
