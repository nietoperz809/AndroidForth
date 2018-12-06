package com.pit.administrator.textconsole;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import jforth.JForth;
import tools.StringStream;

import static com.pit.administrator.textconsole.R.id.scrollView;
import static com.pit.administrator.textconsole.R.id.textView;

public class MainActivity extends AppCompatActivity
{
    private StringBuffer theText = new StringBuffer();
    private TextView myTextView;
    private ScrollView scroll;
    private EditText edittext;
    private Handler _hand;

    public void print (final CharSequence txt)
    {
        _hand.post(() ->
        {
            theText.append(txt);
            myTextView.setText(theText);
            scroll.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void handleInputLine (String in)
    {
        StringStream _ss = new StringStream();
        JForth _forth = new JForth(_ss.getPrintStream());
        _forth.singleShot(in);
        print (_ss.toString());
    }

    private void init()
    {
        myTextView = findViewById(textView);
        scroll = findViewById(scrollView);
        edittext  =  findViewById(R.id.edittext);
        _hand = new Handler();

        edittext.setOnKeyListener((v, keyCode, event) ->
        {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER))
            {
                handleInputLine (edittext.getText().toString());
                edittext.getText().clear();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
