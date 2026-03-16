package com.chrionline.server.repository;
import com.chrionline.server.db.DatabaseManager;
import java.sql.Connection;
public class UserDAO {
    protected Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }
    // TODO : requêtes JDBC User
}
