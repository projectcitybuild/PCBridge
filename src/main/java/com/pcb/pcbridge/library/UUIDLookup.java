package com.pcb.pcbridge.library;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class UUIDLookup 
{
	private final String API_URL = "https://api.mojang.com/profiles/minecraft";
	private final int RATE = 60; 	// requests available
	private final int PER = 60; 	// per X seconds
	
	private JSONParser _jsonParser = new JSONParser();
	private double allowance = RATE;
	private long lastCheck;
	
	/**
	 * Throttles lookups at the specified rate
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public UUID Query(String username) throws IOException, ParseException
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
		
		return GetUUID(username);
	}
	
	/**
	 * Retrieve the specified username's UUID from Mojang
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	private UUID GetUUID(String username) throws IOException, ParseException
	{
		HttpURLConnection connection = NewConnection();
		
		// send the username
		JSONObject input = new JSONObject();
		JSONArray inputArray = new JSONArray();
		inputArray.add(username);
		String body = inputArray.toJSONString();
		
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
		
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
	
	/**
	 * Create a new connection to the Mojang API via HTTP
	 * 
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection NewConnection() throws IOException
	{
		URL url = new URL(API_URL);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);;
		connection.setDoOutput(true);
		
		return connection;
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
