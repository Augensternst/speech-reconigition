package com.example.backend.handler;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshakeBuilder;


@SuppressWarnings("deprecation")
public class DraftWithOrigin extends Draft_17 {

    private final String originUrl;

    public DraftWithOrigin(String originUrl) {
        this.originUrl = originUrl;
    }

    @Override
    public Draft copyInstance() {
        System.out.println(originUrl);
        return new DraftWithOrigin(originUrl);
    }

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
        request.put("Origin", originUrl);
        return request;
    }
}