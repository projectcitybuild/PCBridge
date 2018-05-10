package com.pcb.pcbridge.archived.pcbridge.models;

import com.google.gson.annotations.Expose;

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
