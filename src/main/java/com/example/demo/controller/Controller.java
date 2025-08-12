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

    @GetMapping("/hello")
    public String helloWorld() throws IOException {

        String url = "https://www.gov.uk/bank-holidays.json";

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = okHttpClient.newCall(request);

        try (Response response = call.execute()) {  
            response.body().close();

            return "Request completed successfully";
        }
    }
}
