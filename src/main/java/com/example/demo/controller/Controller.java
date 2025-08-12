package com.example.demo.controller;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {

    @Autowired
    private OkHttpClient okHttpClient;

    private int requestCount = 0;

    @GetMapping("/hello")
    public String helloWorld() throws IOException {

        String url = "https://www.gov.uk/bank-holidays.json";

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);

        try (Response response = call.execute()) {
            String responseSource = response.networkResponse() == null ? "CACHE" : "NETWORK";
            System.out.println("Request #" + requestCount + " served from: " + responseSource);

            response.body().close();

            return "Request " + requestCount + " handled. Response from: " + responseSource;
        }
    }
}
