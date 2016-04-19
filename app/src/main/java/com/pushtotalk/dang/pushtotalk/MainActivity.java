package com.pushtotalk.dang.pushtotalk;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;


public class MainActivity extends Activity {
    private static final String LOG_TAG = HeadsetButtonReceiver.class.getSimpleName();
    private static final int LONG_PRESS_DELAY = 500;

    private Button btnPlay;
    private Button btnRecorder;
    private Button btnStop;
    private String filePath = null;
    private static final int RECORDING_RATE = 8000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static int BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL, FORMAT);

    private Thread recordingThread = null;
    private boolean isRecording = false;
    private AudioRecord audioRecord;
    public static AudioManager audiomanager;
    BufferedOutputStream os = null;
    byte audioData[] = new byte[BUFFER_SIZE];

    AutomaticGainControl agc;
    AudioOutputManager myAudioReceiver;
    HeadsetButtonReceiver HeadsetButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audiomanager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        myAudioReceiver = new AudioOutputManager();


        btnPlay = (Button) findViewById(R.id.btn_play);
        btnRecorder = (Button) findViewById(R.id.btn_recorder);
        btnStop = (Button) findViewById(R.id.btn_stop);

        btnPlay.setEnabled(false);
        btnStop.setEnabled(false);


        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.wav";


        btnRecorder.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                isRecording = true;
                /*****/

                recordingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                                RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE * 10);


                 		/*audioRecord= new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            					RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE*10); */
                 		/**/
                 		/*echoCanceler(audioRecord);*/
                        if (AcousticEchoCanceler.isAvailable()) {
                            AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
                            if (aec != null && !aec.getEnabled()) {
                                aec.setEnabled(true);
                            }
                        }
                        if (NoiseSuppressor.isAvailable()) {
                            NoiseSuppressor noise = NoiseSuppressor.create(audioRecord.getAudioSessionId());

                            if (noise != null && !noise.getEnabled()) {
                                noise.setEnabled(true);
                            }
                        }
                 		/**/
                        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                            audioRecord.startRecording();
                            Log.e("Audioengine", "start record");
                        }


                 		 /*agc = AutomaticGainControl.create(audioRecord.getAudioSessionId());
                 		if (AutomaticGainControl.isAvailable()){
                 			agc.setEnabled(true);
                 		}
                 		else {
                 			Log.i("AD","not AutomaticGainControl");
                 		}*/


                        /***/
                        /*****/
                        FileOutputStream os = null;
                        try {
                            os = new FileOutputStream(filePath);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        float gain = 1.5f;

                         /*ad*/


                        audioData = new byte[BUFFER_SIZE];

                        while (isRecording) {
                            int reallySampledBytes = audioRecord.read(audioData, 0, audioData.length);


                            /*****************************************/

                            if (reallySampledBytes > 0) {
                                for (int i = 0; i < reallySampledBytes; ++i)
                                    audioData[i] = (byte) Math.min((int) (audioData[i] * gain), (int) Short.MAX_VALUE);
                            }
                            /*****************************************************************/

                            try {
                                os.write(audioData, 0, audioData.length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        /**************************************************/

                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        /*****/
                    }

                });
                recordingThread.start();
                /*****/
                btnRecorder.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });


        btnPlay.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                playAudioTrack();
                btnRecorder.setEnabled(true);
                btnStop.setEnabled(false);

            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    isRecording = false;
                    audioRecord.stop();
                    audioRecord.release();
                    recordingThread = null;
                    audioRecord = null;
                    Log.e("Audioengine", "stop record");
                }

                btnStop.setEnabled(false);
                btnPlay.setEnabled(true);
            }
        });

    }


    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myAudioReceiver, filter);

        super.onResume();


    }

    @Override
    public void onPause() {
        unregisterReceiver(myAudioReceiver);
        super.onPause();
    }


    private void unMuteMicrophone() {
        if (audiomanager.isMicrophoneMute()) {
            audiomanager.setMicrophoneMute(false);
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        int action = event.getAction();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
	            if (action == KeyEvent.ACTION_UP)
	            {
	                if (SystemClock.uptimeMillis() - event.getDownTime() > LONG_PRESS_DELAY) {
	                    Log.i(LOG_TAG," long press");
	                }
	            }
                break;

        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                Log.i(LOG_TAG," KEYCODE_HEADSETHOOK");
                break;
        }
        return true;
    }
	/**/

    /******************************************************************************/
    public static void echoCanceler(AudioRecord audio_record) {
        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler aec = AcousticEchoCanceler.create(audio_record.getAudioSessionId());
            if (aec != null && !aec.getEnabled()) {
                aec.setEnabled(true);
            }
        }
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor noise = NoiseSuppressor.create(audio_record.getAudioSessionId());

            if (noise != null && !noise.getEnabled()) {
                noise.setEnabled(true);
            }
        }
    }

    /*************************************************************************/
    int minRecBufBytes = BUFFER_SIZE;
    int recBufferByteSize = minRecBufBytes * 2;
    int frameByteSize = minRecBufBytes / 2;
    int sampleBytes = frameByteSize;
    int recBufferBytePtr = 0;

    void adjsut_audioRecord(byte buffer[], int numberOfBytes) {
        int i = 0;
        if (numberOfBytes > 0) {
            while (i < numberOfBytes) {
                float sample = (float) (buffer[recBufferBytePtr + i] & 0xFF
                        | buffer[recBufferBytePtr + i + 1] << 8);

                // THIS is the point were the work is done:
                // Increase level by about 6dB:
                sample *= 2;
                // Or increase level by 20dB:
                // sample *= 10;
                // Or if you prefer any dB value, then calculate the gain factor outside the loop
                // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
                // sample *= gainFactor;

                // Avoid 16-bit-integer overflow when writing back the manipulated data:
                if (sample >= 32767f) {
                    buffer[recBufferBytePtr + i] = (byte) 0xFF;
                    buffer[recBufferBytePtr + i + 1] = 0x7F;
                } else if (sample <= -32768f) {
                    buffer[recBufferBytePtr + i] = 0x00;
                    buffer[recBufferBytePtr + i + 1] = (byte) 0x80;
                } else {
                    int s = (int) (0.5f + sample);  // Here, dithering would be more appropriate
                    buffer[recBufferBytePtr + i] = (byte) (s & 0xFF);
                    buffer[recBufferBytePtr + i + 1] = (byte) (s >> 8 & 0xFF);
                }
                i += 2;
            }

            // Do other stuff like saving the part of buffer to a file
            // if ( reallySampledBytes > 0 ) { ... save recBuffer+recBufferBytePtr, length: reallySampledBytes

            // Then move the recording pointer to the next position in the recording buffer
            recBufferBytePtr += numberOfBytes;

            // Wrap around at the end of the recording buffer, e.g. like so:
            if (recBufferBytePtr >= recBufferByteSize) {
                recBufferBytePtr = 0;
                sampleBytes = frameByteSize;
            } else {
                sampleBytes = recBufferByteSize - recBufferBytePtr;
                if (sampleBytes > frameByteSize)
                    sampleBytes = frameByteSize;
            }


        }

    }

    /*****/
    public void agc_max(byte[] buffer, int numberOfBytes) {


    }

    public void agc_noise(byte[] buffer, int numberOfBytes) {


    }
    /****/

    /*************************************************************************/

    public void playAudioTrack() {

        File file = new File(filePath);
        int minBufferSize = AudioTrack.getMinBufferSize(RECORDING_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;

        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);

	    /*final AudioTrack at = new AudioTrack(AudioManager.STREAM_VOICE_CALL, RECORDING_RATE, AudioFormat.CHANNEL_OUT_MONO,
				  AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);*/
        final AudioTrack at = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);


        int i = 0;
        byte[] s = new byte[bufferSizeInBytes];

        try {
            final FileInputStream fin = new FileInputStream(filePath);
            final DataInputStream dis = new DataInputStream(fin);
            at.setNotificationMarkerPosition((int) (file.length() / 2));


            at.play();

            while ((i = dis.read(s, 0, bufferSizeInBytes)) > -1) {
                at.write(s, 0, i);

            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        } catch (Exception e) {

        }

    }


    /***********************************/
    void adjustAmplitude(byte buffer[], int numberOfBytes, float gain) {
        int amplitude = 0;
        for (int i = 0; i < numberOfBytes; i++) {
            short curBuffer = (short) (buffer[i] * gain);
            if (curBuffer > amplitude) {
                amplitude = curBuffer;
            }
            buffer[i] = (byte) curBuffer;
        }
    }

    /***********************************************/
    /**********
     * Automatic Gain Controller
     ************/
    int MAX_AMPLITUDE = 32767;
    int MIN_AMPLITUDE = -32768;

    int gainFactor = 1;

    /****************
     * peak detector
     ****************/
    private int peak_sample = 0;
    private int Npkobs = 0; /*number of peak samples */ /*si valeur a définir par defaut*/

    void peakDetector(byte buffer[], int numberOfBytes) {
        peak_sample = 0;
        for (int i = 0; i < numberOfBytes; i++) {
            if (buffer[i] > peak_sample) {
                peak_sample = buffer[i];
            }
        }
		/*Npkobs++;*/ /*à définir si c'est une valeur à fixer*/
    }


    /******************
     * voice activity detector
     *********************/
    byte peakSample[]; /*taille BUFFER_SIZE a verifier*/
    int Vak[]; /* cortaille BUFFER_SIZE a verifier*/
    int Nvak = 0; /* Vak size?*/
    float NoiseFloor = 0;
    float NoiseFloor_margin = 5;
    int n1 = 0;
    int DPKTH = 30; /* à determiner*/

    void voiceActivityDetector(byte buffer[]) {

        int n = 0;
        float peakSample_diff = 0;
        int k = 0;
        Vak = new int[BUFFER_SIZE];
        peakSample = new byte[BUFFER_SIZE];
        int n_vak = 0;

        while (k < buffer.length) {

            if (n1 == Npkobs) {
                if ((buffer[k] < NoiseFloor) || (n_vak >= Nvak)) {
                    Vak[k] = 0;
                    n_vak = 0;
                } else {
                    n_vak++;
                }
                while (n < Npkobs - 1) {
                    peakSample_diff = (peakSample[Npkobs - 1] - peakSample[n]);
                    n++;
                    if ((Math.abs(peakSample_diff) > DPKTH) && (Vak[k] == 0)) {
                        Vak[k] = 1;
                        n_vak = 0;
                        NoiseFloor = Math.min(peakSample[Npkobs - 1], peakSample[n] + NoiseFloor_margin);
                    }
                    peakSample[n - 1] = peakSample[n];
                }

            } else {
                peakSample[n1] = (byte) peak_sample; /** fill peak sample**/
                n = 0;
                n1++;
            }

            k++;
        }


    }

    /***************************************************/

    public double gainController(byte[] sample) {
        double result = 1;

        /***** compute gain*****/
        int res = 0;
        for (int i = 0; i < sample.length; i++) {


        }


        /**** allow headroom ***/


        /*** Fit Gain to Desired Gain Curve ***/


        /**** Compute Incremental Gain ***/

        return result;
    }


    public void amplifierAttenuator(byte[] buffer, int gain) {

    }


    /***************/
    int THRESHOLD_VALUE = 256;
    int OUTPUT_POWER_MIN = -30;
    int OUTPUT_POWER_MAX = +30;

    void AGC(byte[] buffer, int gain_level, int numberOfBytes) {

        double Energie = 0;
        double Puissance = 0;
        double p_db = 0;
        double output_power_Normal = 0, K = 0;

        for (int n = 0; n < numberOfBytes; n++) {
            Energie += buffer[n] * buffer[n];

        }
        Puissance = Energie / numberOfBytes;
        p_db = 10 * Math.log10(Puissance);

        if (p_db < THRESHOLD_VALUE) {

        }

        output_power_Normal = Math.pow(10, gain_level / 10);
        K = Math.sqrt((output_power_Normal * numberOfBytes) / Energie);
        for (int n = 0; n < numberOfBytes; n++) {

            buffer[n] = (byte) (buffer[n] * K);

        }

    }
    /****************/


}
