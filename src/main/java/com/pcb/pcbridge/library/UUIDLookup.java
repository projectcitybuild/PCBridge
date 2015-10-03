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
	//private final double RATE_LIMIT = 60;
	private final String API_URL = "https://api.mojang.com/profiles/minecraft";
	
	private JSONParser _jsonParser = new JSONParser();
	
	/**
	 * Retrieve the specified username's UUID from Mojang
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public UUID Query(String username) throws IOException, ParseException
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
