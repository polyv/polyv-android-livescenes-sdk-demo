package com.easefun.polyv.livecommon.module.utils.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PLVAudioRecordVolumeHelper {
    private static final int SAMPLE_RATE_IN_HZ = 8000;

    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

    private AudioRecord mAudioRecord;
    private boolean isGetVoiceRun;
    private final Object mLock = new Object();
    private Thread thread;
    private OnAudioRecordListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public void start() {
        getNoiseLevel();
    }

    public void stop() {
        isGetVoiceRun = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setOnGetVolumeListener(OnAudioRecordListener listener) {
        this.listener = listener;
    }

    private void getNoiseLevel() {
        if (isGetVoiceRun) {
            return;
        }
        isGetVoiceRun = true;

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mAudioRecord.startRecording();
                } catch (final Exception e) {
                    e.printStackTrace();
                    isGetVoiceRun = false;
                    if (listener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onStartFail(e);
                                }
                            }
                        });
                    }
                    return;
                }
                if (listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onStartSuccess();
                            }
                        }
                    });
                }
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (short value : buffer) {
                        v += value * value;
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    float halfVolume = 90.3f / 2;
                    final int volumeValue;
                    if (volume >= 90.3) {
                        volumeValue = 100;
                    } else if (volume <= halfVolume) {
                        volumeValue = 0;
                    } else {
                        volumeValue = (int) ((volume - halfVolume) / halfVolume * 100);
                    }
                    if (listener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onVolume(volumeValue);
                                }
                            }
                        });
                    }
                    Log.d("audioRecord", volume + "dB" + "*" + volumeValue);
                    synchronized (mLock) {
                        try {
                            mLock.wait(500); // 一秒两次
                        } catch (InterruptedException e) {
                        }
                    }
                }
                try {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAudioRecord = null;
            }
        });
        thread.start();
    }

    public interface OnAudioRecordListener {
        void onStartSuccess();

        void onStartFail(Throwable t);

        void onVolume(int volumeValue);
    }
}
