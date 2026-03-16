package com.chrionline.server.network;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.handler.*;
import java.io.*;
import java.net.Socket;
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthHandler    auth    = new AuthHandler();
    private final ProductHandler product = new ProductHandler();
    private final CartHandler    cart    = new CartHandler();
    private final OrderHandler   order   = new OrderHandler();
    private final AdminHandler   admin   = new AdminHandler();
    public ClientHandler(Socket socket) { this.socket = socket; }
    @Override
    public void run() {
        try (ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            Message req;
            while ((req = (Message) in.readObject()) != null) {
                out.writeObject(route(req)); out.flush();
            }
        } catch (EOFException e) {
            System.out.println("Client déconnecté : " + socket.getInetAddress());
        } catch (Exception e) { System.err.println("Erreur handler : " + e.getMessage()); }
    }
    private Message route(Message req) {
        return switch (req.getType()) {
            case Protocol.LOGIN, Protocol.REGISTER, Protocol.LOGOUT -> auth.handle(req);
            case Protocol.GET_PRODUCTS, Protocol.GET_PRODUCT, Protocol.GET_CATEGORIES -> product.handle(req);
            case Protocol.GET_CART, Protocol.ADD_TO_CART, Protocol.REMOVE_FROM_CART, Protocol.CLEAR_CART -> cart.handle(req);
            case Protocol.PLACE_ORDER, Protocol.GET_ORDERS, Protocol.GET_ORDER -> order.handle(req);
            case Protocol.ADMIN_ADD_PRODUCT, Protocol.ADMIN_UPDATE_PRODUCT,
                 Protocol.ADMIN_DELETE_PRODUCT, Protocol.ADMIN_GET_USERS, Protocol.ADMIN_GET_ORDERS -> admin.handle(req);
            default -> Message.error("Type inconnu : " + req.getType());
        };
    }
}
