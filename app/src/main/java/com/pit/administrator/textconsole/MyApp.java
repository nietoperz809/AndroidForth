package com.pit.administrator.textconsole;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

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


//    /**
//     * Returns hosting Activity
//     * @param v The view that is hosted
//     * @return an Activity or null
//     */
//    public static Activity getActivity (View v)
//    {
//        Context context =  v.getContext();
//        while (context instanceof ContextWrapper)
//        {
//            if (context instanceof Activity)
//            {
//                return (Activity) context;
//            }
//            context = ((ContextWrapper) context).getBaseContext();
//        }
//        return null;
//    }

    private Activity currentActivity;

    public Activity getActivity()
    {
        return currentActivity;
    }

}
