package com.novartis.voyager.transport;


import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novartis.voyager.aws.s3.S3RepositoryService;
import com.novartis.voyager.common.constants.CommonConstants;
import com.novartis.voyager.common.dto.Actigraph;
import com.novartis.voyager.common.dto.Striiv;
import com.novartis.voyager.dao.ActiGraphDeviceDAO;
import com.novartis.voyager.dao.StriivDeviceDAO;
import com.novartis.voyager.util.CommonUtil;

public class DataProcessor {
	
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessor.class);
	
	private S3RepositoryService s3RepositoryService;
	private String actigraphFolder;
	private String striivFolder;
	private StriivDeviceDAO striivDAO;
	private ActiGraphDeviceDAO activGraphDAO;
	
	boolean processData(String jsonData) {
										
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering DataProcessor.processData method");
		}
		
		boolean dataUpdateStatus = false;
		try{
	
			
			String assetId = null;
			String deviceType = null;
			LOGGER.debug("jsonData : "+jsonData);
			String deviceData = jsonData;
			
			if(jsonData.toUpperCase().contains(CommonConstants.DEVICE_TYPE_ACTIGRAPH)){
				
				deviceType = CommonConstants.DEVICE_TYPE_ACTIGRAPH;
				 LOGGER.info("device Type : "+deviceType);
				 byte[] dataArray = jsonData.getBytes();
				assetId = s3RepositoryService.putAsset(actigraphFolder, new ByteArrayInputStream(dataArray), dataArray.length);
				
				ObjectMapper mapper = new ObjectMapper();
				Actigraph actigraph = mapper.readValue(deviceData, Actigraph.class);
				
				Long qclJsonLogId = activGraphDAO.saveActivGraphQCLJsonData(jsonData,assetId);
				LOGGER.info("qclJsonLogId :: "+qclJsonLogId);
				activGraphDAO.processActiGraphDeviceData(actigraph,qclJsonLogId);
				dataUpdateStatus = true;
			}else if(jsonData.toUpperCase().contains(CommonConstants.DEVICE_TYPE_STRIIV)){
				deviceType = CommonConstants.DEVICE_TYPE_STRIIV;
				 LOGGER.info("device Type : "+deviceType);
				 byte[] dataArray = jsonData.getBytes();
				assetId = s3RepositoryService.putAsset(striivFolder, new ByteArrayInputStream(dataArray), dataArray.length);
				
				Long qclJsonLogId = striivDAO.saveStriivQCLJsonData(jsonData,assetId); 
				LOGGER.info("qclJsonLogId :: "+qclJsonLogId);
				
				ObjectMapper mapper = new ObjectMapper();
				Striiv striiv = mapper.readValue(deviceData, Striiv.class);
				
				striivDAO.processStriivDeviceData(striiv, qclJsonLogId);
				dataUpdateStatus = true;
			}
			
			 LOGGER.debug("device Type : "+deviceType);
			 LOGGER.info("File id created :: "+assetId);
			
		
		}catch(Exception ex){
			LOGGER.error("Exception in processing the JSON data ",ex);
			return dataUpdateStatus;
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving DataProcessor.processData method");
		}
		return dataUpdateStatus;
	}

	
	

	
	/**
	 * @return the s3RepositoryService
	 */
	public S3RepositoryService getS3RepositoryService() {
		return s3RepositoryService;
	}

	/**
	 * @param s3RepositoryService the s3RepositoryService to set
	 */
	public void setS3RepositoryService(S3RepositoryService s3RepositoryService) {
		this.s3RepositoryService = s3RepositoryService;
	}





	/**
	 * @return the actigraphFolder
	 */
	public String getActigraphFolder() {
		return actigraphFolder;
	}





	/**
	 * @param actigraphFolder the actigraphFolder to set
	 */
	public void setActigraphFolder(String actigraphFolder) {
		this.actigraphFolder = actigraphFolder;
	}





	/**
	 * @return the striivFolder
	 */
	public String getStriivFolder() {
		return striivFolder;
	}





	/**
	 * @param striivFolder the striivFolder to set
	 */
	public void setStriivFolder(String striivFolder) {
		this.striivFolder = striivFolder;
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
