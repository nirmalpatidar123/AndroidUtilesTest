package com.github.nirmalpatidar123.web;

/**
 * Created by Saurabh.Jain on 5/20/2014.
 */
public class ApiResponse {

    private String response;
    private int responseCode;

    public ApiResponse(String response, int responseCode) {
        this.response = response;
        this.responseCode = responseCode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "response='" + response + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}
