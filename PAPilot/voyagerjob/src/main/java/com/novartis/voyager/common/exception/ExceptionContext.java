

package com.novartis.voyager.common.exception;

import java.util.Map;

/** 
 * ExceptionContext
 * This class will contain context details about the
 * exception being raised. As the exception propagates
 * between different layers of application. Each layer can add
 * its own context to this list. However it is not mandatory to
 * add a context for each layer. A context object should be added
 * only if the layer has some additional information about the 
 * exception which the layers beneath don't have. Its possible to  
 * change the error severity by adding a new context object with
 * the new severity level
 * 
 * 
 * @author BERASW1
 * @version 1.0 
 */
public class ExceptionContext {
	/**
	 * A unique ID to identify the Context of the Exception.
	 * This id would normally be the business/technical functionality
	 * in which the exception is raised. In case a layer is a reusable
	 * component used across business functions (such as export to Excel)
	 * then it would add a technical context id such as 'ExportToExcel'
	 * Layers/components which utilize this reusable component can then
	 * provide the business context to the error scenario such as 'PurchaseOrderReport'
	 */
	private String contextID;
	
	/**
	 * Error Severity. Valid Values are given below
	 * 
	 * FATAL
	 * ERROR
	 * WARNING
	 * INFO
	 * CONFIRM
	 * 
	 * This class will use only FATAL, ERROR, WARNING and INFO.
	 * INFO will be used where exceptions are caught and suppressed
	 * for e.g. exceptions raised during DB Resource clean up functions
	 */
	private String errorSeverity;
	
	/**
	 * Message to be written to the log file.
	 * This would be the technical message which would be visible
	 * to the application support personnel
	 */
	private String message;
	
	/**
	 * Map of parameters to provide additional details about the context
	 * Typically this would include the parameters to the method in 
	 * which the exception was raised. It can also include some of the 
	 * in process variable values too. Since in production environment
	 * logging level would normally be ERROR, debug and info 
	 * logger statements that normally provide this information would not
	 * be available in log file. Hence it is critical to provide these
	 * details to the support personnel so that the exact scenario in 
	 * which the error occurred would be identified
	 */
	private Map<String, Object> parameters;

	/**
	 * Getter method for Context ID
	 * @return string - Context ID for the Exception Context
	 */
	public String getContextID() {
		return contextID;
	}

	/**
	 * Setter method for Context ID
	 * @param contextID - Context ID for the Exception Context
	 */
	public void setContextID(final String contextID) {
		this.contextID = contextID;
	}

	/**
	 * Getter method for Error severity
	 * @return String - Error Severity normally it would be one of the following 
	 * FATAL,ERROR,WARNING,INFO,CONFIRM
	 */
	public String getErrorSeverity() {
		return errorSeverity;
	}

	/**
	 * Setter method for error Severity
	 * @param errorSeverity - Error Severity normally it would be one of the following 
	 * FATAL,ERROR,WARNING,INFO,CONFIRM
	 */
	public void setErrorSeverity(final String errorSeverity) {
		this.errorSeverity = errorSeverity;
	}

	/**
	 * Getter method for message
	 * @return String - Technical Error Message to be displayed to the support
	 * personnel
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Setter method for message
	 * @param message - Technical Error Message to be displayed to the support
	 * personnel
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * Getter method for parameter
	 * @return Map<String, Object> - Map of parameters to provide additional details about the context
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Setter method for parameters
	 * @param parameters - Map of parameters to provide additional details about the context
	 */
	public void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Constructor for ExceptionContext with Parameters
	 * being optional
	 * 
	 * @param contextID - Context ID for the Exception Context
	 * @param errorSeverity - Error Severity normally it would be one of the following 
	 * FATAL,ERROR,WARNING,INFO,CONFIRM
	 * @param message - Technical Error Message to be displayed to the support
	 */
	public ExceptionContext(final String contextID, 
							final String errorSeverity,
							final String message) {
		this.contextID = contextID;
		this.errorSeverity = errorSeverity;
		this.message = message;
	}
	
	
	/**
	 * Constructor for ExceptionContext including
	 * parameters
	 * @param contextID - Context ID for the Exception Context
	 * @param errorSeverity - Error Severity normally it would be one of the following 
	 * FATAL,ERROR,WARNING,INFO,CONFIRM
	 * @param message - Technical Error Message to be displayed to the support
	 * @param parameters - Map of parameters to provide additional details about the context
	 */
	public ExceptionContext(final String contextID, 
			final String errorSeverity,
			final String message,
			final Map<String, Object> parameters) {
	this.contextID = contextID;
	this.errorSeverity = errorSeverity;
	this.message = message;
	this.parameters = parameters;
	}
	
}
