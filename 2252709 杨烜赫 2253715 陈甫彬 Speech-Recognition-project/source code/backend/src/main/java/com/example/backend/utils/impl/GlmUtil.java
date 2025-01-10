package com.example.backend.utils.impl;

import com.example.backend.dto.AiPromptDTO;
import com.example.backend.utils.AiResponseUtil;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GlmUtil implements AiResponseUtil {

    @Override
    public String generateAiQuestion(AiPromptDTO inputPrompt) {
        // 创建系统消息
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), inputPrompt.getSystemPrompt());
        System.out.println("systemMessage: " + systemMessage.getContent().toString());

        // 构造用户消息
        List<Object> content = getContent(inputPrompt);
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), content);

        // 调用请求生成响应
        ChatMessage response = doRequest(List.of(systemMessage, userMessage), Boolean.FALSE, 1.00f);
        if (response != null) {
            return response.getContent().toString();
        }

        // 如果响应为空，抛出异常
        throw new RuntimeException("Failed to generate AI question");
    }

    private static @NotNull List<Object> getContent(AiPromptDTO inputPrompt) {
        List<Object> content = new ArrayList<>();

        // 添加文本描述对象
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", inputPrompt.getSpecialPrompt());
        content.add(textContent);

        // 遍历 inputPrompt 中的图片 URL，生成内容对象
        List<String> imageUrls = inputPrompt.getImageUrl();
        for (String url : imageUrls) {
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");

            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", url);

            imageContent.put("image_url", imageUrl);
            content.add(imageContent);
        }

        System.out.println("content: " + content.toString());

        return content;
    }


    @Resource
    private ClientV4 client;

    // 默认的是0.95，认为此时是稳定的
    // 较稳定的随机数
    public static final float STABLE_TEMPERATURE = 0.05f;
    // 不稳定的随机数
    public static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 通用请求方法
     *
     * @param aiChatMessages AI聊天消息
     * @param stream 是否开启流式
     * @param temperature 随机性
     * @return AI响应信息
     */
    private ChatMessage doRequest(List<ChatMessage> aiChatMessages, Boolean stream, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("glm-4v-flash")
                .stream(stream)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(aiChatMessages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().getFirst().getMessage();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 简化消息传递
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param stream 是否开启流式
     * @param temperature 随机性
     * @return AI响应信息
     */
    public ChatMessage doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
        // 构造请求
        List<ChatMessage> aiChatMessages = createChatMessage(systemMessage, userMessage);
        return doRequest(aiChatMessages, stream, temperature);
    }

    private List<ChatMessage> createChatMessage(String systemMessage, String userMessage) {
        List<ChatMessage> aiChatMessages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        aiChatMessages.add(systemChatMessage);
        aiChatMessages.add(userChatMessage);
        return aiChatMessages;
    }

    /**
     * 同步请求
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param temperature 随机性
     * @return AI响应信息
     */
    public ChatMessage doSyncRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
    }

    /**
     * 同步请求（答案较稳定）
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return AI响应信息
     */
    public ChatMessage doSyncStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 同步请求（答案较随机）
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return AI响应信息
     */
    public ChatMessage doSyncUnStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, UNSTABLE_TEMPERATURE);
    }

    /**
     * 通用流式请求
     *
     * @param aiChatMessages AI聊天消息
     * @param temperature    随机性
     * @return AI响应信息(流式)
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> aiChatMessages, Float temperature) {
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(aiChatMessages)
                .build();
        ModelApiResponse modelApiResponse = client.invokeModelApi(chatCompletionRequest);
        return modelApiResponse.getFlowable();
    }

    /**
     * 通用流式请求(简化消息传递)
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @param temperature 随机性
     * @return AI响应信息(流式)
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> aiChatMessages = createChatMessage(systemMessage, userMessage);
        return doStreamRequest(aiChatMessages, temperature);
    }
}
