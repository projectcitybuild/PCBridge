package com.pcb.pcbridge.archived;

import com.google.gson.annotations.SerializedName;

public class MinecraftAuthResult {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("admin")
    private boolean isAdmin;

    @SerializedName("active")
    private boolean isActive;

    @SerializedName("suspended")
    private boolean isSuspended;


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isSuspended() {
        return isSuspended;
    }



}
