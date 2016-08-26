
package com.novartis.voyager.aws.s3;

import java.io.IOException;
import java.io.InputStream;

import com.novartis.voyager.common.exception.VoyagerException;


/**
 * This interface contains methods to put,get and delete objects from S3 bucket
 * 
 * @author BERASW1
 * @ver
 * 
 * */
public interface IS3RepositoryService {

	/**
	 * Method to put small object of size less than or equal to 100 MB in S3
	 * bucket as Stream object. It returns boolean value true or false after
	 * upload operation
	 * 
	 * @param region
	 * @param bucketName
	 * @param assetId
	 * @param asset
	 * 
	 */
	public boolean putAssetForSmallFile(String bucketName,
			String assetId, InputStream asset);

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
			String assetId, InputStream asset);

	/**
	 * Method to get the id of the object to be put in S3 bucket.
	 * 
	 * @param region
	 * @param bucketName
	 * @param asset
	 * @throws IOException
	 */

	public String putAsset(String bucketName, InputStream asset,
			long size) throws IOException;

	/**
	 * This method is used to fetch the Stream object from S3 bucket based on
	 * bucket name and Object Id information.
	 * 
	 * @param region
	 * @param bucketName
	 * @param assetId
	 * @throws VoyagerException
	 */
	public InputStream getAssetById(String region, String bucketName,
			String assetId) throws VoyagerException;

	/**
	 * This method is used to delete the Stream object from S3 bucket based on
	 * bucket name and Object Id information.
	 * 
	 * @param region
	 * @param bucketName
	 * @param assetId
	 * @throws VoyagerException
	 */
	public void deleteAsset(String region, String bucketName, String assetId)
			throws VoyagerException;
}
