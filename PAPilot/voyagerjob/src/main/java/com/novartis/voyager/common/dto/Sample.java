package com.novartis.voyager.common.dto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oracle.sql.TIMESTAMP;

public class Sample {
	
	public static void main(String[] args) throws Exception, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		

		//JSON from file to Object
		Actigraph obj = mapper.readValue(new File("c:\\Users\\BERASW1\\acti.json"), Actigraph.class);

		System.out.println("after parsing "+obj.qclJsonVersion +"   :::   "+obj.records[1].activityData.value[0].y.value);
	/*	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	//	java.sql.Date date = (java.sql.Date) dateFormat.parse();
		java.util.Date date = dateFormat.parse(obj.records[0].date.value);
		java.sql.Date sqlStartDate = new java.sql.Date(date.getTime());  
		System.out.println(sqlStartDate);
		
		Long time = obj.twonetProperties.spReceiveTime.value;
		
		System.out.println(new TIMESTAMP(time.toString()));*/
		
		
	//	Striiv obj = mapper.readValue(new File("c:\\Users\\BERASW1\\striiv.json"), Striiv.class);
	//	System.out.println("after parsing "+obj.qclJsonVersion +"   :::   "+obj.twonetProperties.spReceiveTime.value);
	}

}
