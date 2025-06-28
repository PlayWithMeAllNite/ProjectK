package org.example.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Clients {
    private static Clients instance;
    private List<Client> clients;
    
    private Clients() {
        clients = new ArrayList<>();
    }
    
    public static synchronized Clients getInstance() {
        if (instance == null) {
            instance = new Clients();
        }
        return instance;
    }
    
    public List<Client> getClients() {
        return clients;
    }
    
    public void addClient(Client client) {
        clients.add(client);
    }
    
    public Client getClientById(int clientId) {
        for (Client client : clients) {
            if (client.getClientId() == clientId) {
                return client;
            }
        }
        return null;
    }
    
    public Client getClientByPhone(String phone) {
        for (Client client : clients) {
            if (client.getPhone().equals(phone)) {
                return client;
            }
        }
        return null;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        clients.clear();
        String query = "SELECT client_id, phone, full_name, email, total_purchases, discount " +
                      "FROM clients ORDER BY client_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("client_id"),
                    rs.getString("phone"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBigDecimal("total_purchases"),
                    rs.getInt("discount")
                );
                clients.add(client);
            }
        }
    }
    
    public void clear() {
        clients.clear();
    }
} 