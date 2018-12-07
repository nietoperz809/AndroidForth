package jforth.forthwords;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import jforth.JForth;
import jforth.PrimitiveWord;
import jforth.Utilities;
import jforth.WordsList;

import static com.pit.administrator.textconsole.MainActivity.getActivity;

public class AndroidFiller
{
    static void fill (WordsList _fw, PredefinedWords predefinedWords)
    {
        _fw.add(new PrimitiveWord
                (
                        "ask", false, "Show yes/no box",
                        (dStack, vStack) ->
                        {
                            try
                            {
                                final String txt = Utilities.readString (dStack);
                                final Thread tr = Thread.currentThread();
                                getActivity().runOnUiThread(() ->
                                {
                                    // Use the Builder class for convenient dialog construction
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(getActivity());
                                    builder.setTitle(txt+"?");
                                    builder.setPositiveButton("Yes", (dialog, id) ->
                                    {
                                        dStack.push(JForth.TRUE);
                                        tr.interrupt();
                                    });
                                    builder.setNegativeButton("No", (dialog, id) ->
                                    {
                                        dStack.push(JForth.FALSE);
                                        tr.interrupt();
                                    });

                                    Dialog dlg = builder.create();
                                    dlg.show();
                                });
                                try
                                {
                                    Thread.sleep(1000000);
                                }
                                catch (InterruptedException unused)
                                {

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
