package com.kuuhaku.robot.utils;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.File;

public class VoiceUtil {

    public static boolean transfer(String mp3FileName, String amrFileName) {
        File source = new File(mp3FileName);
        File target = new File(amrFileName);
        AudioAttributes audio = new AudioAttributes();
        int mp3TrackLength = getMp3TrackLength(source);
        audio.setCodec("libamr_wb");
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        // 长于300S文件改成低质量格式，防止无法播放
        if (mp3TrackLength > 300) {
            audio.setBitRate(15850);
        } else {
            audio.setBitRate(23850);
        }
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("amr");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, target, attrs);
        } catch (EncoderException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 返回mp3文件播放时长，单位s
     *
     * @param mp3File
     * @return
     */
    private static int getMp3TrackLength(File mp3File) {
        try {
            MP3File f = (MP3File) AudioFileIO.read(mp3File);
            MP3AudioHeader audioHeader = (MP3AudioHeader) f.getAudioHeader();
            return audioHeader.getTrackLength();
        } catch (Exception e) {
            return 1;
        }
    }
}
