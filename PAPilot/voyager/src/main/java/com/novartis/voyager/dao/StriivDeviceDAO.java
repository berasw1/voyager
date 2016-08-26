package com.novartis.voyager.dao;

import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novartis.voyager.aws.DBConnection;
import com.novartis.voyager.common.constants.QueryConstants;
import com.novartis.voyager.common.dto.Striiv;
import com.novartis.voyager.common.dto.Striiv.DeviceDetails;
import com.novartis.voyager.common.dto.Striiv.QclBtlePacketHeaders.Packets;
import com.novartis.voyager.common.dto.Striiv.Record;
import com.novartis.voyager.common.dto.Striiv.Record.ActivityData;
import com.novartis.voyager.common.dto.Striiv.Record.ActivityData.Value;
import com.novartis.voyager.common.dto.Striiv.TwonetProperties;
import com.novartis.voyager.util.CommonUtil;

public class StriivDeviceDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StriivDeviceDAO.class);
	
	private static DBConnection connectionPool;
	
	/**
	 * This method extracts all parts of Striiv device JSON data and sends to store in different tables.
	 * 
	 * @param striiv
	 * @param qclJsonLogId
	 * @throws SQLException
	 */
	public void processStriivDeviceData(Striiv striiv, Long qclJsonLogId) throws Exception{
		Connection connection = null;
		LOGGER.debug("Entering StriivDeviceDAO.processStriivDeviceData method.");
		try{
			connection = connectionPool.getConnection();
			connection.setAutoCommit(false);
			
			 if(striiv == null)
				 return;
			
			DeviceDetails deviceDetails = striiv.deviceDetails;
			
			if (deviceDetails == null || deviceDetails.serialNumber == null
					|| CommonUtil.isNullOrBlank(deviceDetails.serialNumber.value)){
				LOGGER.debug("Serial number is blank ");
				return ;
			}
				
			
			String serialNumber = deviceDetails.serialNumber.value;
			LOGGER.debug("Serial Number of device :: "+serialNumber);
			
			Long deviceId = getStriivDeviceIdBySerialNumber(serialNumber, connection);
			
			if(deviceId == null)
				deviceId = saveStriivDevice(serialNumber, connection);
			
			LOGGER.debug("Device Id  :: "+deviceId);
			
			Long deviceDetailsId = saveStriivDeviceDetails(deviceDetails, deviceId, qclJsonLogId, connection);
			
			Record[] records = striiv.records;
			if(records != null && records.length > 0)
				saveStriivRecords(records, deviceDetailsId, connection, serialNumber);
			
			if( striiv.qclBtlePacketHeaders != null && striiv.qclBtlePacketHeaders.packets != null)
				saveStriivQclBtlePackets(striiv.qclBtlePacketHeaders.packets, deviceDetailsId, connection);
			
			if(striiv.twonetProperties != null)
				saveStriivTwonetProperties(striiv.twonetProperties, deviceDetailsId, connection);
			connection.commit();
		}catch(SQLException se){
			LOGGER.error("Exception in datbase operation",se);
			connection.rollback();
		}catch (Exception e) {
			
			LOGGER.error("Exception in Striiv DAO Layer",e);
		}finally{
			if(connection!=null){
				connection.close();
			}
			
			LOGGER.debug("Leaving ActiGraphDeviceDAO.processActiGraphDeviceData method.");
		}
		
	}
	
	/**
	 * The method to store the JSON data of Striiv device directly in database column.
	 * 
	 * @param striivQclJsonData
	 * @param uuid
	 * @return
	 * @throws SQLException
	 */
	public Long saveStriivQCLJsonData(String striivQclJsonData, String uuid) throws SQLException{
		PreparedStatement pStatement = null;
		Connection connection = null;
		try{
			connection = connectionPool.getConnection();
			connection.setAutoCommit(false);
			pStatement = connection.prepareStatement(QueryConstants.QCL_JSON_LOG_INSERT_QRY, new String[] { "QCL_JSON_LOG_ID" });

			Clob myClob = connection.createClob();
			Writer clobWriter = myClob.setCharacterStream(1);
			StringBuffer sb = new StringBuffer();
			clobWriter.write(striivQclJsonData);
			sb.append(striivQclJsonData);
			myClob.setString(1, sb.toString());
			
			pStatement.setClob(1, myClob);
			pStatement.setString(2, uuid);
			pStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			
			
			if(pStatement.executeUpdate() == 0){
				LOGGER.debug("Data not inserted.");
				throw new SQLException();
			}
				
			ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
			Long qclJsonLogId = null;
			if(null != generatedSeqNum && generatedSeqNum.next()){
				qclJsonLogId = generatedSeqNum.getLong(1);
			}
			connection.commit();
			return qclJsonLogId;
		}catch(Exception e){
			LOGGER.error("Exception in inserting JSon data :: ",e);
			throw new SQLException();
		}finally {

			if(pStatement!=null)
			{
				pStatement.close();
			}
			if(connection!=null)
			{
				connection.close();
			}
			
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivQCLJsonData method.");
		}
	}
	
	/**
	 * Method to save the Striiv device details sending data to Voyager.
	 * @param serialNumber
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long saveStriivDevice(String serialNumber, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivDevice method.");
		try{
			pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_DEVICE_INSERT_QRY, new String[] { "STRIIV_DEVICE_ID" });
			pStatement.setString(1, serialNumber);
			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
			Long deviceId = null;
			if(null != generatedSeqNum && generatedSeqNum.next()){
				deviceId = generatedSeqNum.getLong(1);
			}
			LOGGER.debug("Device id of Striiv device sending data : "+deviceId);
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivDevice method.");
			return deviceId;
		}catch(Exception e){
			LOGGER.error("Exception is saving device details of Striiv. Error :: ",e);
			throw new SQLException();
		}finally{
			pStatement.close();
		}
	}
	
	
	/**
	 * Method to check if an Striiv device with same serial number has already sent data.
	 * 
	 * @param serialNumber
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long getStriivDeviceIdBySerialNumber(String serialNumber, Connection connection) throws SQLException{
		PreparedStatement statement = null;
		LOGGER.debug("Entering StriivDeviceDAO.getStriivDeviceIdBySerialNumber method.");
		try{
			statement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.GET_STRIIV_DEVICE_ID_BY_SERIAL_NUMBER);
			statement.setString(1, serialNumber);
			ResultSet resultSet = statement.executeQuery();
			Long deviceId = null;
			if(resultSet.next()){
				deviceId = resultSet.getLong("STRIIV_DEVICE_ID");
			}	
			LOGGER.info("Device serial number = :: "+serialNumber+" Device Id = :: "+deviceId);
			LOGGER.debug("Leaving ActiGraphDeviceDAO.getActiGraphhDeviceIdBySerialNumber method.");
			return deviceId;
		}catch(Exception e){
			LOGGER.error("Exception in fetching serial device id of devices based on serial number.",e);
			throw new SQLException();
		}finally{
			if (statement != null){
				statement.close();
			}
		}
	}

	/**
	 * Method to store the device details of Striiv devices.
	 * 
	 * @param deviceDetails
	 * @param deviceId
	 * @param qclJsonLogId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long saveStriivDeviceDetails(DeviceDetails deviceDetails, Long deviceId, Long qclJsonLogId,
			Connection connection) throws SQLException {
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivDeviceDetails method.");
		try {
			String  decoderModel 			= null;
			Long 	batteryLevel 			= null;
			String  batteryLevelUnit		= null;
			String  firmwareVersion 		= null;
			String  hardwareVersion 		= null;
			String  currentTimeOnDevice 	= null;
			String  currentTimeOnDeviceUnit = null;
			Long    fileWritesRemaining 	= null;
			Long    fileSpaceFree 			= null;
			String  fileSpaceFreeUnit 		= null;
			Long    apiVersion 				= null;
			Long    screenOnCount 			= null;
			Long    resetCount 				= null;
			Long    radioHardwareVersion	= null;

			pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_DEVICE_DETAILS_INSERT_QRY,
					new String[] { "STRIIV_DEVICE_DETAILS_ID" });

			if (deviceDetails.decoderModel != null){
				decoderModel = deviceDetails.decoderModel.value;
			}
			pStatement.setString(1, decoderModel);	

			if (deviceDetails.batteryLevel != null) {
				batteryLevel = deviceDetails.batteryLevel.value;
				batteryLevelUnit = deviceDetails.batteryLevel.unit;
				pStatement.setLong(2, batteryLevel);
			}else{
				pStatement.setNull(2, Types.LONGVARCHAR);
			}
			pStatement.setString(3, batteryLevelUnit);
			
			if (deviceDetails.firmwareVersion != null){
				firmwareVersion = deviceDetails.firmwareVersion.value;
			}
			pStatement.setString(4, firmwareVersion);

			if (deviceDetails.hardwareVersion != null){
				hardwareVersion = deviceDetails.hardwareVersion.value;
			}
			pStatement.setString(5, hardwareVersion);
			
			if (deviceDetails.currentTimeOnDevice != null){
				currentTimeOnDevice = deviceDetails.currentTimeOnDevice.value;
				currentTimeOnDeviceUnit = deviceDetails.currentTimeOnDevice.unit;
			}
			pStatement.setString(6, currentTimeOnDevice);
			pStatement.setString(7, currentTimeOnDeviceUnit);	

			if (deviceDetails.fileWritesRemaining != null){
				fileWritesRemaining = deviceDetails.fileWritesRemaining.value;
				pStatement.setLong(8, fileWritesRemaining);
			}else{
				pStatement.setNull(8, Types.LONGVARCHAR);
			}

			if (deviceDetails.fileSpaceFree != null) {
				fileSpaceFree = deviceDetails.fileSpaceFree.value;
				fileSpaceFreeUnit = deviceDetails.fileSpaceFree.unit;
				pStatement.setLong(9, fileSpaceFree);
			}else{
				pStatement.setNull(9, Types.LONGVARCHAR);
			}
			pStatement.setString(10, fileSpaceFreeUnit);
			
			if (deviceDetails.apiVersion != null){
				apiVersion = deviceDetails.apiVersion.value;
				pStatement.setLong(11, apiVersion);
			}else{
				pStatement.setNull(11, Types.LONGVARCHAR);
			}

			if (deviceDetails.screenOnCount != null){
				screenOnCount = deviceDetails.screenOnCount.value;
				pStatement.setLong(12, screenOnCount);
			}else{
				pStatement.setNull(12, Types.LONGVARCHAR);
			}

			if (deviceDetails.resetCount != null){
				resetCount = deviceDetails.resetCount.value;
				pStatement.setLong(13, resetCount);
			}else{
				pStatement.setNull(13, Types.LONGVARCHAR);
			}

			if (deviceDetails.radioHardwareVersion != null){
				radioHardwareVersion = deviceDetails.radioHardwareVersion.value;
				pStatement.setLong(14, radioHardwareVersion);
			}else{
				pStatement.setNull(14, Types.LONGVARCHAR);
			}
			pStatement.setLong(15, deviceId);
			pStatement.setLong(16, qclJsonLogId);

			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			
			ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
			Long deviceDetailsId = null;
			if(null != generatedSeqNum && generatedSeqNum.next()){
				deviceDetailsId = generatedSeqNum.getLong(1);
				LOGGER.info("deviceDetailsId after storing device detais of Striiv : "+deviceDetailsId);
			}
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivDeviceDetails method.");
			return deviceDetailsId;
		} catch (Exception e) {
			LOGGER.error("Exception in saving Striiv device details :: ",e);
			throw new SQLException();
		}finally{
			if(pStatement != null ) pStatement.close();
		}
	}
	
	
	/**
	 * Method to save the records of Striiv devices.
	 * 
	 * @param records
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveStriivRecords(Record[] records, Long deviceDetailsId,
			Connection connection, String serialNumber) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivRecords method.");
		try{
			for(Record record : records){
				String  packetType        = null;
				String  recordDate 		  = null;
				String  recordDateUnit 	  = null;
				Boolean isCurrentLocalDay = false;
				Boolean isCurrentDay      = false;

				pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_RECORD_INSERT_QRY, new String[] { "STRIIV_RECORD_ID" });
				
				if(record.packetType != null)
					packetType = record.packetType.value;
				if(record.date != null){
					recordDate = record.date.value;
					recordDateUnit = record.date.unit;
				}
				if(record.isCurrentLocalDay != null){
					isCurrentLocalDay = record.isCurrentLocalDay.value;
				}
				if(record.isCurrentDay != null){
					isCurrentDay = record.isCurrentDay.value;
					
				}
				
				pStatement.setString(1, packetType);
				pStatement.setString(2, recordDate);
				pStatement.setString(3, recordDateUnit);
				pStatement.setBoolean(4, isCurrentLocalDay);
				pStatement.setBoolean(5, isCurrentDay);
				pStatement.setLong(6, deviceDetailsId);

				if(pStatement.executeUpdate() == 0)
					throw new SQLException();
				ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
				Long recordId = null;
				if(null != generatedSeqNum && generatedSeqNum.next())
					recordId = generatedSeqNum.getLong(1);
				LOGGER.info("Record id for STRIIV device ID :: "+deviceDetailsId +" is :: "+recordId);
				if (record.activityData != null && record.activityData.value != null
						&& record.activityData.value.length > 0)
					saveStriivActivityData(record.activityData, recordId, connection, serialNumber);
			}
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivRecords method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in saving Striiv device records :: ",e);
			throw new SQLException();
		}finally{
			if(pStatement != null) pStatement.close();
		}
	}	

	/**
	 * Method to save activity data of Striiv devices.
	 * 
	 * @param activityData
	 * @param recordId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveStriivActivityData(ActivityData activityData, Long recordId, Connection connection, String serialNumber) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivActivityData method.");
		try{
			Value[] activityValues = activityData.value;
			pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_ACTIVITY_INSERT_QRY);
			
			for(Value activityValue : activityValues){
			  	String startTime 			 = null;
			  	String startTimeUnit 		 = null;
				Long   interval 			 = null;
				String intervalUnit 		 = null;
				Long   run 					 = null;
				Long   walk 				 = null;
				Long   distance 			 = null;
				String distanceUnit 		 = null;
				Long   caloriesBurned 		 = null;
				String caloriesBurnedUnit    = null;
				Long   totalActivityTime     = null;
				String totalActivityTimeUnit = null;
				
				if(activityValue.startTime != null){
					startTime = activityValue.startTime.value;
					startTimeUnit = activityValue.startTime.unit;
				}
				pStatement.setString(1, startTime);
				pStatement.setString(2, startTimeUnit);
				
				if(activityValue.interval != null){
					interval = activityValue.interval.value;
					intervalUnit = activityValue.interval.unit;
					pStatement.setLong(3, interval);
				}else{
					pStatement.setNull(3, Types.LONGVARCHAR);
				}
				pStatement.setString(4, intervalUnit);
				
				if(activityValue.run != null){
					run = activityValue.run.value;
				}
				if(activityValue.walk != null)
					walk = activityValue.walk.value;
				if(activityValue.distance != null){
					distance = activityValue.distance.value;
					distanceUnit = activityValue.distance.unit;
				}
				if(activityValue.caloriesBurned != null){
					caloriesBurned = activityValue.caloriesBurned.value;
					caloriesBurnedUnit = activityValue.caloriesBurned.unit;
				}
				if(activityValue.totalActivityTime != null){
					totalActivityTime = activityValue.totalActivityTime.value;
					totalActivityTimeUnit = activityValue.totalActivityTime.unit;
				}
				
				
				
				
				pStatement.setLong(5, run);
				pStatement.setLong(6, walk);
				pStatement.setLong(7, distance);
				pStatement.setString(8, distanceUnit);
				pStatement.setLong(9, caloriesBurned);
				pStatement.setString(10, caloriesBurnedUnit);
				pStatement.setLong(11, totalActivityTime);
				pStatement.setString(12, totalActivityTimeUnit);
				pStatement.setLong(13, recordId);
				
				pStatement.setBoolean(14, striivCheckActivityBySerialNumberAndStartTime(connection, serialNumber, startTime));
				pStatement.addBatch();
			}
			pStatement.executeBatch();
			LOGGER.info("The STRIIV activity data saved for record id :: "+recordId);
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivActivityData method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in saving Striiv Activity data :: ",e);
			throw new SQLException();
		}finally{
			if(pStatement != null) pStatement.close();
		}
	}
	
	/**
	 * Method to store the QclBtlePackets details of Striiv devices.
	 * 
	 * @param packets
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveStriivQclBtlePackets(Packets packets, Long deviceDetailsId, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivQclBtlePackets method.");
		try{
			if(packets != null && packets.value != null && packets.value.length > 0){
				pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_QCL_BTLE_PACKET_INSERT_QRY);
				com.novartis.voyager.common.dto.Striiv.QclBtlePacketHeaders.Packets.Value[] packetValues = packets.value;
				for (com.novartis.voyager.common.dto.Striiv.QclBtlePacketHeaders.Packets.Value packetValue : packetValues) {
					Long 	uuid 					= null;
					String 	uuidDescription 		= null;
					Long 	btlePacketId 			= null;
					String 	uuidType 				= null;
					Long 	btlePacketHeaderLength 	= null;
					Long 	offset 					= null;
					Long 	packetType 				= null;
					Long 	version 				= null;

					if (packetValue.uuid != null)
						uuid = packetValue.uuid.value;
					if (packetValue.uuidDescription != null)
						uuidDescription = packetValue.uuidDescription.value;
					if (packetValue.id != null)
						btlePacketId = packetValue.id.value;
					if(packetValue.uuidType != null)
						uuidType = packetValue.uuidType.value;
					if (packetValue.length != null)
						btlePacketHeaderLength = packetValue.length.value;
					if (packetValue.offset != null)
						offset = packetValue.offset.value;
					if (packetValue.packetType != null)
						packetType = packetValue.packetType.value;
					if (packetValue.version != null)
						version = packetValue.version.value;
					
					pStatement.setLong(1, uuid);
					pStatement.setString(2, uuidDescription);
					pStatement.setLong(3, btlePacketId);
					pStatement.setString(4, uuidType);
					pStatement.setLong(5, btlePacketHeaderLength);
					pStatement.setLong(6, offset);
					pStatement.setLong(7, packetType);
					pStatement.setLong(8, version);
					pStatement.setLong(9, deviceDetailsId);
					pStatement.addBatch();
				}
				pStatement.executeBatch();
			}
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivQclBtlePackets method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in StriivDeviceDAO.saveStriivQclBtlePackets method :: ",e);
			throw new SQLException();
		}finally{
			if(pStatement != null ) pStatement.close();
		}
		
	}
	
	/**
	 * Method to save the TwonetProperties details of Striiv devices.
	 * 
	 * @param tProperties
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveStriivTwonetProperties(TwonetProperties tProperties, Long deviceDetailsId, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering StriivDeviceDAO.saveStriivTwonetProperties method.");
		try{
		 	String airInterfaceType 	= null;
		 	String cdeVersion 			= null;
		 	String customerId 			= null;
		 	String customerName 		= null;
		 	String deviceAddress 		= null;
		 	String deviceData 			= null;
		 	String deviceModel 			= null;
		 	String deviceSerialNumber 	= null;
		 	String deviceType 			= null;
		 	String hubId 				= null;
		 	Long   hubReceiveTimeOffset = null;
		 	String twonetId 			= null;
		 	String timeZone 			= null;
		 	Long   hubReceiveTime 		= null;
		 	Long   spReceiveTime 		= null;
		 	String exporterVersion 		= null;
		 	String decoderVersion         = null;
			 	
			if (tProperties.airInterfaceType != null)
				airInterfaceType = tProperties.airInterfaceType.value;
			if (tProperties.cdeVersion != null)
				cdeVersion = tProperties.cdeVersion.value;
			if (tProperties.customerId != null)
				customerId = tProperties.customerId.value;
			if (tProperties.customerName != null)
				customerName = tProperties.customerName.value;
			if (tProperties.deviceAddress != null)
				deviceAddress = tProperties.deviceAddress.value;
			if (tProperties.deviceData != null)
				deviceData = tProperties.deviceData.value;
			if (tProperties.deviceModel != null)
				deviceModel = tProperties.deviceModel.value;
			if (tProperties.deviceSerialNumber != null)
				deviceSerialNumber = tProperties.deviceSerialNumber.value;
			if (tProperties.deviceType != null)
				deviceType = tProperties.deviceType.value;
			if (tProperties.hubId != null)
				hubId = tProperties.hubId.value;
			if (tProperties.hubReceiveTimeOffset != null)
				hubReceiveTimeOffset = tProperties.hubReceiveTimeOffset.value;
			if (tProperties.twonetId != null)
				twonetId = tProperties.twonetId.value;
			if (tProperties.timeZone != null)
				timeZone = tProperties.timeZone.value;
			if (tProperties.hubReceiveTime != null)
				hubReceiveTime = tProperties.hubReceiveTime.value;
			if (tProperties.spReceiveTime != null)
				spReceiveTime = tProperties.spReceiveTime.value;		
			if(tProperties.exporterVersion != null)
				exporterVersion = tProperties.exporterVersion.value;
			
			if(tProperties.decoderVersion != null){
				decoderVersion = tProperties.decoderVersion.value;
				LOGGER.debug("decoderVersion "+decoderVersion);
				
			}
			
			pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_TWONET_PROPERTIES_INSERT_QRY);

			pStatement.setString(1, airInterfaceType);
			pStatement.setString(2, cdeVersion);
			pStatement.setString(3, customerId);
			pStatement.setString(4, customerName);
			pStatement.setString(5, deviceAddress);
			if(deviceData != null){
				Clob myClob = connection.createClob();
				Writer clobWriter = myClob.setCharacterStream(1);
				StringBuffer sb = new StringBuffer();
				clobWriter.write(deviceData);
				sb.append(deviceData);
				myClob.setString(6, sb.toString());
				pStatement.setClob(6, myClob);
			}
			pStatement.setString(7, deviceModel);
			pStatement.setString(8, deviceSerialNumber);
			pStatement.setString(9, deviceType);
			pStatement.setString(10, hubId);
			pStatement.setLong(11, hubReceiveTimeOffset);
			pStatement.setString(12, twonetId);
			pStatement.setString(13, timeZone);
			pStatement.setLong(14, hubReceiveTime);
			pStatement.setLong(15, spReceiveTime);
			pStatement.setString(16, exporterVersion);
			pStatement.setLong(17, deviceDetailsId);
			pStatement.setString(18, decoderVersion);
			
			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			LOGGER.debug("Leaving StriivDeviceDAO.saveStriivTwonetProperties method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in StriivDeviceDAO.saveStriivTwonetProperties method :: ",e);
			throw new SQLException();
		}finally{
			if(pStatement != null) pStatement.close();
		}
	}

	
	/**
	 * 
	 * This method is to check if activity data of a Striiv device already exists for a timestamp.
	 * 
	 * @param connection
	 * @param serialNumber
	 * @param startTime
	 * @return
	 * @throws SQLException
	 */
	private boolean striivCheckActivityBySerialNumberAndStartTime(Connection connection, String serialNumber, String startTime) throws SQLException{
		LOGGER.debug("Entering StriivDeviceDAO.striivCheckActivityBySerialNumberAndStartTime method.");
		PreparedStatement pStatement = null;
		try{
			pStatement = connection.prepareStatement(QueryConstants.VoyagerStriivSqlConstants.STRIIV_CHECK_ACTIVITY_BY_SERIAL_NUMBER_AND_START_TIME);
			pStatement.setString(1, serialNumber);
			pStatement.setString(2, startTime);
			ResultSet resultSet = pStatement.executeQuery();
			boolean isActivityRecordExisits = false;
			if(resultSet.next()){
				isActivityRecordExisits = resultSet.getLong(1) > 0;
			}
			LOGGER.debug("isActivityRecordExisits :: "+isActivityRecordExisits);
			return isActivityRecordExisits;
			
		}catch(Exception e){
			LOGGER.error("Exception in fetchning activity data of serial number",e);
			throw new SQLException();
		}finally{
			LOGGER.debug("Leaving StriivDeviceDAO.striivCheckActivityBySerialNumberAndStartTime method.");
			pStatement.close();
		}
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
		StriivDeviceDAO.connectionPool = connectionPool;
	}
	
	
	
	
}
