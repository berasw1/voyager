
package com.novartis.voyager.aws.uuid;


import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novartis.voyager.aws.DBConnection;
import com.novartis.voyager.common.constants.QueryConstants;






/**
 * This is a singleton class which is used to get the IP and port number as well
 * as generate a sequence number from MySql
 * 
 * 
 * */

public class NodeIdentifier {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeIdentifier.class);
	private static String endPoint = null;
	private static String ipAddress = null;
	private static int sequenceNumber = 0;
	private static DBConnection connectionPool;

	/**
	 * Get the IP and port of server as node identifier
	 * 
	 * @return endPoints
	 * @throws UnknownHostException
	 * @throws MalformedObjectNameException
	 * @throws ReflectionException
	 * @throws MBeanException
	 * @throws InstanceNotFoundException
	 * @throws AttributeNotFoundException
	 * 
	 */

	public String getEndPoints() throws UnknownHostException,
	MalformedObjectNameException, AttributeNotFoundException,
	InstanceNotFoundException, MBeanException, ReflectionException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering NodeIdentifier.getEndPoints method");
		}
		if (endPoint != null) {
			return endPoint;
		}

		String endPoints = null;
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objs = mbs.queryNames(new ObjectName(
				"*:type=Connector,*"), Query.match(Query.attr("protocol"),
						Query.value("HTTP/1.1")));
		String hostname = InetAddress.getLocalHost().getHostName();
		InetAddress addresses = InetAddress.getByName(hostname);

		for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
			ObjectName obj = i.next();
			String scheme = mbs.getAttribute(obj, "scheme").toString();
			String port = obj.getKeyProperty("port");
			String host = addresses.getHostAddress();
			String ep = scheme + "://" + host + ":" + port;
			endPoints = ep.replaceAll(scheme, "").replaceAll("[^a-zA-Z0-9_-]",
					"");
		}
		endPoint = endPoints;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The generated nodeId is " + endPoint);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving NodeIdentifier.getEndPoints method");
		}
		return endPoint;
	}
	public String getNodeIP() throws UnknownHostException{
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering NodeIdentifier.getEndPoints method");
		}
		if (ipAddress != null) {
			return ipAddress;
		}

		String hostname = InetAddress.getLocalHost().getHostName();
		InetAddress addresses = InetAddress.getByName(hostname);

		ipAddress = addresses.getHostAddress();;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The ip address of the node is " + ipAddress);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving NodeIdentifier.getEndPoints method");
		}
		return ipAddress;
	}

	/**
	 * This method is used to return unique sequence number from RDS
	 * 
	 * @return sequenceNumber
	 * @throws SQLException
	 *
	 */

	public int getSequenceNumber() throws SQLException

	{

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering NodeIdentifier.getSequenceNumber method");
		}
		Connection connection = null;
		Statement  statement = null;
		ResultSet resultSet=null;
		try {
			LOGGER.debug("sequence number :: "+sequenceNumber);
			connection = connectionPool.getConnection();
			
			String genSequenceQuery = QueryConstants.GENERATE_SEQUENCE_QUERY;
			statement = connection.createStatement();
			resultSet= statement.executeQuery(genSequenceQuery);
		
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ResultSet is :" + resultSet);
			}
			if (resultSet.next()) {
				sequenceNumber = resultSet.getInt(1);
				LOGGER.debug("sequence number :"+Integer.toString(sequenceNumber));
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Leaving NodeIdentifier.getSequenceNumber method");
			}
		} finally {

			if(resultSet!=null)
			{
				resultSet.close();
			}
			if(statement!=null)
			{
				statement.close();
			}
			if(connection!=null)
			{
				connection.close();
			}
		}
		return sequenceNumber;
	}

	/**
	 * 
	 * @param connectionPool
	 */
	public void setConnectionPool(DBConnection connectionPool) {
		NodeIdentifier.connectionPool = connectionPool;
	}
}


