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
import com.novartis.voyager.common.dto.Actigraph;
import com.novartis.voyager.common.dto.Actigraph.DeviceDetails;
import com.novartis.voyager.common.dto.Actigraph.QclBtlePacketHeaders.Packets;
import com.novartis.voyager.common.dto.Actigraph.Record;
import com.novartis.voyager.common.dto.Actigraph.Record.ActivityData;
import com.novartis.voyager.common.dto.Actigraph.Record.ActivityData.Value;
import com.novartis.voyager.common.dto.Actigraph.TwonetProperties;
import com.novartis.voyager.util.CommonUtil;


public class ActiGraphDeviceDAO {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ActiGraphDeviceDAO.class);
	
	private static DBConnection connectionPool;


	
	/**
	 * This method extracts all parts of Actigraph device JSON data and sends to store in different tables.
	 * 
	 * @param actigraph
	 * @param qclJsonLogId
	 * @throws SQLException
	 */
	public void processActiGraphDeviceData(Actigraph actigraph, Long qclJsonLogId) throws SQLException{
		Connection connection = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.processActiGraphDeviceData method.");
		try{
			connection = connectionPool.getConnection();
			
			//connection.setAutoCommit(false);
	//		Long qclJsonLogId = saveActivGraphQCLJsonData(actigraphJsonData, assetId, connection);
				
			 if(actigraph == null)
				 return;

			
			DeviceDetails deviceDetails = actigraph.deviceDetails;
			
			if (deviceDetails == null || deviceDetails.serialNumber == null
					|| CommonUtil.isNullOrBlank(deviceDetails.serialNumber.value)){
				LOGGER.debug("Serial number is blank ");
				return ;
			}
			
			String serialNumber = deviceDetails.serialNumber.value;
			LOGGER.debug("Serial Number of device :: "+serialNumber);
			
			Long deviceId = getActiGraphhDeviceIdBySerialNumber(serialNumber, connection);
			
			if(deviceId == null){
				deviceId = saveActiGraphDevice(serialNumber, connection);
			}
			
			LOGGER.debug("Device Id  :: "+deviceId);
			
			Long deviceDetailsId = saveActiGraphDeviceDetails(deviceDetails, deviceId, qclJsonLogId, connection);
			
			Record[] records = actigraph.records;
			if(records != null && records.length > 0){
				saveActiGraphRecords(records, deviceDetailsId, connection, serialNumber);
			}
			
			if( actigraph.qclBtlePacketHeaders != null && actigraph.qclBtlePacketHeaders.packets != null)
				saveActiGraphQclBtlePackets(actigraph.qclBtlePacketHeaders.packets, deviceDetailsId, connection);
			
			if(actigraph.twonetProperties != null)
				saveActiGraphTwonetProperties(actigraph.twonetProperties, deviceDetailsId, connection);
			//connection.commit();
		}catch(Exception e) {
			LOGGER.error("Exception in storing Actigraph job data",e);
			connection.rollback();
		}finally{
			if(connection!=null){
				connection.close();
			}
		//	connection.setAutoCommit(true);
			LOGGER.debug("Leaving ActiGraphDeviceDAO.processActiGraphDeviceData method.");
		}
		
	}
	
	
	/**
	 * Method to save the Actigraph device details sending data to Voyager.
	 * @param serialNumber
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long saveActiGraphDevice(String serialNumber, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.saveActiGraphDevice method.");
		try{
			pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_DEVICE_INSERT_QRY, new String[] { "ACTIGRAPH_DEVICE_ID" });
			pStatement.setString(1, serialNumber);
			
			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			
			ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
			Long deviceId = null;
			if(null != generatedSeqNum && generatedSeqNum.next()){
				deviceId = generatedSeqNum.getLong(1);
			}
			LOGGER.debug("Leaving ActiGraphDeviceDAO.saveActiGraphDevice method.");
			return deviceId;
		}catch(Exception e){
			LOGGER.error("Exception is saving device details of Actigraph. Error :: ",e);
			throw new SQLException();
		}finally{
			pStatement.close();
		}
	}
	
	/**
	 * Method to check if an Actigraph device with same serial number has already sent data.
	 * 
	 * @param serialNumber
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long getActiGraphhDeviceIdBySerialNumber(String serialNumber, Connection connection) throws SQLException{
		PreparedStatement statement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.getActiGraphhDeviceIdBySerialNumber method.");
		ResultSet resultSet = null;
		try{
			statement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.GET_ACTIGRAPH_DEVICE_ID_BY_SERIAL_NUMBER);
			statement.setString(1, serialNumber);
			 resultSet = statement.executeQuery();
			Long deviceId = null;
			if(resultSet.next()){
				deviceId = resultSet.getLong(1);
				
			}
			
			LOGGER.info("Device serial number = :: "+serialNumber+" Device Id = :: "+deviceId);
			LOGGER.debug("Leaving ActiGraphDeviceDAO.getActiGraphhDeviceIdBySerialNumber method.");
			return deviceId;
		}catch(Exception e){
			LOGGER.error("Exception in fetching serial device id of devices based on serial number.",e);
			throw new SQLException();
		}finally{
			if(resultSet!=null){
				resultSet.close();
			}
			if(statement!=null){
			statement.close();
			}
		}
	}

	/**
	 * Method to store the device details of Actigraph devices.
	 * 
	 * @param deviceDetails
	 * @param deviceId
	 * @param qclJsonLogId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private Long saveActiGraphDeviceDetails(DeviceDetails deviceDetails, Long deviceId, Long qclJsonLogId,
			Connection connection) throws SQLException {
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.saveActiGraphDeviceDetails method.");
		try {
			
			pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_DEVICE_DETAILS_INSERT_QRY,
					new String[] { "ACTIGRAPH_DEVICE_DETAILS_ID" });

		
			if (deviceDetails.currentTimeOnDevice != null){
			
			pStatement.setString(1, deviceDetails.currentTimeOnDevice.value);
			pStatement.setString(2, deviceDetails.currentTimeOnDevice.unit);
			}
			if (deviceDetails.timeOffsetFromGMT != null){
				pStatement.setLong(3, deviceDetails.timeOffsetFromGMT.value);
				pStatement.setString(4,  deviceDetails.timeOffsetFromGMT.unit);
			}else{
				pStatement.setNull(3, Types.LONGVARCHAR);
				pStatement.setNull(4, Types.VARCHAR);
			}
			
			
			if (deviceDetails.hardfaultResets != null){
				pStatement.setLong(5,  deviceDetails.hardfaultResets.value);
			}else{
				pStatement.setNull(5, Types.LONGVARCHAR);
			}
			
			if (deviceDetails.watchdogResets != null){
				
				pStatement.setLong(6,  deviceDetails.watchdogResets.value);
			}else{
				pStatement.setNull(6, Types.LONGVARCHAR);
			}
			
			if (deviceDetails.unexpectedResets != null){
				pStatement.setLong(7, deviceDetails.unexpectedResets.value);
				
			}else{
				pStatement.setNull(7, Types.LONGVARCHAR);
			}
			
			if (deviceDetails.haltState != null){
				pStatement.setLong(8, deviceDetails.haltState.value);
				pStatement.setString(9, deviceDetails.haltState.description);
			}else{
				pStatement.setNull(8, Types.LONGVARCHAR);
				pStatement.setNull(9, Types.VARCHAR);
			}
			
			
			if (deviceDetails.errorState != null){
				pStatement.setLong(10, deviceDetails.errorState.value);
				pStatement.setString(11, deviceDetails.errorState.description);
			}else{
				pStatement.setNull(10, Types.LONGVARCHAR);
				pStatement.setNull(11, Types.VARCHAR);
			}
			
			
			if (deviceDetails.deviceState != null){
				pStatement.setLong(12, deviceDetails.deviceState.value);
				pStatement.setString(13, deviceDetails.deviceState.description);
			}else{
				pStatement.setNull(12, Types.LONGVARCHAR);
				pStatement.setNull(13, Types.VARCHAR);
			}
			
			
			if (deviceDetails.batteryState!= null){
				pStatement.setLong(14, deviceDetails.batteryState.value);
				pStatement.setString(15, deviceDetails.batteryState.description);
			}else{
				pStatement.setNull(14, Types.LONGVARCHAR);
				pStatement.setNull(15, Types.VARCHAR);
			}
			
			
			if (deviceDetails.sampleRate != null){
				pStatement.setLong(16,  deviceDetails.sampleRate.value);
				pStatement.setString(17, deviceDetails.sampleRate.unit);
			}else{
				pStatement.setNull(16, Types.LONGVARCHAR);
				pStatement.setNull(17, Types.VARCHAR);
			}
			
			
			if (deviceDetails.batteryLevel != null) {
				pStatement.setDouble(18,  deviceDetails.batteryLevel.value);
				pStatement.setString(19, deviceDetails.batteryLevel.unit);
			}else{
				pStatement.setNull(18, Types.DOUBLE);
				pStatement.setNull(19, Types.VARCHAR);
			}
			
			
			if (deviceDetails.firmwareBuildVersion != null){
				pStatement.setLong(21, deviceDetails.firmwareBuildVersion.value);
			}else{
				pStatement.setNull(21, Types.LONGVARCHAR);
			}
			if (deviceDetails.firmwareMinorRevision != null){
				pStatement.setLong(22, deviceDetails.firmwareMinorRevision.value);
			}else{
				pStatement.setNull(22, Types.LONGVARCHAR);
			}
			if (deviceDetails.firmwareMajorRevision != null) {
				pStatement.setLong(23, deviceDetails.firmwareMajorRevision.value);
			}else{
				pStatement.setNull(23, Types.LONGVARCHAR);
			}
			if (deviceDetails.decoderModel != null){
			}
			pStatement.setString(20, deviceDetails.decoderModel.value);
			
			pStatement.setLong(24, deviceId);
			pStatement.setLong(25, qclJsonLogId);

			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			
			ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
			
			Long deviceDetailsId = null;
			if(null != generatedSeqNum && generatedSeqNum.next()){
				deviceDetailsId = generatedSeqNum.getLong(1);
				LOGGER.info("deviceDetailsId after storing device details of Actigraph : "+deviceDetailsId);
			}
			LOGGER.debug("Leaving ActiGraphDeviceDAO.saveActiGraphDeviceDetails method.");
			return deviceDetailsId;
		} catch (Exception e) {
			LOGGER.error("Exception in saving Actigraph device details :: ",e);
			throw new SQLException();
		}finally{
			pStatement.close();
		}
	}
	
	
	/**
	 * Method to save the records of Actrigraph devices.
	 * 
	 * @param records
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public boolean saveActiGraphRecords(Record[] records, Long deviceDetailsId,
			Connection connection, String serialNumber) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.info("Entering ActiGraphDeviceDAO.saveActiGraphRecords method.");
		try{
			for(Record record : records){
				String  packetType        = null;
				String  recordDate 		  = null;
				String  recordDateUnit    = null;
				Boolean isCurrentLocalDay = null;

				pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_RECORD_INSERT_QRY, new String[] { "ACTIGRAPH_RECORD_ID" });
				
				if(record.packetType != null){
					packetType = record.packetType.value;
				}
				if(record.date != null){
					recordDate = record.date.value;
					recordDateUnit = record.date.unit;
				}
				
				pStatement.setString(1, packetType);
				pStatement.setString(2, recordDate);
				pStatement.setString(3, recordDateUnit);
				
				if(record.isCurrentLocalDay != null){
					isCurrentLocalDay = record.isCurrentLocalDay.value;
					pStatement.setBoolean(4, isCurrentLocalDay);
				}else{
					pStatement.setBoolean(4, false);
				}
				pStatement.setLong(5, deviceDetailsId);

				if(pStatement.executeUpdate() == 0){
					
					throw new SQLException();
				}
				ResultSet generatedSeqNum = pStatement.getGeneratedKeys();
				Long recordId = null;
				if(null != generatedSeqNum && generatedSeqNum.next()){
					recordId = generatedSeqNum.getLong(1);
				}
				LOGGER.info("Record id for ACTIGRAPH device ID :: "+deviceDetailsId +" is :: "+recordId);
				if (record.activityData != null && record.activityData.value != null
						&& record.activityData.value.length > 0){
					saveActiGraphActivityData(record.activityData, recordId, connection, serialNumber);
				}
				
			}
			LOGGER.info("Leaving ActiGraphDeviceDAO.saveActiGraphRecords method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in storing Actigraph records data :: ",e);
			throw new SQLException();
		}finally{
			pStatement.close();
		}
	}	

	/**
	 * Method to save activity data of Actograph devices.
	 * 
	 * @param activityData
	 * @param recordId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveActiGraphActivityData(ActivityData activityData, Long recordId, Connection connection, String serialNumber) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.saveActiGraphActivityData method.");
		Boolean ischeckRequired = true;
		try{
			Value[] activityValues = activityData.value;
			pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_ACTIVITY_INSERT_QRY);
			
			int i =0;
			for(Value activityValue : activityValues){
				
			 
				
				if(activityValue.startTime != null){
					
					pStatement.setString(1, activityValue.startTime.value);
					pStatement.setString(2, activityValue.startTime.unit);
					if(ischeckRequired){
						Boolean duplicateStatus = actigraphCheckActivityBySerialNumberAndStartTime(connection, serialNumber, activityValue.startTime.value);
					 LOGGER.debug("duplicateStatus :: "+duplicateStatus);
					 	if(duplicateStatus){
					 		continue;
					 	}else{
					 		ischeckRequired =false;
					 	}
					 	pStatement.setBoolean(14, duplicateStatus);	
					}
					pStatement.setBoolean(14, false);
				}else{
					pStatement.setNull(1, Types.VARCHAR);
					pStatement.setNull(2, Types.VARCHAR);
				}
				
				if(activityValue.interval != null){
					
					
					pStatement.setLong(3, activityValue.interval.value);
					pStatement.setString(4, activityValue.interval.unit);
				}else{
					pStatement.setNull(3, Types.LONGVARCHAR);
					pStatement.setNull(4, Types.VARCHAR);
				}
				
				
				if(activityValue.steps != null){
					pStatement.setLong(5,  activityValue.steps.value);
					
				}
				if(activityValue.wearDetection != null){
					pStatement.setLong(6, activityValue.wearDetection.value);
					pStatement.setString(7, activityValue.wearDetection.description);
				}else{
					pStatement.setNull(6, Types.LONGVARCHAR);
					pStatement.setNull(7, Types.VARCHAR);
				}
				if(activityValue.x != null){
					pStatement.setLong(8, activityValue.x.value);
				}else{
					pStatement.setNull(8, Types.LONGVARCHAR);
				}
				if(activityValue.y != null){
					pStatement.setLong(9, activityValue.y.value);
				}else{
					pStatement.setNull(9, Types.LONGVARCHAR);
				}
				if(activityValue.z != null){
					pStatement.setLong(10, activityValue.z.value);
				}else{
					pStatement.setNull(10, Types.LONGVARCHAR);
				}
				
				pStatement.setLong(11, recordId);
				
				if(activityValue.caloriesBurned != null){
					
					pStatement.setLong(12, activityValue.caloriesBurned.value);
					pStatement.setString(13, activityValue.caloriesBurned.unit);
				}else{
					pStatement.setNull(12, Types.LONGVARCHAR);
					pStatement.setNull(13, Types.VARCHAR);					
				}
				
				Boolean duplicateStatus =false;
				//Boolean duplicateStatus = actigraphCheckActivityBySerialNumberAndStartTime(connection, serialNumber, activityValue.startTime.value);
				LOGGER.debug("duplicateStatus :: "+duplicateStatus);
				pStatement.setBoolean(14, duplicateStatus);
				
				i++;
				pStatement.addBatch();
				if(i%10==0){
					pStatement.executeBatch();
					pStatement.clearBatch();
				}
			}
			if(i%10!=0){
				pStatement.executeBatch();
			}
			LOGGER.info("The ACTIGRAPH activity data saved for record id :: "+recordId);
			LOGGER.debug("Leaving ActiGraphDeviceDAO.saveActiGraphActivityData method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in storing activity data of actigraph device records :: ",e);
			throw new SQLException();
		}finally {

			if (pStatement != null) {
				pStatement.close();
			}
		}

	}
	
	/**
	 * Method to store the QclBtlePackets details of Actigraph devices.
	 * 
	 * @param packets
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveActiGraphQclBtlePackets(Packets packets, Long deviceDetailsId, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.saveActiGraphQclBtlePackets method.");
		try{
			if(packets != null && packets.value != null && packets.value.length > 0){
				pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_QCL_BTLE_PACKET_INSERT_QRY);
				com.novartis.voyager.common.dto.Actigraph.QclBtlePacketHeaders.Packets.Value[] packetValues = packets.value;
				for (com.novartis.voyager.common.dto.Actigraph.QclBtlePacketHeaders.Packets.Value packetValue : packetValues) {
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
			LOGGER.debug("Leaving ActiGraphDeviceDAO.saveActiGraphQclBtlePackets method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in storing ActiGraphQclBtlePackets data ",e);
			throw new SQLException();
		}
		
		
	}
	
	/**
	 * Method to save the TwonetProperties details of Actigraph devices.
	 * 
	 * @param tProperties
	 * @param deviceDetailsId
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	private boolean saveActiGraphTwonetProperties(TwonetProperties tProperties, Long deviceDetailsId, Connection connection) throws SQLException{
		PreparedStatement pStatement = null;
		LOGGER.debug("Entering ActiGraphDeviceDAO.saveActiGraphTwonetProperties method.");
		try{
		 	
			 	
		 	
		 	pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_TWONET_PROPERTIES_INSERT_QRY);

		 	
			if (tProperties.airInterfaceType != null){
				pStatement.setString(1, tProperties.airInterfaceType.value.toString());
			}else{
				pStatement.setNull(1, Types.VARCHAR);
			}
			if (tProperties.cdeVersion != null){
			pStatement.setString(2,  tProperties.cdeVersion.value.toString());
			
			}else{
				pStatement.setNull(2, Types.VARCHAR);
			}
			if (tProperties.customerId != null){
				pStatement.setString(3,  tProperties.customerId.value.toString());
				
			}else{
				pStatement.setNull(3, Types.VARCHAR);
			}
			if (tProperties.customerName != null){
				pStatement.setString(4, tProperties.customerName.value.toString());
				
			}else{
				pStatement.setNull(4, Types.VARCHAR);
			}
			if (tProperties.deviceAddress != null){
				pStatement.setString(5, tProperties.deviceAddress.value.toString());
				
			}else{
				pStatement.setNull(5, Types.VARCHAR);
			}
			if (tProperties.deviceData != null){
				Clob myClob = connection.createClob();
				Writer clobWriter = myClob.setCharacterStream(1);
				StringBuffer sb = new StringBuffer();
				clobWriter.write(tProperties.deviceData.value);
				sb.append(tProperties.deviceData.value);
				myClob.setString(6, sb.toString());
				pStatement.setClob(6, myClob);
			}
			if (tProperties.deviceModel != null){
				pStatement.setString(7, tProperties.deviceModel.value.toString());
				
			}else{
				pStatement.setNull(7, Types.VARCHAR);
			}
			if (tProperties.deviceSerialNumber != null){
				pStatement.setString(8, tProperties.deviceSerialNumber.value.toString());
				
			}else{
				pStatement.setNull(8, Types.VARCHAR);
			}
			if (tProperties.deviceType != null){
				pStatement.setString(9, tProperties.deviceType.value.toString());
				
			}else{
				pStatement.setNull(9, Types.VARCHAR);
			}
			if (tProperties.hubId != null){
				pStatement.setString(10, tProperties.hubId.value.toString());
			}else{
				pStatement.setNull(10, Types.VARCHAR);
			}
			if (tProperties.hubReceiveTimeOffset != null){
				pStatement.setLong(11, tProperties.hubReceiveTimeOffset.value);
				
			}else{
				pStatement.setNull(11, Types.VARCHAR);
			}
			if (tProperties.twonetId != null){
				pStatement.setString(12, tProperties.twonetId.value.toString());
			
			}else{
				pStatement.setNull(12, Types.VARCHAR);
			}
			if (tProperties.timeZone != null){
				pStatement.setString(13, tProperties.timeZone.value.toString());
			}else{
				pStatement.setNull(13, Types.VARCHAR);
			}
			if (tProperties.hubReceiveTime != null){
				//pStatement.setTimestamp(14, new Timestamp(hubReceiveTime));
				pStatement.setLong(14, tProperties.hubReceiveTime.value);
			}else{
				pStatement.setNull(14, Types.VARCHAR);
			}
			if (tProperties.spReceiveTime != null){
				pStatement.setLong(15, tProperties.spReceiveTime.value);
			//	pStatement.setTimestamp(15, new Timestamp(spReceiveTime));
			}else{
				pStatement.setNull(15, Types.VARCHAR);
			}
			if(tProperties.exporterVersion != null){
				pStatement.setString(16,  tProperties.exporterVersion.value);
			}else{
				pStatement.setNull(16, Types.VARCHAR);
			}
			
			if(tProperties.decoderVersion != null){
				pStatement.setString(18, tProperties.decoderVersion.value);
			}else{
				pStatement.setNull(18, Types.VARCHAR);
			}
			pStatement.setLong(17, deviceDetailsId);
			
			if(pStatement.executeUpdate() == 0)
				throw new SQLException();
			LOGGER.debug("Leaving ActiGraphDeviceDAO.saveActiGraphTwonetProperties method.");
			return true;
		}catch(Exception e){
			LOGGER.error("Exception in storing ActiGraphTwonetProperties data ",e);
			throw new SQLException();
		}
	}

	
	/**
	 * This method is to check if Actigraph activity data already stored for a specific device(Serial Number) and a timestamp.
	 * 
	 * @param connection
	 * @param serialNumber
	 * @param startTime
	 * @return
	 * @throws SQLException
	 */
	private boolean actigraphCheckActivityBySerialNumberAndStartTime(Connection connection, String serialNumber, String startTime) throws SQLException{
		
		LOGGER.debug("Entering ActiGraphDeviceDAO.actigraphCheckActivityBySerialNumberAndStartTime method.");
		PreparedStatement pStatement = null;
		try{
			pStatement = connection.prepareStatement(QueryConstants.VoyagerActivGraphSqlConstants.ACTIGRAPH_CHECK_ACTIVITY_BY_SERIAL_NUMBER_AND_START_TIME);
			pStatement.setString(1, serialNumber);
			pStatement.setString(2, startTime);
			ResultSet resultSet = pStatement.executeQuery();
			boolean isActivityRecordExists = false;
			if(resultSet.next()){
				isActivityRecordExists = resultSet.getLong(1) > 0;
			}
			return isActivityRecordExists;
		}catch(Exception e){
			LOGGER.error("Exception in fetching existing activity data from database ",e);
			throw new SQLException();
		}finally{
			pStatement.close();
			LOGGER.debug("Leaving ActiGraphDeviceDAO.actigraphCheckActivityBySerialNumberAndStartTime method.");
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
		ActiGraphDeviceDAO.connectionPool = connectionPool;
	}
	
	
	
}
