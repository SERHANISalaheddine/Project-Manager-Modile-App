package com.example.ProjectManager.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.ProjectManager.activities.OnboardingActivity;
import com.example.ProjectManager.utils.SharedPrefsManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp Interceptor that adds JWT token to all API requests
 * and handles 401 Unauthorized responses (redirects to login).
 */
public class AuthInterceptor implements Interceptor {
    private final Context context;
    private final SharedPrefsManager prefsManager;
    private final Handler mainHandler;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.prefsManager = SharedPrefsManager.getInstance(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Get the token from SharedPreferences
        String token = prefsManager.getAuthToken();

        // Add token to request if available
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        Request newRequest = requestBuilder.build();
        Response response = chain.proceed(newRequest);

        // Handle 401 Unauthorized (token expired or invalid)
        if (response.code() == 401) {
            // Clear user data
            prefsManager.clearUserData();

            // Redirect to OnboardingActivity on the main thread
            mainHandler.post(() -> {
                Intent intent = new Intent(context, OnboardingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            });
        }

        return response;
    }
}
