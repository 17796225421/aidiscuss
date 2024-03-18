package com.example.service;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;

import javax.sound.sampled.Line.Info;
import javax.sound.sampled.*;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import io.github.cdimascio.dotenv.Dotenv;

public class MicTranscriberService {
    private Dotenv dotenv = Dotenv.configure().load();
    private String url = dotenv.get("URL");
    private String appKey = dotenv.get("APP_KEY");
    private String accessKeyId = dotenv.get("ACCESS_KEY_ID");
    private String accessKeySecret = dotenv.get("ACCESS_KEY_SECRET");

    private NlsClient client;

    public MicTranscriberService() {
        AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
        try {
            accessToken.apply();
            System.out.println("get token: " + ", expire time: " + accessToken.getExpireTime());
            if(url.isEmpty()) {
                client = new NlsClient(accessToken.getToken());
            }else {
                client = new NlsClient(url, accessToken.getToken());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录音并进行实时语音转写
     * @param micName 麦克风设备名称
     * @return TranscriberContext 转写上下文对象
     */
    public TranscriberContext startMic(String micName) {
        TranscriberContext context = new TranscriberContext();
        context.start(micName);
        return context;
    }

    /**
     * 停止录音和语音转写
     * @param context 转写上下文对象
     */
    public void stopMic(TranscriberContext context) throws Exception {
        context.stop();
    }

    /**
     * 转写上下文类,用于控制录音转写过程和获取转写结果
     */
    public class TranscriberContext {
        private SpeechTranscriber transcriber;
        private TargetDataLine targetDataLine;
        private AtomicBoolean isRecording = new AtomicBoolean(false);
        private BlockingQueue<String> textQueue = new LinkedBlockingQueue<>();

        /**
         * 开始录音转写
         * @param micName 麦克风设备名称
         */
        private void start(String micName) {
            if (isRecording.compareAndSet(false, true)) {
                try {
                    // 创建实例,建立连接
                    transcriber = new SpeechTranscriber(client, getTranscriberListener());
                    transcriber.setAppKey(appKey);
                    // 输入音频编码方式
                    transcriber.setFormat(InputFormatEnum.PCM);
                    // 输入音频采样率
                    transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
                    // 是否返回中间识别结果
                    transcriber.setEnableIntermediateResult(true);
                    // 是否生成并返回标点符号
                    transcriber.setEnablePunctuation(true);
                    // 是否将返回结果规整化,比如将一百返回为100
                    transcriber.setEnableITN(false);

                    // 此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
                    transcriber.start();

                    // 打开麦克风设备
                    Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                    for (Mixer.Info info : mixerInfos) {
                        Mixer mixer = AudioSystem.getMixer(info);
                        if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                            if (info.getName().contains(micName)) {
                                targetDataLine = (TargetDataLine) mixer.getLine(new Line.Info(TargetDataLine.class));
                                break;
                            }
                        }
                    }

                    if (targetDataLine != null) {
                        AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
                        targetDataLine.open(audioFormat);
                        targetDataLine.start();

                        // 开始录音并发送数据
                        int nByte = 0;
                        final int bufSize = 3200;
                        byte[] buffer = new byte[bufSize];
                        while (isRecording.get() && (nByte = targetDataLine.read(buffer, 0, bufSize)) > 0) {
                            transcriber.send(buffer, nByte);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        /**
         * 停止录音转写
         */
        private void stop() throws Exception {
            if (isRecording.compareAndSet(true, false)) {
                if (targetDataLine != null) {
                    targetDataLine.stop();
                    targetDataLine.close();
                }

                if (transcriber != null) {
                    transcriber.stop();
                    transcriber.close();
                }
            }
        }

        /**
         * 获取语音转写的监听器
         * @return SpeechTranscriberListener
         */
        private SpeechTranscriberListener getTranscriberListener() {
            return new SpeechTranscriberListener() {
                @Override
                public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                    String text = response.getTransSentenceText();
                    textQueue.offer(text);
                }

                @Override
                public void onTranscriberStart(SpeechTranscriberResponse response) {
                    System.out.println("开始录音转写");
                }

                @Override
                public void onSentenceBegin(SpeechTranscriberResponse response) {
                }

                @Override
                public void onSentenceEnd(SpeechTranscriberResponse response) {
                }

                @Override
                public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                    System.out.println("录音转写完成");
                }

                @Override
                public void onFail(SpeechTranscriberResponse response) {
                    System.out.println("录音转写出错: " + response.getStatusText());
                }
            };
        }

        /**
         * 获取新增的转写文本
         * @return 新增的转写文本,如果没有则返回null
         */
        public String getNewText() {
            return textQueue.poll();
        }
    }

    public static void main(String[] args) throws Exception {

        // 获取所有混音器信息
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            // 检查该混音器是否支持某些类型的线（在这里是TargetDataLine，即输入线）
            if (mixer.isLineSupported(new Info(TargetDataLine.class))) {
                // 打印出支持TargetDataLine的混音器名称，这些通常是麦克风设备
                System.out.println(info.getName());

            }
        }
        MicTranscriberService service = new MicTranscriberService();

        // 开始录音并转写,获取TranscriberContext对象
        TranscriberContext context = service.startMic("B1");

    }
}