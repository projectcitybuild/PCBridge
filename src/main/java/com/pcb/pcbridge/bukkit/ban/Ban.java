package com.pcb.pcbridge.bukkit.ban;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * DTO to represent a single ban record
 */

public class Ban 
{
	public Integer Id;
	public String Name;
	public Long BanDate;
	public Long ExpiryDate;
	public Integer Type;
	public String StaffName;
	public String Reason;
	public String IP;
	public Boolean IsActive;
	public String UnbannedBy;
	
	public Ban(String name, Long banDate, Long expiryDate, Integer type, String staffName, String reason, String ip, Boolean isActive) 
	{ 
		Name 		= name;
		BanDate 	= banDate;
		ExpiryDate 	= expiryDate;
		Type 		= type;
		StaffName 	= staffName;
		Reason 		= reason;
		IP 			= ip;
		IsActive	= isActive;
	}
	
	public Ban(HashMap<String, Object> record)
	{
		Id 			= (Integer) record.get("id");
		Name 		= (String) record.get("name");
		BanDate 	= (Long) record.get("time");
		ExpiryDate 	= (Long) record.get("temptime");
		Type 		= (Integer) record.get("type");
		StaffName 	= (String) record.get("admin");
		Reason 		= (String) record.get("reason");
		IP 			= (String) record.get("ip");
		IsActive	= (Boolean) record.get("is_active");
		UnbannedBy	= (String) record.get("unbanned_by");
	}
	
	public boolean IsTempBan()
	{
		return ExpiryDate != null;
	}
	
	@Override
	public String toString()
	{
		return MessageFormat.format("Id: {0}\nName: {1}\nBan Date: {2}\nExpiry Date: {3}\nStaff: {4}\nReason: {5}\nIP: {6}\nType: {7}", 
					Id, Name, BanDate, ExpiryDate, StaffName, Reason, IP, Type
				);
	}
}
