

package com.novartis.voyager.common.exception;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExceptionMessageTxtFormatter This class formats the exception message as
 * simple text with tabs and new lines
 * 
 * @author BERASW1
 * @version 1.0
 */
public class ExceptionMessageTxtFormatter {

	/**
	 * Instance of the Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExceptionMessageTxtFormatter.class);

	/**
	 * Constant for New Line character to be used while formatting
	 */
	private static final String NEWLINE = "\r\n";

	/**
	 * Constant for Tab character to be used while formatting
	 */
	private static final String TAB = "\t";

	/**
	 * This method would format the exception message in simple text format and
	 * return it in a string to the caller. Exception would be formatted using
	 * TAB and NEW LINE characters
	 * 
	 * @param appExp
	 *            - Custom Application Exception object containing details of
	 *            the exception being handled
	 * @return String - Formatted Exception Message in TEXT format
	 */
	public String formatExceptionMsg(final VoyagerException appExp) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : formatExceptionMsg - Entry");
		}

		final StringBuilder msgBuilder = new StringBuilder(200);

		// Log the Error ID and Severity
		msgBuilder.append("Error ID	: ");
		msgBuilder.append(appExp.getErrorID());
		msgBuilder.append(NEWLINE);

		// Loop over the Exception Info ContextList
		// and create the error message in reverse order
		// so that latest context appears on top of the list
		for (int i = appExp.getContextList().size() - 1; i >= 0; i--) {
			final ExceptionContext expCtx = appExp.getContextList().get(i);
			// Add the ContextID
			msgBuilder.append(TAB);
			msgBuilder.append("Context ID	: ");
			msgBuilder.append(expCtx.getContextID());
			msgBuilder.append(NEWLINE);
			msgBuilder.append(TAB);

			// Add the Severity
			msgBuilder.append("Severity	: ");
			msgBuilder.append(expCtx.getErrorSeverity());
			msgBuilder.append(NEWLINE);
			msgBuilder.append(TAB);

			// Add the Error Message
			msgBuilder.append("Message		: ");
			msgBuilder.append(expCtx.getMessage());

			msgBuilder.append(NEWLINE);
			msgBuilder.append(TAB);

			// Add the parameters present in the Context
			// to the message
			if (expCtx.getParameters() == null) {
				msgBuilder.append("Parameters	: None");
				msgBuilder.append(NEWLINE);
			} else {
				msgBuilder.append("Parameters	: ");
				msgBuilder.append(NEWLINE);

				int count = 1;
				// Loop over all the entry objects in the Map
				// and print the Key and Values in separate line
				for (final Entry<String, Object> mapEntry : expCtx
						.getParameters().entrySet()) {
					msgBuilder.append(TAB);
					msgBuilder.append(TAB);
					msgBuilder.append(count);
					msgBuilder.append(") ");
					msgBuilder.append(mapEntry.getKey());
					msgBuilder.append("	: ");
					msgBuilder.append(mapEntry.getValue());
					msgBuilder.append(NEWLINE);
					count++;
				}
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method : formatExceptionMsg - Exit");
		}
		return msgBuilder.toString();
	}

}
