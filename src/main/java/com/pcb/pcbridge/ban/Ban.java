package com.pcb.pcbridge.ban;

import java.util.HashMap;

/**
 * Data Transfer Object to represent a single ban record
 */

public final class Ban 
{
	public final Integer Id;
	public final String UUID;
	public final String Name;
	public final Long BanDate;
	public final Long ExpiryDate;
	public final String StaffUUID;
	public final String StaffName;
	public final String Reason;
	public final String IP;
	public final Boolean IsActive;
	
	public Ban(HashMap<String, Object> record)
	{
		Id 			= (Integer) record.get("id");
		UUID 		= (String) record.get("banned_uuid");
		Name 		= (String) record.get("banned_name");
		BanDate 	= (Long) record.get("date_ban");
		ExpiryDate 	= (Long) record.get("date_expire");
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
}
