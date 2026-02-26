package com.concesionario.service.interfaces;

public interface IAzureHubService {
    void sendNotification(String jsonBody, String tag);
}
