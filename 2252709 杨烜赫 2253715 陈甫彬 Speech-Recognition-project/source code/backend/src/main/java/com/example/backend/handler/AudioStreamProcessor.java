package com.example.backend.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioStreamProcessor {
    private static final int CHUNK_SIZE = 1280; // 讯飞要求的数据块大小
    private static final int SEND_INTERVAL = 40; // 发送间隔（毫秒）
    private final ByteArrayOutputStream audioBuffer;
    private final CustomWebSocketClient xunfeiClient;
    private final ScheduledExecutorService scheduler;
    private long lastSendTime;
    private boolean isProcessing;

    public AudioStreamProcessor(CustomWebSocketClient xunfeiClient) {
        this.xunfeiClient = xunfeiClient;
        this.audioBuffer = new ByteArrayOutputStream();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.lastSendTime = System.currentTimeMillis();
        this.isProcessing = false;
    }

    public synchronized void processAudioChunk(byte[] audioData) {
        try {
            audioBuffer.write(audioData);

            if (audioBuffer.size() >= CHUNK_SIZE) {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastSend = currentTime - lastSendTime;

                if (timeSinceLastSend >= SEND_INTERVAL) {
                    sendAudioData();
                    lastSendTime = currentTime;
                }
            }

            if (!isProcessing) {
                isProcessing = true;
                startProcessing();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendAudioData() {
        try {
            byte[] dataToSend = Arrays.copyOf(audioBuffer.toByteArray(), CHUNK_SIZE);
            xunfeiClient.send(dataToSend);

            byte[] remaining = Arrays.copyOfRange(audioBuffer.toByteArray(), CHUNK_SIZE, audioBuffer.size());
            audioBuffer.reset();
            audioBuffer.write(remaining);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void startProcessing() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (audioBuffer.size() >= CHUNK_SIZE) {
                    sendAudioData();
                    lastSendTime = System.currentTimeMillis();
                }
            }
        }, 0, SEND_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void close() {
        isProcessing = false;
        scheduler.shutdown();
        try {
            if (audioBuffer.size() > 0) {
                byte[] finalData = audioBuffer.toByteArray();
                if (finalData.length > 0) {
                    xunfeiClient.send(finalData);
                }
                xunfeiClient.send("{\"end\": true}".getBytes());
            }
            audioBuffer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
