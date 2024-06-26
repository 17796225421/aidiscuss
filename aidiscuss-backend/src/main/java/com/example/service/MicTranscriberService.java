package com.example.service;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;

import javax.sound.sampled.*;
import java.io.*;

import com.example.model.MicTypeEnum;
import com.example.model.Sentence;
import com.example.thread.MicThread;
import com.example.util.TimeUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;

public class MicTranscriberService {
    private Dotenv dotenv = Dotenv.configure().load();
    private String url = dotenv.get("URL");
    private String appKey = dotenv.get("APP_KEY");
    private String accessKeyId = dotenv.get("ACCESS_KEY_ID");
    private String accessKeySecret = dotenv.get("ACCESS_KEY_SECRET");
    private NlsClient client;
    @Autowired
    private RedisService redisService;

    public MicTranscriberService() {
        AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
        try {
            accessToken.apply();
            client = url.isEmpty() ? new NlsClient(accessToken.getToken()) : new NlsClient(url, accessToken.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        redisService = new RedisService();
    }


    // 启动 targetDataLine 和 transcriber 的方法
    public void startMic(String discussId, MicTypeEnum micTypeEnum, MicThread micThread) throws Exception {
        SpeechTranscriber transcriber = null;
        TargetDataLine targetDataLine = null;

        // 查找指定名称的麦克风设备
        Mixer.Info selectedMixerInfo = null;
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)) && info.getName().contains(micTypeEnum.getValue())) {
                System.out.println(info.getName());
                selectedMixerInfo = info;
                break;
            }
        }
        if (selectedMixerInfo != null) {
            // 创建语音识别实例并设置参数
            transcriber = new SpeechTranscriber(client, getTranscriberListener(discussId, micTypeEnum));
            transcriber.setAppKey(appKey);
            //输入音频编码方式。
            transcriber.setFormat(InputFormatEnum.PCM);
            //输入音频采样率。
            transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            //是否返回中间识别结果。
            transcriber.setEnableIntermediateResult(true);
            //是否生成并返回标点符号。
            transcriber.setEnablePunctuation(true);
            //是否将返回结果规整化，比如将一百返回为100。
            transcriber.setEnableITN(true);

            //设置vad断句参数。默认值：800ms，有效值：200ms～2000ms。
            //transcriber.addCustomedParam("max_sentence_silence", 600);
            //设置是否语义断句。
            transcriber.addCustomedParam("enable_semantic_sentence_detection", true);
            //设置是否开启过滤语气词，即声音顺滑。
            transcriber.addCustomedParam("disfluency", true);
            //设置是否开启词模式。
            //transcriber.addCustomedParam("enable_words",true);
            //设置vad噪音阈值参数，参数取值为-1～+1，如-0.9、-0.8、0.2、0.9。
            //取值越趋于-1，判定为语音的概率越大，亦即有可能更多噪声被当成语音被误识别。
            //取值越趋于+1，判定为噪音的越多，亦即有可能更多语音段被当成噪音被拒绝识别。
            //该参数属高级参数，调整需慎重和重点测试。
            transcriber.addCustomedParam("speech_noise_threshold", 0.3);
            //设置训练后的定制语言模型id。
            //transcriber.addCustomedParam("customization_id","你的定制语言模型id");
            //设置训练后的定制热词id。
            //transcriber.addCustomedParam("vocabulary_id","你的定制热词id");

            // 打开指定的麦克风设备
            Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);
            AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
        }
        if (targetDataLine == null || transcriber == null) {
            return;
        }
        targetDataLine.start();
        transcriber.start();

        int nByte = 0;
        final int bufSize = 3200;
        byte[] buffer = new byte[bufSize];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // 用于存储录音数据
        long startTime = System.currentTimeMillis(); // 记录录音开始时间

        while (micThread.isRunning()) {
            try {
                nByte = targetDataLine.read(buffer, 0, bufSize);
                if (nByte > 0) {
                    transcriber.send(buffer, nByte);
                    outputStream.write(buffer, 0, nByte);
                }

                if (System.currentTimeMillis() - startTime >= 10000) {
                    saveRecording(outputStream.toByteArray(), discussId, micTypeEnum);
                    outputStream.reset();
                    startTime = System.currentTimeMillis();
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        // 循环结束后保存剩余数据
        if (outputStream.size() > 0) {
            saveRecording(outputStream.toByteArray(), discussId, micTypeEnum);
        }
        targetDataLine.stop();
        targetDataLine.close();
        transcriber.stop();
        transcriber.close();
    }

    private void saveRecording(byte[] audioData, String discussId, MicTypeEnum micTypeEnum) {
        String discussName = redisService.getDiscussName(discussId);

        // 在项目的根目录下创建一个data文件夹,用来存放数据文件
        String baseDir = "data/";

        // 这里用replace方法进行替换
        String formattedDiscussName = discussName.replace(" ", "_").replace(".", "-").replace(":", "-");

        // 使用修改后的formattedDiscussName作为文件夹名
        String dirName = baseDir + formattedDiscussName;

        String fileName = dirName + "/" + micTypeEnum + ".wav";

        AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());

        File outputFile = new File(fileName);
        try {
            if (outputFile.exists()) {
                AudioInputStream existingAudioInputStream = AudioSystem.getAudioInputStream(outputFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                AudioSystem.write(existingAudioInputStream, AudioFileFormat.Type.WAVE, baos);
                existingAudioInputStream.close();

                baos.write(audioData);

                ByteArrayInputStream bais2 = new ByteArrayInputStream(baos.toByteArray());
                AudioInputStream appendedAudioInputStream = new AudioInputStream(bais2, audioFormat, baos.size() / audioFormat.getFrameSize());

                AudioSystem.write(appendedAudioInputStream, AudioFileFormat.Type.WAVE, outputFile);
                appendedAudioInputStream.close();
            } else {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
            }
            audioInputStream.close();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }


    public SpeechTranscriberListener getTranscriberListener(String discussId, MicTypeEnum micTypeEnum) {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            //识别出中间结果。仅当setEnableIntermediateResult为true时，才会返回该消息。
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
//                System.out.println(String.format(
//                        "result: %s",
//                        response.getTransSentenceText()                //当前的识别结果
//                ));
                redisService.updateRealTimeSentence(discussId, response.getTransSentenceText());
            }


            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {

            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {

            }

            //识别出一句话.服务端会智能断句,当识别到一句话结束时会返回此消息
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                System.out.println(String.format(
                        "index: %d, result: %s",
                        response.getTransSentenceIndex(),               //句子编号,从1开始递增
                        response.getTransSentenceText()                //当前的识别结果
                ));

                Sentence sentence = new Sentence();
                sentence.setText(response.getTransSentenceText());
                sentence.setBeginTime(TimeUtils.getCurrentFormattedTime());
                sentence.setMicTypeEnum(micTypeEnum);

                redisService.addSentence(discussId, sentence);
            }

            //识别完毕
            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
            }
        };

        return listener;
    }

//    public static void main(String[] args) throws Exception {
//        // 获取所有混音器信息
//        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
//        for (Mixer.Info info : mixerInfos) {
//            Mixer mixer = AudioSystem.getMixer(info);
//            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
//                System.out.println(info.getName());
//            }
//        }
//
//        MicTranscriberService service = new MicTranscriberService();
//
//        // 在主线程中创建两个 MicAndTranscriber 对象
//        MicAndTranscriber micAndTranscriber1 = service.openMic("B1");
//        MicAndTranscriber micAndTranscriber2 = service.openMic("Realtek");
//        System.out.println(micAndTranscriber2.getTargetDataLine().isActive());
//
//        // 创建两个子线程,分别调用不同的麦克风
//        Thread thread1 = new Thread(() -> {
//            try {
//                service.startMic(micAndTranscriber1);
//                // 其他处理逻辑...
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, "Thread-1");
//
//        Thread thread2 = new Thread(() -> {
//            try {
//                service.startMic(micAndTranscriber2);
//                // 其他处理逻辑...
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, "Thread-2");
//
//        // 启动两个子线程
//        thread1.start();
//        thread2.start();
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    System.out.println(micAndTranscriber2.getTargetDataLine().isActive());
//                    service.stopMic(micAndTranscriber2);
//                    System.out.println(micAndTranscriber2.getTargetDataLine().isActive());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 10000);
//
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    service.startMic(micAndTranscriber2);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 20000);
//    }

}