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
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.novartis.voyager.aws.DBConnection;
import com.novartis.voyager.common.constants.CommonConstants;
import com.novartis.voyager.common.dto.Actigraph;
import com.novartis.voyager.common.dto.CommonJobProcessModel;
import com.novartis.voyager.common.dto.Striiv;

public class QclDataProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QclDataProcessor.class);
	private static DBConnection connectionPool;
	private StriivDeviceDAO striivDAO;
	private ActiGraphDeviceDAO activGraphDAO;
	
	
	@Scheduled(fixedDelay = 180000)
	public void processPendingDataJob() throws SQLException{
		Connection connection = null;
		try{
			LOGGER.info("ProcessPendingDataJob Started. : " + new Date());
			connection = connectionPool.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("select QCL_JSON_LOG_ID, JSON_DATA from QCL_JSON_LOG where QCL_JSON_LOG_ID not in (select QCL_JSON_LOG_ID from ACTIGRAPH_DEVICE_DETAILS UNION select QCL_JSON_LOG_ID from striiv_device_details )");
			ResultSet resultSet = preparedStatement.executeQuery();
			List<CommonJobProcessModel> processModels = new ArrayList<CommonJobProcessModel>();
			int i =0;
			while(resultSet.next()){
				Clob clob = resultSet.getClob(2);
				String jsonString = clob.getSubString(1, (int) clob.length());
				CommonJobProcessModel jobProcessModel = new CommonJobProcessModel(
						resultSet.getLong(1), jsonString);
				processModels.add(jobProcessModel);
				i++;
				if(i>30)
					break;
			}
			resultSet.close();
			if(connection!=null){
				connection.close();
			}
			LOGGER.info("Total Number of records need to process : " + processModels.size());
			if(processModels.size() > 0)
				processData(processModels);
			LOGGER.info("Data Processing Job End : " + new Date());
		}catch(Exception e){
			LOGGER.error("Exception in getting job process list.",e);
		}finally{
			if(connection!=null){
				connection.close();
			}
		}
	}
	
	
	boolean processData(List<CommonJobProcessModel> processModels) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering QclDataProcessor.processData method");
		}
		
		boolean dataUpdateStatus = false;
		try{
	
			for(CommonJobProcessModel jobProcessModel : processModels){	
			String deviceType = null;
			
			LOGGER.info("Data processed for qclJsonLogId :: "+jobProcessModel.getQclJsonLogId());
			try{			
			if(jobProcessModel.getQclData().toUpperCase().contains(CommonConstants.DEVICE_TYPE_ACTIGRAPH)){
				
				deviceType = CommonConstants.DEVICE_TYPE_ACTIGRAPH;
				 LOGGER.info("device Type : "+deviceType);
				ObjectMapper mapper = new ObjectMapper();
				Actigraph actigraph = mapper.readValue(jobProcessModel.getQclData(), Actigraph.class);
				
				activGraphDAO.processActiGraphDeviceData(actigraph,jobProcessModel.getQclJsonLogId());
				dataUpdateStatus = true;
			}else if(jobProcessModel.getQclData().toUpperCase().contains(CommonConstants.DEVICE_TYPE_STRIIV)){
				deviceType = CommonConstants.DEVICE_TYPE_STRIIV;
				 LOGGER.info("device Type : "+deviceType);
							
								
				ObjectMapper mapper = new ObjectMapper();
				Striiv striiv = mapper.readValue(jobProcessModel.getQclData(), Striiv.class);
				
				striivDAO.processStriivDeviceData(striiv, jobProcessModel.getQclJsonLogId());
				dataUpdateStatus = true;
			}
			}catch(UnrecognizedPropertyException e){
				LOGGER.info("Data format issue, QclJsonLogId "+jobProcessModel.getQclJsonLogId()+" data :: "+ jobProcessModel.getQclData());
				continue;
			}
			
			 LOGGER.info("Data processed for qclJsonLogId :: "+jobProcessModel.getQclJsonLogId() +" status "+dataUpdateStatus);
		}
			
		}catch(Exception ex){
			LOGGER.error("Exception in processing the JSON data ",ex);
			return dataUpdateStatus;
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving QclDataProcessor.processData method");
		}
		return dataUpdateStatus;
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
		QclDataProcessor.connectionPool = connectionPool;
	}
	
	/**
	 * @return the striivDAO
	 */
	public StriivDeviceDAO getStriivDAO() {
		return striivDAO;
	}

	/**
	 * @param striivDAO the striivDAO to set
	 */
	public void setStriivDAO(StriivDeviceDAO striivDAO) {
		this.striivDAO = striivDAO;
	}


	/**
	 * @return the activGraphDAO
	 */
	public ActiGraphDeviceDAO getActivGraphDAO() {
		return activGraphDAO;
	}

	/**
	 * @param activGraphDAO the activGraphDAO to set
	 */
	public void setActivGraphDAO(ActiGraphDeviceDAO activGraphDAO) {
		this.activGraphDAO = activGraphDAO;
	}
	

}
