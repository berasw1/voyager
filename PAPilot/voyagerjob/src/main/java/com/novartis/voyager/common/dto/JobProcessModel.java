package com.novartis.voyager.common.dto;

public class JobProcessModel {
	
	private Long deviceDetailsId;
	private String serialNumber;
	private Long qclJsonLogId;
	
	public JobProcessModel(){
		
	}

	
	public JobProcessModel(Long deviceDetailsId, String serialNumber, Long qclJsonLogId){
		this.deviceDetailsId = deviceDetailsId;
		this.serialNumber = serialNumber;
		this.qclJsonLogId = qclJsonLogId;
	}
	
	
	public Long getDeviceDetailsId() {
		return deviceDetailsId;
	}

	public void setDeviceDetailsId(Long deviceDetailsId) {
		this.deviceDetailsId = deviceDetailsId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Long getQclJsonLogId() {
		return qclJsonLogId;
	}

	public void setQclJsonLogId(Long qclJsonLogId) {
		this.qclJsonLogId = qclJsonLogId;
	}
	
	

}
