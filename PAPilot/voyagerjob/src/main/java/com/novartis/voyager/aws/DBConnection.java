
package com.novartis.voyager.aws;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is used to get DataBase Connectivity.
 * 
 *
 */
public class DBConnection {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DBConnection.class);

	private DataSource dataSource;

	/**
	 * This method is used to get the dataBase connection from the server.
	 * 
	 * @return connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection connection = null;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering DBConnection.getConnection method");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("dataSource is" + dataSource);
		}
		connection = dataSource.getConnection();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving DBConnection.getConnection method");
		}
		return connection;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource
	 *            the dataSource to set
	 */

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}