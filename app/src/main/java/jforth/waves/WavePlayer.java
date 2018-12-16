package jforth.waves;

import android.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WavePlayer
{
    /**
     * Play Wave file
     * @param arr byte arry including header
     //* @throws Exception if smth went wrong
     */
    public static void play (byte[] arr) throws Exception
    {
        File tempMp3 = File.createTempFile("audio", "wav");
        tempMp3.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempMp3);
        fos.write(arr);
        fos.close();

        MediaPlayer mediaPlayer = new MediaPlayer();
        FileInputStream fis = new FileInputStream(tempMp3);
        mediaPlayer.setDataSource(fis.getFD());
        mediaPlayer.prepare();
        mediaPlayer.start();
    }
}
