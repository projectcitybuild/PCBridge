/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.tasks;

import java.util.UUID;

import com.pcb.pcbridge.Environment;
import com.pcb.pcbridge.models.PlayerConfig;
import com.pcb.pcbridge.utils.cache.StaticCache;

public class GetPlayerCacheTask {
	
	/**
	 * Fetches a player's config from the cache (or puts it there if not found)
	 */
	public static PlayerConfig Get(Environment env, UUID uuid)
	{
		StaticCache<UUID, PlayerConfig> cache = env.GetPlayerCache();
		
		PlayerConfig config = cache.Get(uuid);
		if(config == null)
			config = new PlayerConfig(env.GetPlayerFolder(), uuid);
		
		return config;
	}
	
}
