package noweb;

public interface App
{
    int paramDEF_BG_CLR = 0x00FFFFE0; // default background color parameter: pale yellow
    int paramFORTH_CLR = 0x00000000; // Color for default FORTH output: htmlBlack
    int paramCURSOR_CLR = 0x00008000; // Color for cursor: dark green
    int paramERROR_CLR = 0x00FF0000; // Color for FORTH error text output: htmlRed
    int paramUSER_CLR = 0x00008080; // Color for echoing user keystrokes: htmlDarkTeal
    int paramROWS = 18;
    int paramCOLS = 70;

    int cursor ();
    void clear ();
    void print (String s);

    void emitChar (char c);
}
