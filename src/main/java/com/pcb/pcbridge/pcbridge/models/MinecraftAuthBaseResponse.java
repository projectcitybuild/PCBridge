package com.pcb.pcbridge.pcbridge.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MinecraftAuthBaseResponse {

    @Expose
    private List<MinecraftAuthResult> data;

    @Expose
    private MinecraftAuthError error;

    public List<MinecraftAuthResult> getData() {
        return data;
    }

    public MinecraftAuthError getError() {
        return error;
    }

}
