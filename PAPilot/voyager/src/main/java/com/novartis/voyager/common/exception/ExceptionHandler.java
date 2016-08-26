
package com.novartis.voyager.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ExceptionHandler This class handles the exception that have been created It
 * will log the exception and also notify the respective stakeHolders
 * 
 * 
 * @author BERASW1
 * @version 1.0
 */
public class ExceptionHandler {
	/**
	 * Logger attribute in order to log the exception to the log file
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExceptionHandler.class);


	/**
	 * This method Formats the Exception Message using the specified formatter.
	 * Format for the Exception Message is specified using the Message.Formatter
	 * key in the configuration properties file
	 * 
	 * @param appExp
	 *            - Custom Application Exception object containing details of
	 *            the exception being handled
	 * @return String - formatted Exception Message based on the user specified
	 *         format
	 */
	private String getExceptionMessage(final VoyagerException appExp) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : getExceptionMessage - Entry");
		}

		final ExceptionMessageTxtFormatter msgFormatter = new ExceptionMessageTxtFormatter();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : getExceptionMessage - Exit");
		}

		return msgFormatter.formatExceptionMsg(appExp);
	}

	/**
	 * This method logs the Exception based on the configuration property
	 * specified
	 * 
	 * @param appExp
	 *            - Custom Application Exception object containing details of
	 *            the exception being handled
	 */
	private void logException(final VoyagerException appExp) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : logException - Entry");
		}
		if (appExp.getRootException() == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(getExceptionMessage(appExp), appExp);
			}
		} else {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(getExceptionMessage(appExp),
						appExp.getRootException());
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : logException - Exit");
		}
	}

	/**
	 * This method handles the exception by logging the exception and then
	 * notifying the support personnel. This is the only method which the
	 * Exception Handler class exposes to the outside world
	 * 
	 * @param appExp
	 *            - Custom Application Exception object containing details of
	 *            the exception being handled
	 */
	public void handleException(final Exception appExp) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : handleException - Entry");
		}
		

		Throwable actualAppExp = appExp;
		Throwable voyagerAppExp = appExp;
		while (actualAppExp instanceof VoyagerException) {
			voyagerAppExp = actualAppExp;
			actualAppExp = ((VoyagerException) actualAppExp).getRootException();

		}
		if (voyagerAppExp instanceof VoyagerException) {
			// Log the Exception
			logException((VoyagerException) voyagerAppExp);
		} else {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Exception:", voyagerAppExp);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : handleException - Exit");
		}
	}
}
