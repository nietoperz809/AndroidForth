package jforth.waves;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class WavePlayer
{
    public static void play16PCM (short[] arr, int samplerate)
    {
        AudioTrack tr = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                arr.length*2,
                AudioTrack.MODE_STREAM);

        final Thread t = Thread.currentThread();

        tr.setNotificationMarkerPosition(arr.length - 1);  // Set the marker to the end.
        tr.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener()
                {
                    @Override
                    public void onPeriodicNotification(AudioTrack track) {}

                    @Override
                    public void onMarkerReached(AudioTrack track)
                    {
                        t.interrupt();
                    }
                });
        tr.write (arr, 0, arr.length);
        tr.play();
        if (arr.length > 1000)
        {
            try
            {
                Thread.sleep(100000);
            }
            catch (InterruptedException unused)
            {
                //e.printStackTrace();
            }
        }
    }
}
