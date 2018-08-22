package com.pcb.pcbridge.spigot.ranks.commands;

import com.pcb.pcbridge.framework.commands.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandLogin extends AbstractCommand {

	@Override
	public String getName() {
		return "login";
	}

	@Override
	public String getPermissionNode()  {
		return "pcbridge.login";
	}

//	@Override
//	public String GetUsage() {
//		return "/login <email> <password>";
//	}

	@Override
	public boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
//		if(!args.IsPlayer()) {
//			args.GetSender().sendMessage("Only in-game players can use this command.");
//			return true;
//		}
//
//		if(args.GetArgs().length != 2) {
//			return false;
//		}
//
//		String email = args.GetArg(0);
//		String password = args.GetArg(1);
//
//		attemptLogin(email, password)
//				.thenAccept(response -> {
//
//					if(response.isSuccessful() == false) {
//						String json = null;
//						try {
//							json = response.errorBody().string();
//						} catch(IOException e) {
//							throw new CompletionException(e);
//						}
//
//						Gson gson = new Gson();
//						MinecraftAuthBaseResponse result = gson.fromJson(json, MinecraftAuthBaseResponse.class);
//						MinecraftAuthError error = result.getError();
//
//						Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
//							args.GetPlayer().sendMessage(ChatColor.RED + "Login failed: " + error.getMessage());
//						});
//
//						return;
//					}
//
//					Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
//						MinecraftAuthBaseResponse result = response.body();
//						List<MinecraftAuthResult> users = result.getData();
//
//						if(users.size() == 0) {
//							args.GetPlayer().sendMessage(ChatColor.RED + "An unknown error occured during login");
//							return;
//						}
//
//						MinecraftAuthResult user = users.get(0);
//						if(user.isActive() == false) {
//							args.GetPlayer().sendMessage(ChatColor.RED + "Cannot authenticate - that account is suspended");
//							return;
//						}
//
//						PCBridge.GetVaultHook().GetPermission().playerAddGroup(null, args.GetPlayer(), "Member");
//						args.GetPlayer().sendMessage(ChatColor.GREEN + "Successfully logged-in - your rank has been set to Member");
//					});
//
//				})
//				.exceptionally(error -> {
//					Bukkit.getScheduler().scheduleSyncDelayedTask(GetEnv().GetPlugin(), () -> {
//						args.GetSender().sendMessage(ChatColor.RED + "Login failed due to a server error. Please try again or let a staff member know.");
//					});
//
//					error.printStackTrace();
//					return null;
//				});

		return true;
	}

//	private CompletableFuture<Response<MinecraftAuthBaseResponse>> attemptLogin(String email, String password) {
//		CompletableFuture<Response<MinecraftAuthBaseResponse>> task = CompletableFuture.supplyAsync(() -> {
//
//			OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//
//			Retrofit retrofit = new Retrofit.Builder()
//					.baseUrl("https://projectcitybuild.com/api/")
//					.client(httpClient.build())
//					.addConverterFactory(GsonConverterFactory.create())
//					.build();
//
//			MinecraftAuthApi client = retrofit.create(MinecraftAuthApi.class);
//
//			Call<MinecraftAuthBaseResponse> call = client.login(email, password);
//			try {
//				Response<MinecraftAuthBaseResponse> response = call.execute();
//				return response;
//
//			} catch(IOException e) {
//				e.printStackTrace();
//				throw new CompletionException(e);
//			}
//		});
//
//		return task;
//	}

}
