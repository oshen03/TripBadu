package com.example.tripbadu;

import com.google.gson.annotations.SerializedName;

public class ChargeRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("amount")
    private long amount; // Amount in cents

    public ChargeRequest(String token, long amount) {
        this.token = token;
        this.amount = amount;
    }

    public String getToken() {
        return token;
    }

    public long getAmount() {
        return amount;
    }
}
