package com.novartis.voyager.common.dto;

public class CommonJobProcessModel {
	
	
	private Long qclJsonLogId;
	private String qclData;
	public CommonJobProcessModel(){
		
	}

	
	public CommonJobProcessModel(Long qclJsonLogId, String qclData){
		this.qclJsonLogId = qclJsonLogId;
		this.qclData = qclData;
	}
	
	
	
	public Long getQclJsonLogId() {
		return qclJsonLogId;
	}

	public void setQclJsonLogId(Long qclJsonLogId) {
		this.qclJsonLogId = qclJsonLogId;
	}


	/**
	 * @return the qclData
	 */
	public String getQclData() {
		return qclData;
	}


	/**
	 * @param qclData the qclData to set
	 */
	public void setQclData(String qclData) {
		this.qclData = qclData;
	}
	
	

}
