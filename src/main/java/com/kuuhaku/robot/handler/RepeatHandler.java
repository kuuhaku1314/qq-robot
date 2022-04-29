package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.entity.Quiz;
import com.kuuhaku.robot.service.QuizService;
import com.kuuhaku.robot.service.RepeatService;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 3:15
 * @Description 复读机
 */
@HandlerComponent
public class RepeatHandler {
    @Autowired
    private RepeatService repeatService;

    @Autowired
    private QuizService quizService;

    @Handler(order = -10)
    public void repeat(ChannelContext ctx) {
        repeatService.tryRepeat(ctx.event());
    }

    @Handler(values = {"出题"}, types = {HandlerMatchType.COMPLETE})
    public void quiz(ChannelContext ctx) {
        Quiz quiz = quizService.RandomQuiz();
        ctx.group().sendMessage(quiz.getTitle());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MessageChain messages = MessageUtils.newChain();
        List<String> answers = quiz.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            messages = messages.plus("选项" + (i + 1) + ": " + answers.get(i)).plus("\n");
        }
        ctx.group().sendMessage(messages);
    }

}
