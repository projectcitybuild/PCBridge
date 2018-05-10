package com.pcb.pcbridge.archived.pcbridge.models;

import com.google.gson.annotations.SerializedName;

public class MinecraftAuthError {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("detail")
    private String message;

    @SerializedName("status")
    private int code;


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}
