package com.pit.administrator.textconsole;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyApp extends Application
{
    private static MyApp instance;

    public static MyApp getInstance()
    {
        return instance;
    }

    public static Context getContext()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        instance = this;
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks()
        {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState)
            {
                MyApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity)
            {
                MyApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity)
            {
                MyApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity)
            {
                MyApp.this.currentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity)
            {
                // don't clear current activity because activity may get stopped after
                // the new activity is resumed
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState)
            {

            }

            @Override
            public void onActivityDestroyed(Activity activity)
            {
                // don't clear current activity because activity may get destroyed after
                // the new activity is resumed
            }
        });
    }


    /**
     * Sets this App to Fullscreen Landscape
     * This Class MUST be derived from Activity or it will CRASH!
     */
    static void setFullScreenPortrait(Activity act)
    {
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        act.requestWindowFeature(Window.FEATURE_NO_TITLE);
        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static void showException (Exception e)
    {
        Toast.makeText(instance.getBaseContext(),e.toString(),
                Toast.LENGTH_LONG).show();
    }

    private Activity currentActivity;

    public Activity getActivity()
    {
        return currentActivity;
    }

    //load file from apps res/raw folder or Assets folder
    public String LoadFile (String fileName) throws IOException
    {
        //Create a InputStream to read the file into
        InputStream inputStream = getResources().getAssets().open(fileName);

        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; )
        {
            total.append(line).append('\n');
        }
        return total.toString();
    }
}
