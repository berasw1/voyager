

package com.novartis.voyager.aws.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.Upload;
import com.novartis.voyager.aws.uuid.KeyGenerator;
import com.novartis.voyager.common.constants.CommonErrorConstants;
import com.novartis.voyager.common.exception.ExceptionContext;
import com.novartis.voyager.common.exception.ExceptionHandler;
import com.novartis.voyager.common.exception.VoyagerException;


/**
 * This class implements IS3RepositoryService interface and implements methods
 * which gets,puts and deletes object from/to S3 bucket
 */

public class S3RepositoryService implements IS3RepositoryService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(S3RepositoryService.class);

	private AmazonS3 s3Client;
	private KeyGenerator keyGenerator = null;
	private String regionName;
	private String bucketName;
	private String kmsId;
	
	

	/**
	 * This method is used to fetch the Stream object from S3 bucket based on
	 * bucket name and Object Id information.
	 * 
	 * @param region
	 * @param bucket
	 * @param assetId
	 * @return inputStream
	 * @throws VoyagerException
	 * 
	 */

	public InputStream getAssetById(String region, String bucketName,
			String assetId) throws VoyagerException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering S3RepositoryService.getAssetById method");
		}
		if (null != region) {
			Region getRegionName = Region.getRegion(Regions.fromName(region));
			s3Client.setRegion(getRegionName);
		}

		else {
			Region getRegionName = Region.getRegion(Regions
					.fromName(regionName));
			s3Client.setRegion(getRegionName);
		}

		S3Object object = null;
		try {
			object = s3Client.getObject(new GetObjectRequest(bucketName,
					assetId));
		} catch (AmazonServiceException ex) {
			final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
			ctxParam.put("AmazonServiceException", null);

			final ExceptionContext expCtx = new ExceptionContext(
					"S3RepositoryService.getAssetById method",
					VoyagerException.SEVERITY_ERROR, "AmazonServiceException",
					ctxParam);

			throw new VoyagerException(ex, CommonErrorConstants.ERROR_003, expCtx);
		} catch (AmazonClientException ex) {
			final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
			ctxParam.put("AmazonClientException", null);

			final ExceptionContext expCtx = new ExceptionContext(
					"S3RepositoryService.getAssetById method",
					VoyagerException.SEVERITY_ERROR, "AmazonClientException",
					ctxParam);

			throw new VoyagerException(ex, CommonErrorConstants.ERROR_010, expCtx);
		}
				
		InputStream inputStream = object.getObjectContent();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving S3RepositoryService.getAssetById method");
		}
		return inputStream;
	}

	/**
	 * Method to put large object of size greater than 100 MB in S3 bucket as
	 * Stream object. It returns boolean value true or false after upload
	 * operation
	 * 
	 * @param region
	 * @param bucketName
	 * @param assetId
	 * @param asset
	 * @return
	 */
	public boolean putAssetForLargeFile(String bucketName,
			String assetId, InputStream asset) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering S3RepositoryService.putAssetForLargeFile method");
		}
		boolean successFlag = true;
		
			Region getRegionName = Region.getRegion(Regions
					.fromName(regionName));
			s3Client.setRegion(getRegionName);
		File file = null;
		FileOutputStream fos = null;
		TransferManager transferManager = null;
		TransferManagerConfiguration tConfig = null;
		Upload upload = null;
		try {
			file = new File(assetId);
			fos = new FileOutputStream(file);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing in File Started");
			}
			byte[] buffer = new byte[50 * 1024 * 1024];
			int len = asset.read(buffer);
			while (len != -1) {
				fos.write(buffer, 0, len);
				len = asset.read(buffer);
			}
			fos.close();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing in File Complete");
			}
			transferManager = new TransferManager(s3Client);
			tConfig = new TransferManagerConfiguration();
			tConfig.setMinimumUploadPartSize(50 * 1024 * 1024);
			transferManager.setConfiguration(tConfig);
			upload = transferManager.upload(bucketName, assetId, file);
			upload.waitForCompletion();
		} catch (Exception ex) {
			if (ex != null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("There is an exception in putAsset Method of S3RepositoryService :: "
							+ ex.getMessage());
				}
				successFlag = false;
			}
			ExceptionHandler exHandler = new ExceptionHandler();
			exHandler.handleException(ex);
		} finally {
			if (asset != null) {
				try {
					asset.close();
				} catch (IOException ex) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("There was an error while closing the Input Stream :: "
								+ ex.getMessage());
					}
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("There was an error while closing the File Output Stream :: "
								+ ex.getMessage());
					}
				}
			}
			if (file != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("File Path :: " + file.getAbsolutePath());
				}
				boolean fileExistFlag = file.exists();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("File delete operation started");
				}
				boolean fileDeleteFlag=false;
				if (fileExistFlag){
					 fileDeleteFlag = file.delete();
				}
				if (fileExistFlag && fileDeleteFlag) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("File is successfully deleted :: "
								+ file.getName());
					}
				}

			}
			file = null;
			upload = null;
			tConfig = null;
			transferManager = null;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving S3RepositoryService.putAssetForLargeFile method");
		}
		return successFlag;
	}
	/**
	 * Method to put small object of size less than or equal to 100 MB in S3
	 * bucket as Stream object. It returns boolean value true or false after
	 * upload operation
	 * 
	 * @param region
	 * @param bucket
	 * @param assetId
	 * @param asset
	 * @returns successFlag
	 */

	public boolean putAssetForSmallFile(String bucketName,
			String assetId, InputStream asset) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering S3RepositoryService.putAssetForSmallFile method");
		}

		boolean successFlag = true;
		ObjectMetadata meta = new ObjectMetadata();

			Region getRegionName = Region.getRegion(Regions
					.fromName(regionName));
			s3Client.setRegion(getRegionName);
		try {

			if (null != asset && asset.available() > 0) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("asset.available()inside:" + asset.available());
				}
				meta.setContentEncoding(Constants.DEFAULT_ENCODING);
				meta.setContentType("text/plain");
				meta.setContentLength(asset.available());
			}
			if (null != asset) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("asset.available()outside:"
							+ asset.available());
				}
			}
			LOGGER.debug("kmsId :: "+kmsId);
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucketName, assetId, asset, meta).withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsId));
			putObjectRequest.getRequestClientOptions().setReadLimit(100000000);
			s3Client.putObject(putObjectRequest);
		} catch (Exception ex) {

			if (ex != null) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("There is an exception in putAsset Method of S3RepositoryService ");
				}
				successFlag = false;
			}
			ExceptionHandler exHandler = new ExceptionHandler();
			exHandler.handleException(ex);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving S3RepositoryService.putAssetForSmallFile method");
		}
		return successFlag;
	}

	/**
	 * Method to put object with Id in S3 bucket as Stream object. It returns
	 * the assetId
	 * 
	 * @param region
	 * @param bucket
	 * @param asset
	 * @return assetId
	 * @throws IOException
	 * 
	 */

	public String putAsset(String folderName, InputStream asset,
			long size) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering S3RepositoryService.putAsset method");
		}
		String assetId = null;
		boolean status = true;
		try {
			assetId = keyGenerator.generateKey();
		} catch (VoyagerException ex) {
			ExceptionHandler expHandler = new ExceptionHandler();
			expHandler.handleException(ex);
			throw new IOException("There is an error while generating the UUID");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info("The obtained assetId is :" + assetId);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Size of the File is :: " + size);
		}
	//	if (size <= 104857600) {
			status = putAssetForSmallFile(bucketName, folderName+"/"+assetId, asset);
		//} else {
		//	status = putAssetForLargeFile(bucketName, assetId, asset);
		//}
		if (status == false) {
			throw new IOException(
					"There is an error while putting an asset to S3 bucket.");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving S3RepositoryService.putAsset method");
		}
		return assetId;
	}
	
	/**
	 * This method is used to delete an asset from the bucket.
	 * 
	 * @param region
	 * @param bucketName
	 * @param assetId
	 * @throws VoyagerException
	 */
	
	public void deleteAsset(String region, String bucketName, String assetId)
			throws VoyagerException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering S3RepositoryService.deleteAsset method");
		}
		if (null != region) {
			Region getRegionName = Region.getRegion(Regions.fromName(region));
			s3Client.setRegion(getRegionName);
		}

		else {
			Region getRegionName = Region.getRegion(Regions
					.fromName(regionName));
			s3Client.setRegion(getRegionName);
		}

		try {
			s3Client.deleteObject(bucketName, assetId);
		}

		catch (AmazonServiceException ex) {
			ExceptionHandler expHandler = new ExceptionHandler();
			expHandler.handleException(ex);

			final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
			ctxParam.put("AmazonServiceException", null);

			final ExceptionContext expCtx = new ExceptionContext(
					"S3RepositoryService.deleteAsset",
					VoyagerException.SEVERITY_ERROR, "AmazonServiceException",
					ctxParam);

			throw new VoyagerException(ex, CommonErrorConstants.ERROR_003, expCtx);
		} catch (AmazonClientException ex) {
			ExceptionHandler expHandler = new ExceptionHandler();
			expHandler.handleException(ex);

			final HashMap<String, Object> ctxParam = new HashMap<String, Object>();
			ctxParam.put("AmazonClientException", null);

			final ExceptionContext expCtx = new ExceptionContext(
					"S3RepositoryService.deleteAsset",
					VoyagerException.SEVERITY_ERROR, "AmazonClientException",
					ctxParam);

			throw new VoyagerException(ex, CommonErrorConstants.ERROR_010, expCtx);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Leaving S3RepositoryService.deleteAsset method");
		}
	}

	/**
	 * @return the s3Client
	 */
	public AmazonS3 getS3Client() {
		return s3Client;
	}

	/**
	 * @param s3Client
	 *            the s3Client to set
	 */
	public void setS3Client(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	/**
	 * 
	 * @return keyGenerator
	 */
	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	/**
	 * 
	 * @param keyGenerator
	 */
	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	/**
	 * @return the regionName
	 */
	public String getRegionName() {
		return regionName;
	}

	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * @return the bucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

	/**
	 * @param bucketName the bucketName to set
	 */
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	/**
	 * @return the kmsId
	 */
	public String getKmsId() {
		return kmsId;
	}

	/**
	 * @param kmsId the kmsId to set
	 */
	public void setKmsId(String kmsId) {
		this.kmsId = kmsId;
	}
	

}
