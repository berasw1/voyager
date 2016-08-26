
package com.novartis.voyager.common.exception;

import java.util.ArrayList;
import java.util.List;


public class VoyagerException extends RuntimeException {

	/**
	 * Serial Version ID for the exception
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constant for FATAL Severity ERROR
	 */
	public static final String SEVERITY_FATAL = "FATAL";
	
	/**
	 * Constant for ERROR Severity ERROR
	 */
	public static final String SEVERITY_ERROR = "ERROR";
	
	/**
	 * Constant for WARNING Severity ERROR
	 */
	public static final String SEVERITY_WARNING = "WARNING";
	
	/**
	 * Constant for INFO Severity Message
	 */
	public static final String SEVERITY_INFO = "INFO";
	
	/**
	 * Constant for CONFIRM Severity Message 
	 */
	public static final String SEVERITY_CONFIRM = "CONFIRM";
	
	/**
	 * Root Exception that is being encapsulated
	 * by this custom exception class. Please note that it
	 * is not mandatory to have a root exception in the 
	 * custom application for e.g. in case of business exceptions
	 * root exception would not be present
	 */
	private Throwable rootException;
	
	/**
	 * Error ID corresponding to this error. 
	 * Each application should define its error scenarios and 
	 * error id needs to be created for each error scenario.
	 * Typical error id would be as ERR-nnnnn where nnnnn is a 
	 * 5 digit number. This error id along with the context id
	 * would get translated to the error message for the
	 * different stakeholders
	 */
	private String errorID;

	/**
	 * List of ExceptionContext objects which will give additional
	 * details about the exception. As the exception propagates
	 * between different layers of application. Each layer can add
	 * its own context to this list. However it is not mandatory to
	 * add a context for each layer. A context object should be added
	 * only if the layer has some additional information about the 
	 * exception which the layers beneath don't have. Its possible to  
	 * change the error severity by adding a new context object with
	 * the new severity level
	 */
	private List<ExceptionContext> contextList;

	/**
	 * Getter method for rootException
	 * @return Throwable - Root Exception which is encapsulated in the exception
	 */
	public Throwable getRootException() {
		return rootException;
	}

	/**
	 * Setter method for rootException
	 * @param rootException - Root Exception which is encapsulated in the exception
	 */
	public void setRootException(final Throwable rootException) {
		this.rootException = rootException;
	}

	/**
	 * Getter method for errorID
	 * @return String - Error ID corresponding to the exception being raised
	 */
	public String getErrorID() {
		return errorID;
	}

	/**
	 * Setter method for errorID
	 * @param errorID  - Error ID corresponding to the exception being raised
	 */
	public void setErrorID(final String errorID) {
		this.errorID = errorID;
	}

	/**
	 * Getter method for contextList
	 * @return List<ExceptionContext> - List of ExceptionContext objects which 
	 * provide additional details about the context of the exception
	 */
	public List<ExceptionContext> getContextList() {
		return contextList;
	}

	/**
	 * Method to add a new Context object to the Custom Application Exception
	 * 
	 * @param expContext - ExceptionContext object which provides further
	 * details about the exception being thrown
	 */
	public void addExceptionContext(final ExceptionContext expContext) {
		// Create a new list if not already available
		if (this.contextList == null) {
			this.contextList = new ArrayList<ExceptionContext>();
		}
		
		//Add the context object to the list
		this.contextList.add(expContext);
	}

	/**
	 * Constructor to be used when an exception is encapsulated for the
	 * first time
	 * 
	 * @param rootException - Root exception which is being encapsulated
	 * @param errorID - Error ID assigned to this error scenario
	 * @param expContext - List of exceptionContext objects providing additional
	 * details about the error scenario such as error severity, context id, 
	 * technical message etc
	 */
	public VoyagerException(final Throwable rootException, 
						final String errorID, 
						final ExceptionContext expContext) {
		super();
		this.rootException = rootException;
		this.errorID = errorID;
		addExceptionContext(expContext);
	}

	
	/**
	 * Constructor to be used for raising a business Exception which
	 * doesn't have an underlying RootException
	 * 
	 * @param errorID - Error ID assigned to this error scenario
	 * @param expContext - List of exceptionContext objects providing additional
	 * details about the error scenario such as error severity, context id, 
	 * technical message etc
	 */
	public VoyagerException(final String errorID, final ExceptionContext expContext) {
		super();
		this.errorID = errorID;
		addExceptionContext(expContext);
	}
	
	/**
	 * This method gets the exception severity based on the ExceptionContext
	 * objects added to the contextList. This method would return one of the
	 * following values. Severity level corresponding to the topmost layers
	 * context object would be returned to the user. This is with the assumption
	 * that top most layer would have more context about the exception and 
	 * would be in a better position to determine the severity of the 
	 * exception
	 * 
	 * FATAL
	 * ERROR
	 * WARNING
	 * 
	 * @return String - Exception Severity
	 */
	public String getErrorSeverity() {
		String errorSeverity = "";
		
		//Check that contextList is not empty
		if (this.contextList != null &&
			this.contextList.size() > 0) {
			
			//Get the Last ExceptionContext Object added to the list
			//Since the last layer that added the context info would 
			//be in a better position to decide the correct exception 
			//severity 
			final ExceptionContext expCtx = this.contextList.get(
								this.contextList.size() - 1);
			
			//Get ErrorSeverity from ExceptionContext object
			if (expCtx != null) {
				errorSeverity = expCtx.getErrorSeverity();
			}
		}
		
		return errorSeverity;
	}
	
	/**
	 * This method will return the key to be used to fetch the user
	 * message. It would be a concatenation of the ContextID and 
	 * ErrorID separated by a period (.). Context ID corresponding to the topmost layers
	 * context object would be used in this case. This is with the assumption
	 * that top most layer would have more context about the exception and 
	 * would be in a better position to determine the message to be displayed to the 
	 * users
	 * 
	 * @return String - Resource bundle key for the message to be displayed to the
	 * users
	 */
	public String getUserMessageKey() {
		final StringBuilder userMessage = new StringBuilder();
		
		//Check that contextList is not empty
		if (this.contextList != null &&
			this.contextList.size() > 0) {
			
			//Get the Last ExceptionContext Object added to the list
			//Since the last layer that added the context info would 
			//be in a better position to provide an accurate error 
			//message to the user
			final ExceptionContext expCtx = this.contextList.get(
								this.contextList.size() - 1);
			
			//Get Context ID from ExceptionContext object and 
			//append the errorID to it
			if (expCtx != null) {
				userMessage.append(expCtx.getContextID());
				userMessage.append('.');
				userMessage.append(this.errorID);
			}
		}
		
		return userMessage.toString();
	}
}
