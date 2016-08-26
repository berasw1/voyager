package com.novartis.voyager.dao;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novartis.voyager.aws.DBConnection;
import com.novartis.voyager.common.dto.Actigraph;
import com.novartis.voyager.common.dto.JobProcessModel;

public class CommonDAO {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonDAO.class);
	private static DBConnection connectionPool;
	
	private ActiGraphDeviceDAO actiGraphDeviceDAO = new ActiGraphDeviceDAO();

	@Scheduled(fixedDelay = 3600000)
	public void processActigraphDataJob() throws SQLException{
		Connection connection = null;
		try{
			LOGGER.info("Data Processing Job Started. : " + new Date());
			connection = connectionPool.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("select  ACTIGRAPH_DEVICE_DETAILS_ID, SERIAL_NUMBER, QCL_JSON_LOG_ID  from ACTI_JOBTRACKER WHERE UPDATE_STATUS is null");
			ResultSet resultSet = preparedStatement.executeQuery();
			List<JobProcessModel> processModels = new ArrayList<JobProcessModel>();
			//int i =0;
			while(resultSet.next()){
				JobProcessModel jobProcessModel = new JobProcessModel(
						resultSet.getLong(1), resultSet.getString(2),
						resultSet.getLong(3));
				processModels.add(jobProcessModel);
				
			}
			resultSet.close();
			LOGGER.info("Total Number of records need to process : " + processModels.size());
			if(processModels.size() > 0)
				processActivGrapRecords(processModels);
			LOGGER.info("Data Processing Job End : " + new Date());
		}catch(Exception e){
			LOGGER.error("Exception in getting job process list.",e);
		}finally{
			if(connection!=null){
				connection.close();
			}
		}
	}
	
	private void processActivGrapRecords(List<JobProcessModel> jobProcessModels)
			throws SQLException {
		for(JobProcessModel jobProcessModel : jobProcessModels){
			processActiGraphRecord(jobProcessModel);
		}
		

	}
	
	private void processActiGraphRecord(JobProcessModel jobProcessModel) throws SQLException{
		Connection connection = null;
		LOGGER.debug("Entering CommonDAO.processActiGraphRecord method.");
		try{
			LOGGER.info("Start Record  for Processing  :: " + jobProcessModel.getDeviceDetailsId() + "\t" + jobProcessModel.getSerialNumber() + "\t" + jobProcessModel.getQclJsonLogId());
			connection = connectionPool.getConnection();
			//connection.setAutoCommit(false);
			Actigraph actigraph = getActiGraphByQclJsonLogId(jobProcessModel.getQclJsonLogId(), connection);
			if(actigraph != null){
				//deleteRecordsInforByDeviceDetailsId(jobProcessModel.getDeviceDetailsId(), connection);
				actiGraphDeviceDAO.saveActiGraphRecords(actigraph.records,
						jobProcessModel.getDeviceDetailsId(), connection, jobProcessModel.getSerialNumber());
				updateJobTracker(jobProcessModel.getDeviceDetailsId(), connection);
			}
			//connection.commit();
			LOGGER.info("End processing Record ");
		}catch(Exception e){
			LOGGER.debug( "Exception in processActiGraphRecord Record", e);
		}finally{
			if(connection!=null){
				connection.close();
			}
		}
	}
	
	
	public Actigraph getActiGraphByQclJsonLogId(Long qclJsonId, Connection connection) throws SQLException{
		ResultSet resultSet = null;
		try{
			LOGGER.debug("Entering CommonDAO.getActiGraphByQclJsonLogId method.");
			PreparedStatement pStatement = connection.prepareStatement("Select JSON_DATA FROM QCL_JSON_LOG WHERE QCL_JSON_LOG_ID = ?");
			pStatement.setLong(1, qclJsonId);
			 resultSet = pStatement.executeQuery();
			if(resultSet.next()){
				Clob clob = resultSet.getClob(1);
				String jsonString = clob.getSubString(1, (int) clob.length());
				ObjectMapper mapper = new ObjectMapper();
				Actigraph actigraph = mapper.readValue(jsonString, Actigraph.class);
				LOGGER.debug("Leaving CommonDAO.getActiGraphByQclJsonLogId method.");
				return actigraph;
			}
			
			return null;
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.debug( "Exception in getActiGraphByQclJsonLogId Record", e);
			return null;
		}finally{
			if(resultSet!=null){
				resultSet.close();
			}
		}
	}
	
	
	private  boolean deleteRecordsInforByDeviceDetailsId(Long deviceDetailsId, Connection connection){
		try{
			LOGGER.debug("Entering CommonDAO.deleteRecordsInforByDeviceDetailsId method.");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACTIGRAPH_RECORDS Where ACTIGRAPH_DEVICE_DETAILS_ID = ?");
			preparedStatement.setLong(1, deviceDetailsId);
			preparedStatement.execute();
			LOGGER.debug("Leaving CommonDAO.deleteRecordsInforByDeviceDetailsId method.");
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}

	private  boolean updateJobTracker(Long deviceDetailsId, Connection connection){
		try{
			LOGGER.debug("Entering CommonDAO.deleteRecordsInforByDeviceDetailsId method.");
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ACTI_JOBTRACKER SET UPDATE_STATUS='Y', UPDATE_DATE=SYSDATE Where ACTIGRAPH_DEVICE_DETAILS_ID = ?");
			preparedStatement.setLong(1, deviceDetailsId);
			preparedStatement.execute();
			LOGGER.debug("Leaving CommonDAO.deleteRecordsInforByDeviceDetailsId method.");
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * @return the connectionPool
	 */
	public static DBConnection getConnectionPool() {
		return connectionPool;
	}

	/**
	 * @param connectionPool the connectionPool to set
	 */
	public static void setConnectionPool(DBConnection connectionPool) {
		CommonDAO.connectionPool = connectionPool;
	}
	
	
	
}
