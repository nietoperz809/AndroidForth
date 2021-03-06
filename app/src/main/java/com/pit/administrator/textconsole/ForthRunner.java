package com.pit.administrator.textconsole;

import jforth.JForth;
import jforth.Utilities;
import tools.StringStream;

import java.util.concurrent.ArrayBlockingQueue;

public class ForthRunner extends Thread
{
    private ArrayBlockingQueue<String> que = new ArrayBlockingQueue<>(10);
    private StringStream _ss;
    private JForth _forth;
    private MainActivity _ma;

    public ForthRunner (MainActivity ma)
    {
        _ma = ma;
        _ss = new StringStream();
        _forth = new JForth(_ss.getPrintStream());
        start();
    }

    /**
     * Receive input
     * @param s Line of text
     */
    public void receive (String s)
    {
        que.offer(s);
    }

    /**
     * Forth thread
     */
    public void run()
    {
        _ma.print (Utilities.buildInfo+"\n");
        while (true)
        {
            try
            {
                _forth.singleShot(que.take());
                _ma.print(_ss.toString());
                _ss.clear();
            }
            catch (InterruptedException unused)
            {

            }
        }
    }
}
