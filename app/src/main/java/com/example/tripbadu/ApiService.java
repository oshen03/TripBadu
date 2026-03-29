package com.example.tripbadu;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/gear")
    Call<List<Gear>> getGear();

    @POST("api/checkout")
    Call<Void> processCheckout(@Body Order order);

    @Multipart
    @POST("api/ads")
    Call<Void> uploadAd(
            @Part("owner_id") RequestBody ownerId,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part MultipartBody.Part image
    );

    @GET("api/admin/ads/pending")
    Call<List<Gear>> getPendingAds();

    @PUT("api/admin/ads/{id}/approve")
    Call<Void> approveAd(@Path("id") int id);

    @DELETE("api/admin/ads/{id}")
    Call<Void> rejectAd(@Path("id") int id);
}
