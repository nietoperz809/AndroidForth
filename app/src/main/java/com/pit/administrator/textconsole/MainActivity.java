package com.pit.administrator.textconsole;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import static com.pit.administrator.textconsole.R.id.scrollView;
import static com.pit.administrator.textconsole.R.id.textView;

public class MainActivity extends AppCompatActivity
{
    private JsEvaluator jsEvaluator = new JsEvaluator(this);
    private TextView _myTextView;
    private ScrollView _scroll;
    private EditText _edittext;
    private Handler _hand;
    private ForthRunner _runner;
    private static final String CLS = "\u001b[2J";

    public String javaScript (final String arg) throws Exception
    {
        final ArrayBlockingQueue<String> que = new ArrayBlockingQueue<>(2);
        runOnUiThread(() ->
        {
            jsEvaluator.evaluate(arg, new JsCallback()
            {
                @Override
                public void onResult(String result)
                {
                    que.offer(result);
                }

                @Override
                public void onError(String errorMessage)
                {
                    que.offer(errorMessage);
                }
            });
        });
        return que.take();
    }

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
        MyApp.setFullScreenPortrait(this);
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

    private void saveFile (String filename, String content)
    {
        FileOutputStream outputStream;
        try
        {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String readFile (String filename)
    {
        FileInputStream inputStream;
        try
        {
            inputStream = openFileInput(filename);
            byte[] buff = new byte[256];
            int res = inputStream.read(buff);
            inputStream.close();
            return new String(buff).substring(0,res);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }


    private void codeSlot (MenuItem item)
    {
        final String title = String.valueOf(item.getTitle());
        String content = readFile(title);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        // Set up the input
        final EditText input = new EditText(this);
        input.setText(content);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(5);
        input.setMaxLines(10);
        input.setVerticalScrollBarEnabled(true);
        input.setMovementMethod(ScrollingMovementMethod.getInstance());
        input.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Execute", (dialog, which) ->
        {
            String ss = input.getText().toString();
            print (ss);
            _runner.receive(ss);
            if (!ss.equals(content))
                saveFile (title, ss);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Clear", (dialog, which) ->
        {
            saveFile (title, "");
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        int id = item.getItemId();
        codeSlot (item);
        return super.onOptionsItemSelected(item);
    }
}
