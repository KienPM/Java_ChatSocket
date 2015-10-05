/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Ken
 */
public class MySQLConnector {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String USER = "root";
    static final String PASS = "root";

    public static Connection getConnection(String dbName) throws Exception {
        String dbURL = "jdbc:mysql://localhost:3306/"
                + dbName
                + "?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf-8";
        Class.forName(JDBC_DRIVER).newInstance();
        return DriverManager.getConnection(dbURL, USER, PASS);
    }

}
