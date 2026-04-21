package school.hei.federationagricole.datasource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {
    private final String url = System.getenv("JDBC_URL");
    private final String username = System.getenv("USERNAME");
    private final String password = System.getenv("PASSWORD");

    @Bean
    public Connection getConnection(){
        try {
            return DriverManager.getConnection(url,username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}