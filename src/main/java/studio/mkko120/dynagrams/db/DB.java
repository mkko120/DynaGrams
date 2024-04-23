package studio.mkko120.dynagrams.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Database connection class
 * <p>
 * Uses HikariCP for connection pooling
 * @since 1.0.0
 * @author mkko120
 */
public class DB {

    private HikariPool pool;

    public DB(
        String host,
        int port,
        String database,
        String username,
        String password
    ) {
        // Connect to the database
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        try {
            // Initialize the connection pool
            Class.forName("com.mysql.cj.jdbc.Driver");
            pool = new HikariPool(config);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("Failed to connect to the database");
            e.printStackTrace();
        }
    }

    /**
     * Get a connection from the pool
     * @return {@link Connection} or {@link null} if failed
     */
    public Connection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Failed to get a connection from the database");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the connection pool
     */
    public void close() {
        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            Bukkit.getLogger().warning("Failed to close the database connection");
            e.printStackTrace();
        }
    }

}
