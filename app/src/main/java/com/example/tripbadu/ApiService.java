package com.example.tripbadu;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/gear")
    Call<List<Gear>> getGear();

    @POST("api/checkout")
    Call<Void> processCheckout(@Body Order order);
}
