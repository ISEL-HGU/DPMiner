package edu.handong.csee.isel.repo.control;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroBasic {
	
	//Retrofit turns your REST API into a Java interface.
	private Retrofit retrofit;
	private static final String BASE_URL = "https://api.github.com/";
	
	public Retrofit createObject(String token) {
		
		if (!token.isEmpty()) {
			OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
				
					/*
					 * token : personal access token
					 */
					Request newRequest = chain.request().newBuilder()
							.addHeader("Authorization", "token " + token)
							.build();
				
					return chain.proceed(newRequest);
				}
			}).build();
		
			retrofit = new Retrofit.Builder()
					.client(client)
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
			
		} 
		
		else {
			retrofit = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		
		return retrofit;
	}
	
}
