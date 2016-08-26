
package com.novartis.voyager.aws.uuid;

import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novartis.voyager.common.constants.CommonErrorConstants;
import com.novartis.voyager.common.exception.ExceptionContext;
import com.novartis.voyager.common.exception.ExceptionHandler;
import com.novartis.voyager.common.exception.VoyagerException;


public class KeyGenerator {

	private NodeIdentifier nodeIdentifier;
	
	private static final Logger LOGGER=LoggerFactory.getLogger(KeyGenerator.class);
	
	
	/**
	 * This method is used to generate the final UUID which is a combination of portID,sequenceNumber and random string value. 
	 * 
	 * @return generatedKey
	 * 
	 */
	public String generateKey() {
		if (LOGGER.isDebugEnabled()){
		LOGGER.debug("Entering KeyGenerator.generateKey method");
		}
		String generatedKey = null;
		String nodeId = null;
		try {
			String key = KeyGenerator.getUuid();
			//nodeId = nodeIdentifier.getEndPoints();
			int seqNumber = nodeIdentifier.getSequenceNumber();
			generatedKey = seqNumber + key;
			//generatedKey =  key;
			if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Leaving KeyGenerator.generateKey method");
			}
		} catch (Exception ex) {
			ExceptionHandler expHandler = new ExceptionHandler();
			expHandler.handleException(ex);

			
			final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
			ctxParam.put("Error Generating the UUID", generatedKey);

			final ExceptionContext expCtx = new ExceptionContext(
					"KeyGenerator.generateKey method",
					VoyagerException.SEVERITY_ERROR, "Error Generating the UUID",
					ctxParam);

			throw new VoyagerException(ex, CommonErrorConstants.ERROR_001, expCtx);
		}
		return generatedKey;
	}

	/**
	 * Generate a random string each time when called upon
	 * 
	 * @return uuid
	 */
	public static String getUuid() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering KeyGenerator.getUuid method");
		}
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving KeyGenerator.getUuid method");
		}
		return uuid;
	}
	
	public String generateUpdateId()
	{
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("Entering KeyGenerator.generateUpdateId method");
			}
			String generatedKey = null;
			try {
				String key = KeyGenerator.getUuid();
				int seqNumber = nodeIdentifier.getSequenceNumber();
				generatedKey = seqNumber + key;
				if (LOGGER.isDebugEnabled()){
				LOGGER.debug("Leaving KeyGenerator.generateUpdateId method");
				}
				
				
				/*
				 * Below logic is to check if there is already any data available with same ID in S3 bucket. 
				 * Generate next random key if data with same id is already available.
				 * 
				 */
				
				
				
				
			} catch (Exception ex) {
				ExceptionHandler expHandler = new ExceptionHandler();
				expHandler.handleException(ex);

				
				final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
				ctxParam.put("Error Generating the UUID", generatedKey);

				final ExceptionContext expCtx = new ExceptionContext(
						"KeyGenerator.generateUpdateId method",
						VoyagerException.SEVERITY_ERROR, "Error Generating the UUID",
						ctxParam);

				throw new VoyagerException(ex, CommonErrorConstants.ERROR_001, expCtx);
			}
			return generatedKey;
	}
	
	/**
	 * 
	 * @param nodeIdentifier
	 */
	public void setNodeIdentifier(NodeIdentifier nodeIdentifier) {
		this.nodeIdentifier = nodeIdentifier;
	}

}
