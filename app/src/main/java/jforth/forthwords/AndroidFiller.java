package jforth.forthwords;

import android.app.Dialog;
import android.hardware.Camera;
import android.support.v7.app.AlertDialog;
import com.pit.administrator.textconsole.MyApp;
import jforth.JForth;
import jforth.PrimitiveWord;
import jforth.Utilities;
import jforth.WordsList;
import jforth.waves.Wave16;
import jforth.waves.WaveForms;


public class AndroidFiller
{
    static Camera camera = null;
    static boolean light = false;

    static void flashLight (boolean on)
    {
        if (camera == null)
            camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        if (on && !light)
        {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            light = true;
        }
        else if (!on && light)
        {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            light = false;
        }
    }


    static void fill (WordsList _fw, PredefinedWords predefinedWords)
    {
        _fw.add(new PrimitiveWord
                (
                        "lon", false, "Flashlight on",
                        (dStack, vStack) ->
                        {
                            flashLight(true);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loff", false, "Flashlight off",
                        (dStack, vStack) ->
                        {
                            flashLight(false);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "sinWav", false, "Vake sinus wave",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double freq = Utilities.readDouble(dStack); // Hz
                                int len = (int)Utilities.readLong(dStack);  // milliseconds
                                Wave16 wv = WaveForms.curveSine(11000,
                                        11*len,
                                        freq, 0);
                                dStack.push(wv.toString());
                                return 1;
                            }
                            catch (Exception unused)
                            {
                                return 0;
                            }
                        }
                ));


        _fw.add(new PrimitiveWord
                (
                        "msg", false, "Show message box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                final String txt = Utilities.readString (dStack);
                                final Thread tr = Thread.currentThread();
                                MyApp.getInstance().getActivity().runOnUiThread(() ->
                                {
                                    // Use the Builder class for convenient dialog construction
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MyApp.getInstance().getActivity());
                                    builder.setTitle(txt);
                                    builder.setPositiveButton("Got that", (dialog, id) ->
                                    {
                                        tr.interrupt();
                                    });
                                    Dialog dlg = builder.create();
                                    dlg.show();
                                });
                                try
                                {
                                    Thread.sleep(1000000);
                                }
                                catch (InterruptedException unused)
                                {

                                }
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ask", false, "Show yes/no box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                final String txt = Utilities.readString (dStack);
                                final Thread tr = Thread.currentThread();
                                MyApp.getInstance().getActivity().runOnUiThread(() ->
                                {
                                    // Use the Builder class for convenient dialog construction
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(MyApp.getInstance().getActivity());
                                    builder.setTitle(txt+"?");
                                    builder.setPositiveButton("Yes", (dialog, id) ->
                                    {
                                        dStack.push(JForth.TRUE);
                                        tr.interrupt();
                                    });
                                    builder.setNegativeButton("No", (dialog, id) ->
                                    {
                                        dStack.push(JForth.FALSE);
                                        tr.interrupt();
                                    });

                                    Dialog dlg = builder.create();
                                    dlg.show();
                                });
                                try
                                {
                                    Thread.sleep(1000000);
                                }
                                catch (InterruptedException unused)
                                {

                                }
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

    }
}
