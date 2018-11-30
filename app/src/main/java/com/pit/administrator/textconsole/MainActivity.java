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
import noweb.*;

import static com.pit.administrator.textconsole.R.id.scrollView;
import static com.pit.administrator.textconsole.R.id.textView;

public class MainActivity extends AppCompatActivity implements App, StringSink
{
    private StringBuffer theText = new StringBuffer();
    private TextView myTextView;
    private ScrollView scroll;
    private Handler _hand;
    private WebForthVM webForthVM;

    public enum MODE {EDIT, DIRECT}
    public MODE mode = MODE.DIRECT;
    private LineEdit _editor;

    public void post (final CharSequence txt)
    {
        _hand.post(() ->
        {
            theText.append(txt);
            myTextView.setText(theText);
            scroll.fullScroll(View.FOCUS_DOWN);
        });
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myTextView = findViewById(textView);
        scroll = findViewById(scrollView);
        _hand = new Handler();
        _editor = new LineEdit(new StringStream(this).getPrintStream());

        webForthVM = new WebForthVM(this);
        webForthVM.prepare();
        webForthVM.start();

        final EditText edittext =  findViewById(R.id.edittext);
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

    private void handleInputLine (String s)
    {
        if (mode == MODE.DIRECT)
        {
            if (s.equals ("editor"))
            {
                mode = MODE.EDIT;
                //post ("Type #h for help ...");
                _editor.handleLine("#h");
            }
            else
            {
                for (int l = 0; l < s.length(); l++)
                {
                    webForthVM.enqueueKey(s.charAt(l));
                }
                webForthVM.enqueueKey('\n');
            }
        }
        else // mode == EDIT
        {
            if (!_editor.handleLine(s))
            {
                mode = MODE.DIRECT;
            }
        }
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
            //textOut(""+System.currentTimeMillis());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int cursor ()
    {
        return 0;
    }

    @Override
    public void clear ()
    {

    }

    @Override
    public void print (String s)
    {
        post(s);
    }

    @Override
    public void emitChar (char c)
    {
        post (""+c);
    }
}
