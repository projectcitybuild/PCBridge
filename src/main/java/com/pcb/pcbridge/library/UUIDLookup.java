package com.pcb.pcbridge.library;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class UUIDLookup 
{
	private final String API_UUID_URL = "https://api.mojang.com/profiles/";
	private final String API_USER_URL = "https://api.mojang.com/user/profiles/";
	private final int RATE = 60; 	// ___ requests available per
	private final int PER = 60; 	// per ___ seconds
	
	private JSONParser _jsonParser = new JSONParser();
	private double allowance = RATE;
	private long lastCheck;
	
	/**
	 * Throttles lookup requests at the specified rate
	 * 
	 * @param username
	 * @throws IOException
	 * @throws ParseException 
	 */
	private void Throttle()
	{
		long now = System.currentTimeMillis();
		long timePassed = now - lastCheck;
		lastCheck = now;
		
		// refill available requests (capped at RATE limit)
		allowance += timePassed * (RATE / PER);
		if(allowance > RATE)
		{
			allowance = RATE;
		}
		// sleep thread if no requests available
		if(allowance < 1.0)
		{
			try 
			{
				Thread.sleep(1);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		allowance -= 1.0;
	}
	
	/**
	 * Retrieve the specified username's current UUID from Mojang
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public UUID GetCurrentUUID(String username) throws IOException, ParseException
	{
		Throttle();
		
		HttpURLConnection connection = NewConnection(API_UUID_URL + "minecraft", "POST");
		
		// send the username
		SendInput(connection, username);
		
		// parse the response
		InputStreamReader reader = new InputStreamReader( connection.getInputStream() );
		JSONArray array = (JSONArray) _jsonParser.parse(reader);
		String uuid = null;
        for (Object profile : array) 
        {		
        	JSONObject player = (JSONObject) profile;
        	uuid = (String) player.get("id");		
        }
        
        return ParseUUID(uuid);
	}
	
	public HashMap<String, Long> GetNameHistory(String uuid) throws IOException, ParseException 
	{
		Throttle();
		
		// strip hyphens out of the uuid
		uuid = uuid.replace("-", "");

		HttpURLConnection connection = NewConnection(API_USER_URL + uuid + "/names", "GET");
		connection.connect();		
			
		// parse the response
		HashMap<String, Long> results = new HashMap<String, Long>();
		InputStreamReader reader = new InputStreamReader( connection.getInputStream() );
		JSONArray array = (JSONArray) _jsonParser.parse(reader);
        for (Object profile : array) 
        {		
        	JSONObject player = (JSONObject) profile;
        	
        	String name = (String) player.get("name");	
        	Long date = (Long) player.get("changedToAt");
        	
        	results.put(name, date);
        }   
		
        return results;
	}
	
	/**
	 * Create a new connection to the Mojang API via HTTP
	 * 
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection NewConnection(String apiUrl, String requestMethod) throws IOException
	{
		URL url = new URL(apiUrl);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(requestMethod);		
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setReadTimeout(8000);
		
		connection.setDoOutput(requestMethod == "POST");
		
		return connection;
	}
	
	private HttpURLConnection NewConnection(String apiUrl) throws IOException
	{
		return NewConnection(apiUrl, "GET");
	}
	
	/**
	 * Post data to the connection as a JSON array
	 * 
	 * @param connection
	 * @param data
	 * @throws IOException
	 */
	private void SendInput(HttpURLConnection connection, String data) throws IOException
	{
		JSONArray inputArray = new JSONArray();
		inputArray.add(data);
		String body = inputArray.toJSONString();
		
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
	}
	
	private UUID ParseUUID(String uuid)
	{
		if(uuid == null)
			return null;
		
		return UUID.fromString(
				uuid.substring(0, 8) + "-" + 
				uuid.substring(8, 12) + "-" + 
				uuid.substring(12, 16) + "-" + 
				uuid.substring(16, 20) + "-" + 
				uuid.substring(20, 32)
			   );
	}
	
}
