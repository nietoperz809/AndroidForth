package noweb;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Line Editor: -->
 * Commands are:
 * #l         -- List (with line numbers)
 * #t         -- print list as String
 * #c         -- clear all
 * #x         -- leave line editor. Content is pushed on forth stack
 * #r text    -- read file where text is the file name
 * #s test    -- save file where text is the file name
 * #innn text -- Insert before, where nnn is the line number and text is the content
 * #nnn      -- Delete line nnn
 * <p>
 * ... any other input is appended.
 * Type "editor" to enter the line editor
 */
public class LineEdit
{
    private ArrayList<String> list = new ArrayList<>();
    private final PrintStream _out;

    private static final String helpText =
            " * Line Editor: -->\n" +
                    " Commands are:\n" +
                    " #l         -- List (with line numbers)\n" +
                    " #t         -- print list as String\n" +
                    " #c         -- clear all\n" +
                    " #h         -- this help text\n" +
                    " #dir       -- List directory\n" +
                    " #x         -- leave line editor\n" +
                    " #r text    -- read file where text is the file name\n" +
                    " #s test    -- save file where text is the file name\n" +
                    " #innn text -- Insert before, where nnn is the line number and text is the content\n" +
                    " #nnn      -- Delete line nnn\n" +
                    " ... any other input is appended to the buffer.";

    public LineEdit (PrintStream p)
    {
        _out = p;
    }

    private void printErr()
    {
        _out.print("ERROR");
        _out.flush();
    }

    private void printOk()
    {
        _out.print("OK");
        _out.flush();
    }

    public boolean handleLine (String in)
    {
        if (in.startsWith("#"))
        {
            int firstspc = in.indexOf(' ');
            String args;
            String cmd;
            if (firstspc == -1)  // no Space found
            {
                cmd = in.substring(1, in.length());
                args = null;
            }
            else
            {
                cmd = in.substring(1, firstspc);
                args = in.substring(firstspc + 1, in.length());
            }
            try
            {
                int linenum = Integer.parseInt(cmd);
                if (args == null)
                {
                    try
                    {
                        list.remove(linenum);
                    }
                    catch (Exception e)
                    {
                        printErr();
                    }
                }
                else
                {
                    list.set(linenum, args);
                }
                return true;
            }
            catch (Exception ignored)
            {

            }
            try
            {
                boolean retval = true;
                if (cmd.equals("l")) // List with line numbers
                {
                    for (int s = 0; s < list.size(); s++)
                    {
                        _out.println(s + ": " + list.get(s));
                    }
                }
                else if (cmd.equals("t")) // List without line numbers
                {
                    String s = toString();
                    if (!s.isEmpty())
                    {
                        _out.println(s);
                    }
                }
                else if (cmd.equals("c"))  // clear program
                {
                    clear();
                }
                else if (cmd.equals("r"))   // load new program
                {
                    load(args);
                }
                else if (cmd.equals("a"))   // append program from disk
                {
                    append(args);
                }
                else if (cmd.equals("s"))   // save program
                {
                    save(args);
                }
                else if (cmd.equals("h"))   // save program
                {
                    _out.println(helpText);
                }
                else if (cmd.equals("dir")) // show directory
                {
                    //String s = Utilities.dir(".");
                    //_out.println(s.trim());
                    _out.println("not implemented");
                }
                else if (cmd.equals("x"))   // leave editor
                {
                    retval = false;
                }
                else if (cmd.startsWith("i"))
                {
                    int pos = Integer.parseInt(cmd.substring(1));
                    list.add(pos, args);
                }
                else
                {
                    printErr();
                    return true;
                }
                printOk();
                return retval;
            }
            catch (Exception e)
            {
                printErr();
            }
        }
        else
        {
            if (in.length() > 0)
            {
                list.add(in);
                printOk();
            }
        }
        return true;
    }

    public String toString ()
    {
        StringBuilder sb = new StringBuilder();
        for (String s : list)
        {
            sb.append(s).append('\n');
        }
        return sb.toString().trim();
    }

    private void clear ()
    {
        list.clear();
    }

    private void load (String name) throws Exception
    {
        _out.println("not implemented");
        //        list = Utilities.fileLoad(name);
    }

    private void append (String name) throws Exception
    {
        _out.println("not implemented");
//        ArrayList<String> l2 = Utilities.fileLoad(name);
//        list.addAll(l2);
    }

    private void save (String name) throws Exception
    {
        _out.println("not implemented");
//        Utilities.fileSave(list, name);
    }
}
