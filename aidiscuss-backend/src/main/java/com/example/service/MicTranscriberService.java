package com.example.service;

import com.alibaba.nls.client.AccessToken;
import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;

import javax.sound.sampled.*;
import java.io.IOException;

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
            client = url.isEmpty() ? new NlsClient(accessToken.getToken()) : new NlsClient(url, accessToken.getToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMic(String micName) throws Exception {
        SpeechTranscriber transcriber = null;
        TargetDataLine targetDataLine = null;
        try {
            // 查找指定名称的麦克风设备
            Mixer.Info selectedMixerInfo = null;
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : mixerInfos) {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(new Line.Info(TargetDataLine.class)) && info.getName().contains(micName)) {
                    selectedMixerInfo = info;
                    break;
                }
            }

            if (selectedMixerInfo != null) {
                // 创建语音识别实例并设置参数
                transcriber = new SpeechTranscriber(client, getTranscriberListener());
                transcriber.setAppKey(appKey);
                transcriber.setFormat(InputFormatEnum.PCM);
                transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
                transcriber.setEnableIntermediateResult(true);
                transcriber.setEnablePunctuation(true);
                transcriber.setEnableITN(false);
                transcriber.start();

                // 打开麦克风设备并开始读取音频数据
                AudioFormat audioFormat = new AudioFormat(16000.0F, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                // 从麦克风读取音频数据并发送给服务端
                int nByte = 0;
                final int bufSize = 3200;
                byte[] buffer = new byte[bufSize];
                while ((nByte = targetDataLine.read(buffer, 0, bufSize)) > 0) {
                    transcriber.send(buffer, nByte);
                }
            } else {
                System.out.println("Microphone not found: " + micName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭语音识别实例和麦克风设备
            if (transcriber != null) {
                transcriber.stop();
                transcriber.close();
            }
            if (targetDataLine != null) {
                targetDataLine.stop();
                targetDataLine.close();
            }
        }
    }


    public SpeechTranscriberListener getTranscriberListener() {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {

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
                        "index: %d, result: %s, begin_time: %.1f, time: %.1f",
                        response.getTransSentenceIndex(),               //句子编号，从1开始递增
                        response.getTransSentenceText(),                //当前的识别结果
                        response.getSentenceBeginTime() / 1000.0,       //句子开始时间，单位是秒
                        response.getTransSentenceTime() / 1000.0        //当前已处理的音频时长，单位是秒
                ));
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

    public static void main(String[] args) throws Exception {
        // 获取所有混音器信息
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                System.out.println(info.getName());
            }
        }

        MicTranscriberService service = new MicTranscriberService();
        service.startMic("M3");
    }
}