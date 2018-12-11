package com.pit.administrator.textconsole;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import static com.pit.administrator.textconsole.R.id.scrollView;
import static com.pit.administrator.textconsole.R.id.textView;

public class MainActivity extends AppCompatActivity
{
    private TextView _myTextView;
    private ScrollView _scroll;
    private EditText _edittext;
    private Handler _hand;
    private ForthRunner _runner;
    private static final String CLS = "\u001b[2J";

    public void print (final String txt)
    {
        _hand.post(() ->
        {
            int pos = txt.indexOf(CLS);
            if (pos >= 0)
            {
                _myTextView.setText(txt.substring(pos+CLS.length()));
            }
            else
            {
                _myTextView.append(txt);
            }
            _scroll.post(() -> _scroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void init()
    {
        _runner = new ForthRunner(this);
        _myTextView = findViewById(textView);
        _scroll = findViewById(scrollView);
        _edittext =  findViewById(R.id.edittext);
        _hand = new Handler();

        _edittext.setOnKeyListener((v, keyCode, event) ->
        {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER))
            {
                String ss = _edittext.getText().toString();
                print (ss+"\n");
                _runner.receive(ss);
                _edittext.getText().clear();
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
