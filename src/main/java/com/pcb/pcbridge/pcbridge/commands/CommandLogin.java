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
package com.pcb.pcbridge.pcbridge.commands;

import com.google.gson.Gson;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.pcbridge.api.MinecraftAuthApi;
import com.pcb.pcbridge.pcbridge.models.MinecraftAuthBaseResponse;
import com.pcb.pcbridge.pcbridge.models.MinecraftAuthError;
import com.pcb.pcbridge.pcbridge.models.MinecraftAuthResult;
import com.pcb.pcbridge.utils.commands.AbstractCommand;
import com.pcb.pcbridge.utils.commands.CommandArgs;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class CommandLogin extends AbstractCommand {

	@Override
	public String GetName() 
	{
		return "login";
	}
	
	@Override
	public String GetDescription() 
	{
		return "Syncs your in-game rank with your PCB forum rank";
	}

	@Override
	public String GetPermission() 
	{
		return "pcbridge.login";
	}

	@Override
	public String GetUsage()
	{
		return "/login <username> <password>";
	}

	@Override
	public boolean OnExecute(CommandArgs args) 
	{
		if(!args.IsPlayer()) {
			args.GetSender().sendMessage("Only in-game players can use this command.");
			return true;
		}

		if(args.GetArgs().length != 2) {
			return false;
		}

		String email = args.GetArg(0);
		String password = args.GetArg(1);

		attemptLogin(email, password)
				.thenAccept(response -> {

					if(response.isSuccessful() == false) {
						String json = null;
						try {
							json = response.errorBody().string();
						} catch(IOException e) {
							throw new CompletionException(e);
						}

						Gson gson = new Gson();
						MinecraftAuthBaseResponse result = gson.fromJson(json, MinecraftAuthBaseResponse.class);
						MinecraftAuthError error = result.getError();

						Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
							args.GetPlayer().sendMessage(ChatColor.RED + "Login failed: " + error.getMessage());
						});

						return;
					}

					Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
						MinecraftAuthBaseResponse result = response.body();
						List<MinecraftAuthResult> users = result.getData();

						if(users.size() == 0) {
							args.GetPlayer().sendMessage(ChatColor.RED + "An unknown error occured during login");
							return;
						}

						MinecraftAuthResult user = users.get(0);
						if(user.isActive() == false) {
							args.GetPlayer().sendMessage(ChatColor.RED + "Cannot authenticate - that account is suspended");
							return;
						}

						PCBridge.GetVaultHook().GetPermission().playerAddGroup(null, args.GetPlayer(), "Member");
						args.GetPlayer().sendMessage(ChatColor.GREEN + "Successfully logged-in - your rank has been set to Member");
					});

				})
				.exceptionally(error -> {
					Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
						args.GetSender().sendMessage(ChatColor.RED + "Login failed due to a server error. Please try again or let a staff member know.");
					});

					error.printStackTrace();
					return null;
				});

		return true;
	}

	private CompletableFuture<Response<MinecraftAuthBaseResponse>> attemptLogin(String email, String password) {
		CompletableFuture<Response<MinecraftAuthBaseResponse>> task = CompletableFuture.supplyAsync(() -> {

			OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("https://dev.projectcitybuild.com/api/")
					.client(httpClient.build())
					.addConverterFactory(GsonConverterFactory.create())
					.build();

			MinecraftAuthApi client = retrofit.create(MinecraftAuthApi.class);

			Call<MinecraftAuthBaseResponse> call = client.login(email, password);
			try {
				Response<MinecraftAuthBaseResponse> response = call.execute();
				return response;

			} catch(IOException e) {
				e.printStackTrace();
				throw new CompletionException(e);
			}
		});

		return task;
	}

}
