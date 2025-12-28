package com.example.ProjectManager.api;

import com.example.ProjectManager.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a configured Retrofit instance. Not used yet.
 */
public final class RetrofitClient {
    private static volatile Retrofit retrofit;

    private RetrofitClient() {
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build();
                }
            }
        }
        return retrofit;
    }

    // Example usage when backend is ready:
    // ApiService api = getInstance().create(ApiService.class);
}
