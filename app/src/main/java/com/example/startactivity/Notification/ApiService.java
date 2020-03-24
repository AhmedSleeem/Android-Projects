package com.example.startactivity.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAANYIilmo:APA91bGfTBnNogYnkqCccAcylVlbfS_t_HraWFIvuaU02nAKKpeHcnur_JSYaf-cJRXaatBg1jWCefRgugCIKLq3x8nIjyE183DN-jjE5D4aC6DqYKSAGweYYx8bGdr-EWZEaLwgy_Zk"

            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
