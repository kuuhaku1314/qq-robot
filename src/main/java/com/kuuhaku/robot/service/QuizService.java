package com.kuuhaku.robot.service;

import com.kuuhaku.robot.entity.Quiz;
import com.kuuhaku.robot.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author by kuuhaku
 * @date 2022/3/5 18:26
 * @description
 */
@Service
@Slf4j
public class QuizService {
    List<Quiz> quizList = new ArrayList<>();
    @Value("${robot.quiz.path}")
    private String quizPath;

    @PostConstruct
    void init() {
        try (InputStream inputStream = new FileInputStream(quizPath)) {

            Yaml yaml = new Yaml();
            Map properties = yaml.loadAs(inputStream, Map.class);
            for (Object value : properties.values()) {
                Map<Object, Object> subProperties = (Map<Object, Object>) value;
                String title = (String) subProperties.get("question");
                List<String> answers = new ArrayList<>();
                ArrayList<Map<Object, Object>> answersProperties = (ArrayList<Map<Object, Object>>) subProperties.get("answers");
                answersProperties.forEach((m) -> {
                    answers.add((String) m.get("answer"));
                });
                Quiz quiz = new Quiz();
                quiz.setTitle(title);
                quiz.setAnswers(answers);
                quizList.add(quiz);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Quiz randomQuiz() {
        return quizList.get(RandomUtil.random(quizList.size()));
    }
}
