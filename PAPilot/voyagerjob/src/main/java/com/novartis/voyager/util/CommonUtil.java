
package com.novartis.voyager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novartis.voyager.common.constants.CommonErrorConstants;
import com.novartis.voyager.common.exception.ExceptionContext;
import com.novartis.voyager.common.exception.ExceptionHandler;
import com.novartis.voyager.common.exception.VoyagerException;



public class CommonUtil {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CommonUtil.class);

	/**
	 * This method returns SQL date to facilitate insertion of date in DB
	 * 
	 * @return SQL Date
	 */
	public static java.sql.Date getCurrentDate() {
		final java.util.Date today = DateTime.now(DateTimeZone.UTC ).toDate();
		return new java.sql.Date(today.getTime());
	}


	/**
	 * This method return true if String is Null or blank.
	 * Method isNullOrBlank
	 *
	 * @version 1.0
	 * @param inputObj
	 * @return boolean
	 */
	public static boolean isNullOrBlank(String inputObj) 
	{
		boolean status = false;
		if(inputObj==null ||inputObj.trim().equals(""))
		{
			status = true;
		}
		return status;
	}
	/**
	 * This method return true if String is Null .
	 * Method isNull
	 *
	 * @version 1.0
	 * @param inputObj
	 * @return boolean
	 */

	public static boolean isNull(String inputObj) 
	{
		boolean status = false;
		if(inputObj==null)
		{
			status = true;
		}
		return status;
	}

	/**
	 * This method return true if String is not Null or blank.
	 * Method isNotNullOrBlank
	 *
	 * @version 1.0
	 * @param  inputObj
	 * @return boolean
	 */
	public static boolean isNotNullOrBlank(String inputObj) 
	{
		boolean status = false;
		if(!(inputObj==null ||inputObj.trim().equals("")))
		{
			status = true;
		}
		return status;
	}
	
	/**
	 * Function to convert string to MD5
	 * @param cacheKeyString
	 * @param charSet
	 * @return
	 */
	public static byte[] stringToMD5Byte(String cacheKeyString,Charset charSet) {
		if(LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Entering CommonUtil.stringToMD5Byte method");
			
		}
		MessageDigest messageDigest;
		byte[] messageMD5="null".getBytes();;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageMD5=messageDigest.digest(cacheKeyString.getBytes(charSet));
		} catch (Exception ex) {
			ExceptionHandler expHandler=new ExceptionHandler();
			expHandler.handleException(ex);
			if(LOGGER.isDebugEnabled())
			{
				LOGGER.debug("Leaving CommonUtil.stringToMD5Byte method");
			}
			final HashMap<String, Object> ctxParam=new HashMap<String, Object>();
			ctxParam.put("cacheKey is: ", cacheKeyString);
			final ExceptionContext expCtx=new ExceptionContext("DuplicateCheckerCacheService.stringToMD5Byte", VoyagerException.SEVERITY_ERROR, "Unable to convert String to MD5 Byte", ctxParam);
			throw new VoyagerException(ex,CommonErrorConstants.ERROR_004, expCtx);

		}
		if(LOGGER.isDebugEnabled())
		{
			LOGGER.debug("Leaving CommonUtil.stringToMD5Byte method");
		}
		return messageMD5;
	}
	/**
	 * This Class is a common utility class to delete files created before a specified time 
	 * @param folder the temporay folder where files are stored
	 * @param timeInMillis time before which if a file is created then it will be deleted
	 */
	public static void deleteFile(String folder,long timeInMillis) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Entering CommonUtil.deleteFile method");
		}
		GenericCreationTimeFilter filter = new GenericCreationTimeFilter(timeInMillis);
		File dir = new File(folder);
		String[] fileNameList = null;
		if(dir!=null){
			// List out all the file name which are created before timeInMillis value from current system time 
			// Calls the accept method of GenericCreationTimeFilter and list the file names
			fileNameList=dir.list(filter);
		}
		if (fileNameList==null || fileNameList.length == 0){
			//If list is empty then return
			return;
		}
		File fileDelete;
		for (String fileName : fileNameList) {
			//Delete all the files and folder in the list
			String filePath = new StringBuffer(folder).append(File.separator)
					.append(fileName).toString();
			fileDelete = new File(filePath);
			boolean isdeleted=false;
			if (fileDelete.exists()){
				isdeleted= fileDelete.delete();
			}
			 if(LOGGER.isDebugEnabled()){
				 LOGGER.debug("file : {} is deleted : {}" ,filePath,isdeleted);
				}
		}
	}
	/**
	 * This is an inner class to implement FilenameFilter to list all files and 
	 * folder which are created before a specified time in milliseconds.
	 * 
	 */
	public static class GenericCreationTimeFilter implements FilenameFilter {
		private long milliSeconds;//3600000 for 1hr

		public GenericCreationTimeFilter(long milliSeconds) {
			this.milliSeconds = milliSeconds;
		}
		/**
		 * This method will return true if files and folder are created before a specified time.
		 */
		public boolean accept(File dir, String name) {
			String newPath=dir + File.separator + name;
			Path path = Paths.get(newPath);
			BasicFileAttributes view = null;
			FileTime currentTime = null;
			try {
				view = Files.getFileAttributeView(path,BasicFileAttributeView.class).readAttributes();
				if(null!=view && !view.isDirectory()){
					//If the view is a file then it enters this block and checks for the creation date before specified time and return true if passes.
					currentTime = FileTime.fromMillis(new Date().getTime());
					
					boolean returnFlag=(currentTime.toMillis() - view.creationTime().toMillis()) >= milliSeconds;
					if(returnFlag){
						if(LOGGER.isDebugEnabled()){
							LOGGER.debug("Files added to the list:" + newPath);
						}
					}
					return returnFlag;
				}
				else{
					//If the view is a folder then it enters this block and sets the new path as directory location and calls deleteFile method again.
					deleteFile(newPath,milliSeconds);
					return true;
				}
			} catch (IOException e) {
				ExceptionHandler expHandler=new ExceptionHandler();
				//Handling the exception
				expHandler.handleException(e);
			}
			return false;
		}
	}
	/**
	 * 
	 * @param inputStream
	 * @param targetDirectory
	 * @param targetFileName
	 * @return
	 * @throws IOException
	 */
	public static File copyStreamToStreamAndCloseBoth(InputStream inputStream, String targetDirectory, String targetFileName) throws IOException {
    	if(LOGGER.isDebugEnabled())
    	{
    		LOGGER.debug("Entering CommonUtil.copyStreamToStreamAndCloseBoth method");
    	}
        File targetDirectoryAsFile = new File(targetDirectory);
        if (!targetDirectoryAsFile.exists()) {
            targetDirectoryAsFile.mkdirs();
        }

        String targetFilePath = targetDirectory + File.separator + targetFileName;
        if(LOGGER.isDebugEnabled())
        {
        	LOGGER.debug("Target File Path : "+targetFilePath);
        }
        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(targetFilePath);
        int bytesRead;
        int BUFFER_SIZE = 4096;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
        if(LOGGER.isDebugEnabled())
        {
        	LOGGER.debug("Leaving CommonUtil.copyStreamToStreamAndCloseBoth method");
        }
        return new File(targetFilePath);
    }
}
