package pl.r3.zlecenie;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        this.username = username;
        this.password = password;
    }

    public boolean connect() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean tableExists(String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createTable() {
        String tableName = "player_data";
        if (tableExists(tableName)) {
            return true; // Table already exists
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                + "    id INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    player_name VARCHAR(50) NOT NULL,\n"
                + "    player_uuid VARCHAR(36) NOT NULL,\n"
                + "    player_level INT NOT NULL,\n"
                + "    player_exp INT NOT NULL,\n"
                + "    highest_reward_received INT NOT NULL\n"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean playerExists(UUID playerUUID) {
        String sql = "SELECT 1 FROM player_data WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void insertPlayerData(String playerName, UUID playerUUID) {
        String sql = "INSERT INTO player_data (player_name, player_uuid, player_level, player_exp, highest_reward_received) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerName);
            stmt.setString(2, playerUUID.toString());
            stmt.setInt(3, 1);
            stmt.setInt(4, 0); // Initial experience points set to 0
            stmt.setInt(5, 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getHighestRewardReceived(UUID playerId) {
        String sql = "SELECT MAX(highest_reward_received) FROM player_data WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Get the highest reward received or 0 if no result
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void updateHighestRewardReceived(UUID playerId, int newHighestRewardReceived) {
        String sql = "UPDATE player_data SET highest_reward_received = ? WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newHighestRewardReceived);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int getPlayerLevel(UUID playerId) {
        String sql = "SELECT player_level FROM player_data WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("player_level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int getPlayerExp(UUID playerId) {
        String sql = "SELECT player_exp FROM player_data WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("player_exp");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void updatePlayerLevel(UUID playerId, int newLevel) {
        String sql = "UPDATE player_data SET player_level = ? WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newLevel);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerExp(UUID playerId, int newExp) {
        String sql = "UPDATE player_data SET player_exp = ? WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newExp);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
