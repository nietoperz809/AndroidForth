package com.pit.administrator.textconsole;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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

    private Activity currentActivity;

    public Activity getActivity()
    {
        return currentActivity;
    }

}
