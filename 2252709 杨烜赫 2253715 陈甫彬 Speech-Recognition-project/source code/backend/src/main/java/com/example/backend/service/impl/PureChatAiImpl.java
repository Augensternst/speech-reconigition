package com.example.backend.service.impl;

import com.example.backend.dto.AiPromptDTO;
import com.example.backend.dto.AiQuestionDTO;
import com.example.backend.dto.ChildResponseDTO;
import com.example.backend.model.StudentResponse;
import com.example.backend.model.Task;
import com.example.backend.repository.StudentResponseRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AiService;
import com.example.backend.service.UserTaskStageService;
import com.example.backend.utils.AiResponseUtil;
import com.example.backend.utils.ImageProcessUtil;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PureChatAiImpl implements AiService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AiResponseUtil aiTeacher;
    private final StudentResponseRepository studentResponseRepository;
    private final UserTaskStageService userTaskStageService;

    private static final String EXPECTED_RESPONSE_TYPE =
           """
           你的回答中应该严格按照以下格式，即只包含一个json字符串，格式如下：
           {
                "questions": [
                    {
                        "question": "问题1",
                        "hints": [
                            "提示1（一个词语）",
                            "提示2（一个词语）"
                        ]
                    },
                    {
                        "question": "问题2",
                        "hints": [
                            "提示1（一个词语）"
                        ]
                    },
                    {
                        "question": "问题3",
                        "hints": [
                            “提示1（一个词语）”,
                            ”提示2（一个词语）“,
                            ”提示3（一个词语）“
                        ]
                    }
                ]
            }
            你提出的问题不应该超过三个，且每个问题的提示不应该超过三个，且提示与提问是文本中未包含的内容。
            提示1，提示2，提示3的内容是一个词语。
           """;

    private static final String[] SYSTEM_PROMPT = {
            """
            你是一个指导低年级小学生学习的专家，你现在需要指导学生完成将图片连成故事的任务。
            你需要对学生的回答进行评分，并根据学生的回答进行指引，帮助学生更好地学习。
            为了更好地指导学生，你需要根据学生的回答，提出问题，引导学生思考。这些问题可以引导学生改正错误，或者完善回答。
            为了更好地指导学生，我们将问题设置为了三个阶段，每个阶段的侧重点都不同。
            在第一个阶段，你需要引导学生观察图片，讲明图片上面的内容。
            在第二个阶段，你需要引导学生思考图片之间的联系，引导学生将图片连成故事。
            在第三个阶段，你需要引导学生优化语言的表达，并激发想象力，甚至可以部分地超越图片的内容。
            你不应该提问小朋友的回答中已经包含的内容，而应该提问小朋友的回答中未包含的内容。
            """
            ,
            """
            目前在阶段一，你需要主要从图片中的元素是否表示完全、图片中的主要角色的行为是否表述完全、语言是否通顺三个方面评判。
            你所有的问题应该主要围绕这三个方面展开。
            你的问题现在应该倾向于“第几章图片中有什么内容？”或更具体、生动的描述，来引导小朋友找全重点的元素。
            """
            ,
            """
            目前在阶段二，你需要主要从图片之间的逻辑关系是否合理、故事的连贯性、故事的完整性三个方面评判。
            你所有的问题应该主要围绕这三个方面展开。
            你的问题现在应该倾向于引导小朋友思考图片之间的联系，或者引导小朋友思考故事的完整性。
            """
            ,
            """
            目前在阶段三，你需要主要从语言的表达是否生动、语言的表达是否丰富、语言的表达是否准确三个方面评判。
            你所有的问题应该主要围绕这三个方面展开。
            你的问题现在应该倾向于引导小朋友优化语言的表达，或者引导小朋友激发想象力。
            你可以尝试加入修辞的内容，此时提示词中要具体给出类似于“什么像什么”的具体的指导。
            """
    };

    public PureChatAiImpl(TaskRepository taskRepository, UserRepository userRepository, AiResponseUtil aiTeacher,
                          StudentResponseRepository studentResponseRepository, UserTaskStageService userTaskStageService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.aiTeacher = aiTeacher;
        this.studentResponseRepository = studentResponseRepository;
        this.userTaskStageService = userTaskStageService;
    }

    @Transactional
    @Override
    public List<AiQuestionDTO> getAiQuestions(ChildResponseDTO response) throws Exception {
        if (response.getResponse() == null || response.getResponse().isEmpty()) {
            throw new RuntimeException("Response is empty");
        }
        Task task = validateAndFetchTask(response.getTaskId());
        StudentResponse responseForTask = createStudentResponse(response, task);

        userTaskStageService.updateStage(response.getStudentId(), response.getTaskId(), response.getPhase());

        AiPromptDTO aiPrompt = buildAiPrompt(responseForTask, response.getPhase());
        List<AiQuestionDTO> aiQuestions = generateAiResponse(aiPrompt);

        saveStudentResponse(responseForTask, aiQuestions);

        convertHintsToBase64(aiQuestions);

        return aiQuestions;
    }

    private Task validateAndFetchTask(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private StudentResponse createStudentResponse(ChildResponseDTO response, Task task) {
        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setTask(task);
        studentResponse.setResponseText(response.getResponse());
        studentResponse.setStudent(userRepository.getReferenceById(response.getStudentId()));
        return studentResponse;
    }

    private AiPromptDTO buildAiPrompt(StudentResponse studentResponse, Integer phase) {
        String taskImage = studentResponse.getTask().getImageDescription();
        if (taskImage == null) {
            throw new RuntimeException("Task image not found");
        }
        List<String> imageUrls = List.of(taskImage);

        System.out.println(imageUrls);
        AiPromptDTO aiPrompt = new AiPromptDTO();
        aiPrompt.setImageUrl(imageUrls);
        aiPrompt.setSpecialPrompt(studentResponse.getResponseText());
        aiPrompt.setSystemPrompt(generatePromptByPhase(phase));

        return aiPrompt;
    }

    private List<AiQuestionDTO> generateAiResponse(AiPromptDTO aiPrompt) throws Exception {
        String aiResponse = aiTeacher.generateAiQuestion(aiPrompt);
        return parseAiResponse(aiResponse);
    }

    private List<AiQuestionDTO> parseAiResponse(String aiResponse) throws JSONException {
        int startIdx = aiResponse.indexOf('{');
        int endIdx = aiResponse.lastIndexOf('}');

        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx) {
            throw new RuntimeException("Invalid AI response");
        }

        String jsonPart = aiResponse.substring(startIdx, endIdx + 1);
        System.out.println(jsonPart);
        // 构造 JSONObject
        JSONObject jsonObject = new JSONObject(jsonPart);

        // 解析 questions
        JSONArray questionsArray = jsonObject.getJSONArray("questions");
        List<AiQuestionDTO> questions = new ArrayList<>();

        for (int i = 0; i < questionsArray.length(); i++) {
            JSONObject questionObject = questionsArray.getJSONObject(i);

            AiQuestionDTO questionDTO = new AiQuestionDTO();
            questionDTO.setQuestion(questionObject.getString("question"));

            // 解析 hints
            JSONArray hintsArray = questionObject.getJSONArray("hints");
            List<String> hints = new ArrayList<>();
            for (int j = 0; j < hintsArray.length(); j++) {
                hints.add(hintsArray.getString(j));
            }
            questionDTO.setHints(hints);

            questions.add(questionDTO);
        }

        return questions;
    }

    private void saveStudentResponse(StudentResponse studentResponse, List<AiQuestionDTO> aiQuestions) {
        studentResponse.setAiFeedback(Map.of("questions", aiQuestions));
        studentResponseRepository.save(studentResponse);
    }

    private void convertHintsToBase64(List<AiQuestionDTO> aiQuestions) {
        aiQuestions.forEach(aiQuestion ->
                aiQuestion.getHints().replaceAll(ImageProcessUtil::imgToBase64)
        );
    }

    @Override
    public Map<String, Object> getConversation(Integer userId) {
        try {
            if (!userRepository.existsById(userId)) {
                throw new RuntimeException("User not found");
            }

            List<StudentResponse> responses = studentResponseRepository.findByStudentId(userId);
            return Map.of("StudentResponses", responses, "succeeded", true);
        } catch (RuntimeException e) {
            return Map.of("succeeded", false, "message", e.getMessage());
        }
    }

    private String generatePromptByPhase(Integer phase) {
        return switch (phase) {
            case 1 -> SYSTEM_PROMPT[0] + SYSTEM_PROMPT[1] + EXPECTED_RESPONSE_TYPE;
            case 2 -> SYSTEM_PROMPT[0] + SYSTEM_PROMPT[2] + EXPECTED_RESPONSE_TYPE;
            case 3 -> SYSTEM_PROMPT[0] + SYSTEM_PROMPT[3] + EXPECTED_RESPONSE_TYPE;
            default -> throw new RuntimeException("Invalid phase");
        };
    }
}
