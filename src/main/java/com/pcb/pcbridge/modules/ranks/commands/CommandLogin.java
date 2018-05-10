package com.pcb.pcbridge.modules.ranks.commands;

import com.google.gson.Gson;
import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.modules.ranks.api.MinecraftAuthApi;
import com.pcb.pcbridge.archived.pcbridge.models.MinecraftAuthBaseResponse;
import com.pcb.pcbridge.archived.pcbridge.models.MinecraftAuthError;
import com.pcb.pcbridge.archived.pcbridge.models.MinecraftAuthResult;
import com.pcb.pcbridge.archived.utils.commands.AbstractCommand;
import com.pcb.pcbridge.archived.utils.commands.CommandArgs;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
					.baseUrl("https://projectcitybuild.com/api/")
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
