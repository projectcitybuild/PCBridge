package com.pcb.pcbridge.ban;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * DTO to represent a single ban record
 */

public final class Ban 
{
	public final Long Id;
	public final String UUID;
	public final String Name;
	public final Integer BanDate;
	public final Integer ExpiryDate;
	public final String StaffUUID;
	public final String StaffName;
	public final String Reason;
	public final String IP;
	public final Boolean IsActive;
	
	public Ban(HashMap<String, Object> record)
	{
		Id 			= (Long) record.get("id");
		UUID 		= (String) record.get("banned_uuid");
		Name 		= (String) record.get("banned_name");
		BanDate 	= (Integer) record.get("date_ban");
		ExpiryDate 	= (Integer) record.get("date_expire");
		StaffUUID 	= (String) record.get("staff_uuid");
		StaffName 	= (String) record.get("staff_name");
		Reason 		= (String) record.get("reason");
		IP 			= (String) record.get("ip");
		IsActive 	= (Boolean) record.get("is_active");
	}
	
	public boolean IsTempBan()
	{
		return ExpiryDate != null;
	}
	
	@Override
	public String toString()
	{
		return MessageFormat.format("Id: {0}\nUUID: {1}\nName: {2}\nBan Date: {3}\nExpiry Date: {4}\nStaff UUID: {5}\nStaff: {6}\nReason: {7}\nIP: {8}\nActive: {9}", 
					Id, UUID, Name, BanDate, ExpiryDate, StaffUUID, StaffName, Reason, IP, IsActive
				);
	}
}
