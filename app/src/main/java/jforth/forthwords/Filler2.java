package jforth.forthwords;

import android.util.Base64;
import jforth.*;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;

class Filler2
{
    static void fill (WordsList _fw, PredefinedWords predefinedWords)
    {
        _fw.add(new PrimitiveWord
                (
                        "hexStr", false, "Make hex string",
                        (dStack, vStack) ->
                        {
                            Object o1 = dStack.pop();
                            byte[] b = null;
                            if (o1 instanceof String)
                            {
                                try
                                {
                                    b = ((String) o1).getBytes("ISO-8859-1");
                                }
                                catch (UnsupportedEncodingException e)
                                {
                                    return 0;
                                }
                            }
                            else if (o1 instanceof DoubleSequence)
                            {
                                b = ((DoubleSequence) o1).asBytes();
                            }
                            if (b != null)
                            {
                                dStack.push(Utilities.printHexBinary(b));
                                return 1;
                            }
                            if (o1 instanceof Long)
                            {
                                dStack.push(Long.toHexString((Long)o1));
                                return 1;
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unhexStr", false, "Make Hexstr to Bytes",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] b = Utilities.parseHexBinary(ss);
                                dStack.push(new DoubleSequence(b));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "seq", false, "generate sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                double d3 = Utilities.readDouble(dStack);
                                int l2 = (int)Utilities.readLong(dStack);
                                double d1 = Utilities.readDouble(dStack);
                                DoubleSequence ds = DoubleSequence.makeCounted(d1, l2, d3);
                                dStack.push(ds);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "hash", false, "generate hash string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String hash = Utilities.readString(dStack);
                                String input = Utilities.readString(dStack);
                                MessageDigest md = MessageDigest.getInstance(hash);
                                dStack.push(new DoubleSequence(md.digest(input.getBytes("ISO-8859-1"))));
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "b64", false, "make Base64 from String",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] bytes = ss.getBytes(JForth.ENCODING);
                                String encoded = Base64.encodeToString(bytes,
                                        Base64.NO_WRAP | Base64.URL_SAFE);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "unb64", false, "make String from Base64",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                byte[] decoded = android.util.Base64.decode(ss, Base64.DEFAULT);
                                String b2 = new String(decoded, JForth.ENCODING);
                                dStack.push(b2);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlEnc", false, "URL encode a string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLEncoder.encode(ss, JForth.ENCODING);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "urlDec", false, "Decode URL encoded string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                String encoded = URLDecoder.decode(ss, JForth.ENCODING);
                                dStack.push(encoded);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "psp", false, "Push space on stack",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                dStack.push(" ");
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "say", false, "speak a string",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String ss = Utilities.readString(dStack);
                                JForth.speak(ss);
                                return 1;
                            }
                            catch (Exception ex)
                            {
                                return 0;
                            }
                        }
                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "js", false, "evaluate js expression string",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                String ss = Utilities.readString(dStack);
//                                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
//                                Object o = engine.eval(ss);
//                                dStack.push(o);
//                                return 1;
//                            }
//                            catch (Exception ex)
//                            {
//                                return 0;
//                            }
//                        }
//                ));
//
//        _fw.add(new PrimitiveWord
//                (
//                        "java", false, "compile and run java class",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                String source = Utilities.readString(dStack);
//                                String classname = "Solution";
//                                source = "public class " + classname + " {" + source + "}";
//                                Object arg = dStack.pop();
//                                JavaExecutor compiler = new JavaExecutor();
//                                final Method greeting = compiler.compileStaticMethod("main", classname, source);
//                                final Object result = greeting.invoke(null, arg);
//                                dStack.push(result);
//                                return 1;
//                            }
//                            catch (Exception ex)
//                            {
//                                return 0;
//                            }
//                        }
//                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "forth", false, "execute forth line asynchronously",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                final String ss = Utilities.readString(dStack);
//                                new Thread(() ->
//                                {
//                                    JForth f = new JForth(AnsiConsole.out);
//                                    f.interpretLine(ss);
//                                    AnsiConsole.out.flush();
//                                }).start();
//                                return 1;
//                            }
//                            catch (Exception ex)
//                            {
//                                return 0;
//                            }
//                        }
//                ));

        _fw.add(new PrimitiveWord
                (
                        "execute", false, "executes word from stack",
                        (dStack, vStack) ->
                        {
                            Object o = dStack.pop();
                            if (o instanceof BaseWord)
                            {
                                BaseWord bw = (BaseWord) o;
                                return bw.execute(dStack, vStack);
                            }
                            return 0;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "if", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            int currentIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            IfControlWord ifcw = new IfControlWord(currentIndex);
                            predefinedWords._jforth.wordBeingDefined.addWord(ifcw);
                            vStack.push(ifcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "then", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            Object o = vStack.pop();
                            int thenIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            if (o instanceof ElseControlWord)
                            {
                                ((ElseControlWord) o).setThenIndexIncrement(thenIndex);
                                o = vStack.pop();
                            }
                            if (o instanceof IfControlWord)
                            {
                                ((IfControlWord) o).setThenIndex(thenIndex);
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "else", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            Object o = vStack.peek();
                            if (o instanceof IfControlWord)
                            {
                                int elseIndex = predefinedWords._jforth.wordBeingDefined.getNextWordIndex() + 1;
                                ElseControlWord ecw = new ElseControlWord(elseIndex);
                                predefinedWords._jforth.wordBeingDefined.addWord(ecw);
                                vStack.push(ecw);
                                ((IfControlWord) o).setElseIndex(elseIndex);
                            }
                            else
                            {
                                return 0;
                            }
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "do", true,
                        (dStack, vStack) ->
                        {
                            predefinedWords.createTemporaryImmediateWord();
                            DoLoopControlWord dlcw = new DoLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(dlcw);
                            int index = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "i", false, "put loop variable i on stack",
                        (dStack, vStack) ->
                        {
                            Object o = vStack.peek();
                            dStack.push(o);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "j", false, "put loop variable j on stack",
                        (dStack, vStack) ->
                        {
                            Object o1 = vStack.pop();
                            Object o2 = vStack.pop();
                            Object o3 = vStack.peek();
                            dStack.push(o3);
                            vStack.push(o2);
                            vStack.push(o1);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "leave", true,
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            LeaveLoopControlWord llcw = new LeaveLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(llcw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "loop", true,  "repeat loop",
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, LoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "+loop", true, "adds value to loop counter i",
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, PlusLoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "begin", true,
                        (dStack, vStack) ->
                        {
                            predefinedWords.createTemporaryImmediateWord();
                            int index = predefinedWords._jforth.wordBeingDefined.getNextWordIndex();
                            vStack.push((long) index);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "until", true,
                        (dStack, vStack) ->
                                WordHelpers.addLoopWord(vStack, predefinedWords, EndLoopControlWord.class)
                ));

        _fw.add(new PrimitiveWord
                (
                        "again", true,
                        (dStack, vStack) ->
                        {
                            try
                            {
                                predefinedWords._jforth.wordBeingDefined.addWord(predefinedWords._wl.search("false"));
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                            return WordHelpers.addLoopWord(vStack, predefinedWords, EndLoopControlWord.class);
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "break", true, "Breaks out of the forth word",
                        (dStack, vStack) ->
                        {
                            if (!predefinedWords._jforth.compiling)
                            {
                                return 1;
                            }
                            BreakLoopControlWord ecw = new BreakLoopControlWord();
                            predefinedWords._jforth.wordBeingDefined.addWord(ecw);
                            return 1;
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "clltz", false, "Get collatz sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                long n = Utilities.readLong(dStack);
                                ArrayList<Double> ar = new ArrayList<>();
                                ar.add((double)n);
                                for (;n!=1;)
                                {
                                    if (n%2 == 0)
                                        n=n/2;
                                    else
                                        n=3*n+1;
                                    ar.add((double)n);
                                }
                                dStack.push (new DoubleSequence(ar));
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "ping", false, "Check a host",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String host  = Utilities.readString (dStack);
                                InetAddress inet = InetAddress.getByName(host);
                                if (inet.isReachable(5000))
                                    dStack.push(JForth.TRUE);
                                else
                                    dStack.push(JForth.FALSE);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "msg", false, "Show message box",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                String txt = Utilities.readString (dStack);
//                                JOptionPane.showMessageDialog(null,
//                                        txt,
//                                        "JForth",
//                                        JOptionPane.PLAIN_MESSAGE);
//                                return 1;
//                            }
//                            catch (Exception e)
//                            {
//                                return 0;
//                            }
//                        }
//                ));

//        _fw.add(new PrimitiveWord
//                (
//                        "ask", false, "Show yes/no box",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                String txt = Utilities.readString (dStack);
//                                int dialogResult = JOptionPane.showConfirmDialog(null,
//                                        txt+"?",
//                                        "Jforth", JOptionPane.YES_NO_OPTION);
//                                if (dialogResult == 0)
//                                    dStack.push(JForth.TRUE);
//                                else
//                                    dStack.push(JForth.FALSE);
//                                return 1;
//                            }
//                            catch (Exception e)
//                            {
//                                return 0;
//                            }
//                        }
//                ));


//        _fw.add(new PrimitiveWord
//                (
//                        "asyncmsg", false, "Show asynchronous message box",
//                        (dStack, vStack) ->
//                        {
//                            try
//                            {
//                                String txt = Utilities.readString (dStack);
//                                new Thread(() ->
//                                {
//                                    JOptionPane.showMessageDialog(null,
//                                            txt,
//                                            "JForth",
//                                            JOptionPane.PLAIN_MESSAGE);
//                                }).start();
//                                return 1;
//                            }
//                            catch (Exception e)
//                            {
//                                return 0;
//                            }
//                        }
//                ));

        _fw.add(new PrimitiveWord
                (
                        "what", false, "Show description about a word",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                String wordname = Utilities.readString (dStack);
                                BaseWord bw = _fw.search(wordname); //dictionary.search(word);
                                if (bw == null)
                                    return 0;
                                String info = bw.getInfo();
                                dStack.push(info);
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "collect", false, "collects all numbers from stack into sequence",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence sq = new DoubleSequence();
                                while (!dStack.isEmpty() && Utilities.canBeDouble(dStack.peek()))
                                {
                                    sq.add(Utilities.readDouble(dStack));
                                }
                                dStack.push (sq.reverse());
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

        _fw.add(new PrimitiveWord
                (
                        "scatter", false, "desintegrate sequence onto stack",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                DoubleSequence ds = Utilities.readDoubleSequence(dStack);
                                for (Double d : ds.asPrimitiveArray())
                                {
                                    dStack.push(d);
                                }
                                return 1;
                            }
                            catch (Exception e)
                            {
                                return 0;
                            }
                        }
                ));

    }
}
