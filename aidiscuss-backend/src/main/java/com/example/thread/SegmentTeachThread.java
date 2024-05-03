package com.example.thread;

import com.example.model.Sentence;
import com.example.service.GptService;
import com.example.service.RedisService;

import java.util.List;

public class SegmentTeachThread extends Thread {
    private String discussId;
    private volatile boolean running = true;
    private RedisService redisService = new RedisService();
    private GptService gptService = new GptService();
    private static final int MAX_TEXT_LENGTH = 100;

    public SegmentTeachThread(String discussId) {
        this.discussId = discussId;
    }

    @Override
    public void run() {
        try {
            while (running) {
                List<Sentence> sentenceList = redisService.getSentences(discussId);

                int segmentTeachCursor = redisService.getSegmentTeachCursor(discussId);

                // 初始化未处理文本的StringBuilder
                StringBuilder unprocessedText = new StringBuilder();

                while (segmentTeachCursor < sentenceList.size() && unprocessedText.length() <= MAX_TEXT_LENGTH) {
                    unprocessedText.append(sentenceList.get(segmentTeachCursor).getText());
                    segmentTeachCursor++;
                }

                if (unprocessedText.length() > MAX_TEXT_LENGTH) {
                    String text = unprocessedText.toString();
                    String segmentTeach = gptService.requestGpt4("gpt-4-turbo-2024-04-09",
                            "你擅长使用费曼技巧进行解释",
                            "使用费曼技巧进行解释\n" +
                            "L1 儿童 （child）：假想的费曼对象是儿童，8 岁以下，景知识为 0，抽象理解能力接近为 0（主要靠具体事物来理解，很难抽象思考）。\n" +
                            "L2 初中生 （teen）：假想对象是 13~18 岁的中学时。有一定的知识储备，具备了一定的抽象思考能力。这个级别的费曼，信息量明显提升，除了基本概念之外，还可以探讨如何研究，意义如何。\n" +
                            "L3 大一新生 （college student）：典型对象是大一学生，具有成年人该有的正常的知识储备、正常的理解能力。我在写书和互联网分享时，给自己设定的费曼等级就是L3。我默认我的读者是成年人，有大学生的学习能力，有普通成年人该有的知识储备和理解能力。\n" +
                            "L4 研究生 （grad student）：典型对象是同领域的研究生。在特定领域有较深的理论背景、有较强的理解能力。这种对话，信息量大，会涉及研究方法和专业案例，词汇上也会涉及更高级的概念和理论。\n" +
                            "L5 领域专家 （expert）：典型对象是同领域的专业人士。这种费曼已经是思想对话。\n" +
                                    text);
                    redisService.addSegmentTeach(discussId, segmentTeach);

                    // 将更新后的游标位置保存到Redis中
                    redisService.setSegmentTeachCursor(discussId, segmentTeachCursor);
                }

                // 休眠一段时间,避免过于频繁的轮询
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

}