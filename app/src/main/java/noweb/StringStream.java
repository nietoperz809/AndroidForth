package noweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Administrator on 4/15/2017.
 */

public class StringStream
{
    private final StringSink _mbs;
    private final ByteArrayOutputStream baos;
    private final PrintStream ps;

    class myBAStream extends ByteArrayOutputStream
    {
        @Override
        public synchronized void write (byte[] b, int off, int len)
        {
            StringStream.this._mbs.post(new String (b, off, len));
        }
    }

    public OutputStream getOutputStream()
    {
        return baos;
    }

    public PrintStream getPrintStream()
    {
        return ps;
    }

    public void clear()
    {
        baos.reset();
    }

    public StringStream (StringSink bs)
    {
        _mbs = bs;
        if (bs == null)
            baos = new ByteArrayOutputStream();
        else
            baos = new myBAStream();
        ps = new PrintStream(baos);
    }

    public StringStream ()
    {
        this (null);
    }

    @Override
    public String toString ()
    {
        try
        {
            baos.flush();
        }
        catch (IOException e)
        {
            return e.toString();
        }
        return new String (baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
