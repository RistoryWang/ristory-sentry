package com.ristory.extlog;

import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionFactory {

    private static interface Singleton {

        final ConnectionFactory INSTANCE = new ConnectionFactory();
    }

    private final DataSource dataSource;


    private ConnectionFactory() {

        Properties props = new Properties();
        Properties properties = new Properties();
        try {
            InputStream stream = ConnectionFactory.class.getClassLoader().getResourceAsStream("application.properties");
            props.load(stream);
        } catch (FileNotFoundException e) {
            System.out.println("读取配置文件异常");
        } catch(IOException ie){
            System.out.println("读取配置文件时IO异常");
        }

        properties.setProperty("user", props.getProperty("spring.datasource.username"));
        properties.setProperty("password", props.getProperty("spring.datasource.password")); // or get properties from some configuration file

        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>();
        DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                props.getProperty("spring.datasource.url"), properties

        );
        new PoolableConnectionFactory(
                connectionFactory, pool, null, "SELECT 1", 3, false, false, Connection.TRANSACTION_READ_COMMITTED
        );

        this.dataSource = new PoolingDataSource(pool);

    }

    public static Connection getDatabaseConnection() throws SQLException {

        return Singleton.INSTANCE.dataSource.getConnection();
    }
}
