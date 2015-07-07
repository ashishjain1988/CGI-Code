package com.cgix.datafiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnector {

	public static Connection getConnection() throws SQLException
	{
		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:mysql://localhost/test_cgi?user=cbio_user&password=somepassword");
		return connection;
	}
	public static Connection getConnection(String databaseName,String userName,String password) throws SQLException
	{
		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:mysql://localhost/"+databaseName+"?user="+userName+"&password="+password);
		return connection;
	}
}
