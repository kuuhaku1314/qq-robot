package com.kuuhaku.robot.service;

import com.kuuhaku.robot.entity.Quiz;
import com.kuuhaku.robot.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    @Value("${robot.quiz.path}")
    private String quizPath;
    List<Quiz> quizList = new ArrayList<>();

    @PostConstruct
    void init() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(quizPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Yaml yaml = new Yaml();
        Map<Object, Object> properties = yaml.loadAs(inputStream, Map.class);
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
    }

    public Quiz RandomQuiz(){
        return quizList.get(RandomUtil.random(quizList.size()));
    }
}
