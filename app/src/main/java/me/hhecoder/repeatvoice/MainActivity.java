package me.hhecoder.repeatvoice;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView tvAction;
    private ImageView ivStatus;

    private boolean isStartRecord;

    static final int frequency = 8000;//44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize,playBufSize;
    AudioRecord audioRecord;
    AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAction=findViewById(R.id.tv_action);
        ivStatus=findViewById(R.id.iv_status);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1001);

        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);

        playBufSize=AudioTrack.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency,
                channelConfiguration, audioEncoding, recBufSize*10);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                channelConfiguration, audioEncoding,
                playBufSize, AudioTrack.MODE_STREAM);

        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStartRecord){
                    startRecord();
                }else {
                    endRecord();
                }
            }
        });

    }

    private void startRecord() {
        isStartRecord=true;
        tvAction.setText("Recording......");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[recBufSize];
                    audioRecord.startRecording();//开始录制
                    audioTrack.play();//开始播放

                    while (isStartRecord) {
                        //从MIC保存数据到缓冲区
                        int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);

                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        //写入数据即播放
                        audioTrack.write(tmpBuf, 0, tmpBuf.length);
                    }
                    audioTrack.stop();
                    audioRecord.stop();
                } catch (Throwable t) {
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();



    }

    private void endRecord() {
        isStartRecord=false;
        tvAction.setText("Ready Record");


    }


}
