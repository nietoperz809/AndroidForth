package noweb;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

/**
 * Created by Administrator on 3/13/2017.
 */
public class WebForthVM extends Thread
{   // Version VER.EXT
//    public static final int MAX_CHAR = 511; //5 CMJ
//    static final int CRR = 13;   // carriage return
//    static final int TIC = 39;   // tick: '
//    static final int BASEE = 10;
//    static final int VOCS = 8;
//    // //4 PSHP-2    static final int TOOLONG = 129;    // one more than max line length in source file //4 PSHP
////    static final int FIBLENGTH = 129;  // one more than max line length in source file //4 PSHP-2
//    static final int TOOLONG = 256;    // one more than max line length in source file //6 CJ
//    static final int FIND = 38; // FIND
    // Keyboard Event buffer
    private static final int KEYBUFLEN = 256;
    // Output character buffer
    private static final int OUTBUFLEN = 256;
    // Block I/O data
    private static final int BLKLEN = 1024;
    private static final int NUMBLOCKS = 100;
    private static final int BUFS = 8;
    private static final int VER = 1;
    private static final int EXT = 0x00;
    // Memory is 64K words, I/O buffer is 8 BLOCKS of 1024 words
    private static final int MEMORY_SIZE = 0x00010000 + 0x2000;
    private static final int PRIMITIVE = 0x00020000;
    private static final int TRUE = -1;
    private static final int FALSE = 0;
    private static final int BKSPP = 8;    // backspace
    private static final int LF = 10;   // line feed
    private static final int ERR = 27;   // Escape character
    // In this implementation, the address units is a 32-bit
    // DWORD, and the cell is also a 32-bit DWORD, so the
    // "size" of a cell is 1 unit.
    // (This implementation doesn't "know" about bytes.)
    private static final int CELLL = 1;
    private static final int EM = 0x0000FF00;// top of memory
    private static final int BUFFER0 = 0x00010000;// I/O buffer addr
    private static final int COLDD = 0x0100;       // cold start vector
    private static final int US = 128 * CELLL;    //  user area size in cells
    private static final int RTS = 128 * CELLL;    // return stack/TIB size
    private static final int UP = 0;            // start of user variables (US)
    private static final int RPP = EM - 8 * CELLL;   // start of return stack (RP0)
    private static final int TIBB = RPP - RTS;      // terminal input buffer (TIB)
    private static final int SPP = TIBB - 8 * CELLL; // start of data stack (SP0)
    private static final int BSPP = EM - 256 * CELLL; // bottom of data stack
    private static final int NAMEE = BSPP - 8 * CELLL; // name dictionary
    private static final int CODEE = COLDD + US;     // code dictionary
    private static final int TIB_SIZE = 80;       // size of terminal input buffer //1 PSHP-5
    private static final int PAD_OFFSET = 80;       // offset of PAD from HERE       //1 PSHP-5
    private static final int PAD_SIZE = 84;       // size of PAD                   //1 PSHP-5
    // Tracing mode constants
    private static final int NOTRACE = 0;
    private static final int STEPPING = 2;
    private static final int SHOWING = 4;
    private static final int FIBLENGTH = 256;  // one more than max line length in source file //6 CJ
    private static final int RSLASHO = 1;      // file access method (fam) value for read only //5 PSHP
    // Lexicon constants
    private static final int COMPO = 0x00000040;  // lexicon compile-only bit
    private static final int IMEDD = 0x00000080;  // lexicon immediate bit
    private static final int MASKK = 0xFFFFFF3F;  // lexicon bit mask
    // User Variables, allocated in forthStack[]
    private static final int SZERO = 1 + UP;     // SP0     Points to bottom of data stack
    private static final int RZERO = 2 + UP;  // RP0        Points to bottom of return stack
    private static final int TQKEY = 6 + UP;  // '?KEY  Execution vector of ?KEY
    private static final int TEMIT = 7 + UP;  // 'EMIT  Execution vector of EMIT
    private static final int TEXPE = 8 + UP;  // 'EXPECT    Execution vector of EXPECT
    private static final int TTAP = 9 + UP;  // 'TAP   Execution vector of TAP
    private static final int TECHO = 10 + UP; // 'ECHO  Execution vector of ECHO
    private static final int TPROM = 11 + UP; // 'PROMPT Execution vector of PROMPT
    private static final int BASE = 12 + UP; // BASE   Radix base for numeric I/O
    private static final int TEMP = 13 + UP; // tmp        Temp. storage for parse and find
    private static final int SPAN = 14 + UP; // SPAN   Character count for EXPECT
    private static final int INN = 15 + UP; // >IN        Pointer for parsing input stream
    private static final int NTIB = 16 + UP; // #TIB   Current count and address of the
    private static final int CSP = 18 + UP; // CSP        Stack pointer for error checking
    private static final int TEVAL = 19 + UP; // 'EVAL  Execution vector of EVAL
    private static final int TNUMB = 20 + UP; // 'NUMBER    Execution vector of NUMBER?
    private static final int HLD = 21 + UP; // HLD        Pointer in numeric output string
    private static final int HANDL = 22 + UP; // HANDLER    Ret stack ptr for error handling
    private static final int CNTXT = 24 + UP; // CONTEXT    Area to specify vocabulary search
    private static final int CRRNT = 32 + UP; // CURRENT    Points to vocabulary to extend
    private static final int CP = 34 + UP; // CP     Top of the code dictionary
    private static final int NP = 35 + UP; // NP     Top of the name dictionary
    private static final int LAST = 36 + UP; // LAST   Last name in the name dictionary
    private static final int BLK = 37 + UP; // BLK        BLOCK # being interpreted
    private static final int BUF0 = 38 + UP; // BUF0   BLOCK # for buffer area 0
    // = 45             BLOCK # for buffer area 7
    private static final int UPD0 = BUF0 + BUFS;
    // = 46             Flag that buffer 0 is updated
    // = 54             Flag that buffer 7 is updated
    private static final int LSTBUF = UPD0 + BUFS;
    //          BUF # most-recently used
    private static final int BLOCKS = LSTBUF + 1;
    //          Number of blocks in current BLOCK file
    private static final int CURBLK = BLOCKS + 1;
    //          BLOCK # of most recently accessed BLOCK
    private static final int CURPOS = CURBLK + 1;
    //          Current address inside current block
    private static final int TRACING = CURPOS + 1;
    //          Indicates tracing mode
    private static final int NULLSTR = TRACING + 1;
    //          Empty error string
    private static final int CAPS = NULLSTR + 1;
    //          Indicates case sensitivity, TRUE for case-sensitive
    private static final int FG_CLR = CAPS + 1;
    //          Current forground color for text output
    private static final int BG_CLR = FG_CLR + 1;
    //          Current background color for text output
    private static final int STATE = BG_CLR + 1;
    //          Interpretation/compilation state
    private static final int LEAVES = STATE + 1;
    //          Pointer to unresolved LEAVEs
    private static final int DEF_BG_CLR = LEAVES + 1;
    //          Default background color
    private static final int FORTH_CLR = DEF_BG_CLR + 1;
    //         terminal input buffer.
    //          Color for default FORTH output
    private static final int CURSOR_CLR = FORTH_CLR + 1;
    //          Color for cursor
    private static final int ERROR_CLR = CURSOR_CLR + 1;
    //          Color for FORTH error text output
    private static final int USER_CLR = ERROR_CLR + 1;
    //          Color for echoing user keystrokes
    private static final int ABORT$ = USER_CLR + 1;   //1 PSHP
    //          Address of ABORT" string
    private static final int NAME$ = ABORT$ + 1;     //1 PSHP
    //          Address of unrecognized name string
    private static final int DEEPBLK = NAME$ + 1;    //1 PSHP
    //         order (8 vocabularies)
    //          Holds deepest BLK  # when an exception is thrown
    private static final int DEEPPOS = DEEPBLK + 1;  //1 PSHP
    //   33;            Vocabulary link pointer
    //          Holds >IN value associated with DEEPBLK
    private static final int SID = DEEPPOS + 1;                                              //4 PSHP-2
    //          Holds SOURCE-ID value                   //4 PSHP-2
    private static final int DEEPSID = SID + 1;                                              //4 PSHP-2
    //          Holds deepest SOURCE-ID when an exception is thrown //4 PSHP-2
    private static final int DEEPLINE = DEEPSID + 1;                                         //4 PSHP-F
    // Holds line number of deepest file when an exception is thrown //4 PSHP-F
    private static final int ROWS = DEEPLINE + 1;                                            //5 CMJ
    //          Holds height of console in characters   //5 CMJ
    private static final int COLS = ROWS + 1;                                                //5 CMJ
    // Primitive word Symbols
    private static final int BYE = 1;  // BYE
    private static final int QRX = 2;  // ?RX
    private static final int TXSTO = 3;  // TX!
    private static final int STOIO = 4;  // !IO
    private static final int DOLIT = 5;  // doLIT
    private static final int EXIT = 6;  // EXIT
    private static final int EXECU = 7;  // EXECUTE
    private static final int DONXT = 8;  // next
    private static final int QBRAN = 9;  // ?branch
    private static final int BRAN = 10; // branch
    private static final int STORE = 11; // !
    private static final int AT = 12; // @
    private static final int CSTOR = 13; // C!
    private static final int CAT = 14; // C@
    private static final int RFROM = 15; // R>
    private static final int RAT = 16; // R@
    private static final int RPAT = 17; // RP@
    private static final int RPSTO = 18; // RP!
    private static final int TOR = 19; // >R
    private static final int SPAT = 20; // SP@
    private static final int SPSTO = 21; // SP!
    private static final int DROP = 22; // DROP
    private static final int DUPP = 23; // DUP
    private static final int SWAP = 24; // SWAP
    private static final int OVER = 25; // OVER
    //          Holds width of console in characters    //5 CMJ
    private static final int ROT = 26; // ROT
    // .........................................................
    private static final int ZLESS = 27; // 0<
    private static final int ANDD = 28; // AND
    private static final int ORR = 29; // OR
    private static final int XORR = 30; // XOR
    private static final int UPLUS = 31; // UM+
    private static final int UMMOD = 32; // UM/MOD
    private static final int UMSTA = 33; // UM*
    private static final int INC = 34; // 1+
    private static final int DEC = 35; // 1-
    private static final int DEPTH = 36; // DEPTH
    private static final int PICK = 37; // PICK
    private static final int UINIT = 39; // UINIT
    private static final int VERBO = 40; // VERBOSE
    private static final int QUIET = 41; // QUIET
    private static final int BLOCKRD = 42; // block@
    private static final int BLOCKWR = 43; // block!
    private static final int SCRNREF = 44; // screen-refresh
    private static final int PLOOP = 45; // +LOOP
    private static final int EXECTR = 46; // EXEC-TRACE
    private static final int ATXY = 47; // AT-XY
    private static final int PAGE = 48;  // PAGE
    private static final int MS = 49;  // MS (milliseconds)
    private static final int PSEMI = 50; // (;)       //1 PSHP
    private static final int doCON = 51; // doCON     //1 PSHP
    private static final int doUSER = 52; // doUSER    //1 PSHP
    private static final int ZEQUAL = 53; // 0=        //1 PSHP-5
    private static final int OPENFILE = 54; // OPEN-FILE                             //5 PSHP
    private static final int CLOSEFILE = 55; // CLOSE-FILE                            //5 PSHP
    private static final int FILEPOSITION = 56; // FILE-POSITION                         //5 PSHP
    private static final int FILESIZE = 57; // FILE-SIZE                             //5 PSHP
    private static final int READFILE = 58; // READ-FILE                             //5 PSHP
    private static final int READLINE = 59; // READ-LINE                             //5 PSHP
    private static final int REPOSITIONFILE = 60; // REPOSITION-FILE                       //5 PSHP
    private static final int FILENAME = 61; // FILE-NAME                             //5 PSHP
    private static final int LINENUMBER = 62; // LINE-NUMBER                           //5 PSHP
    private static final int SETLINENUMBER = 63; // SET-LINE-NUMBER                       //5 PSHP
    private static final int REPEATLINE = 64; // REPEAT-LINE                           //4 PSHP-2
    private static final int KEYQ = 65; // KEY?                                  //4 CMJ
    private static final int GETXY = 66; // GET-XY                                //5 CMJ
    private static final int NAMESEARCH = 67; // NAME-SEARCH                           //5 CMJ
    // All primitives should have lower numbers than this one!!
    private static final int __the_last__ = 68;                                          //5CMJ
    // Primitive word Symbols
    private static final String[] primName =
            {
                    "( ??? )", "BYE", "?RX", "TX!",
                    "!IO", "doLIT", "EXIT", "EXECUTE",
                    "next", "?branch", "branch", "!",
                    "@", "C!", "C@", "R>",
                    "R@", "RP@", "RP!", ">R",
                    "SP@", "SP!", "DROP", "DUP",
                    "SWAP", "OVER", "ROT", "0<",
                    "AND", "OR", "XOR", "UM+",
                    "UM/MOD", "UMSTA", "1+", "1-",
                    "DEPTH", "PICK", "find", "UINIT",
                    "VERBOSE", "QUIET", "block@", "block!",
                    "screen-refresh", "+LOOP", "EXEC-TRACE", "AT-XY",
                    "PAGE", "MS", "(;)", "doCON",
                    "doUSER", "0=",
                    "OPEN-FILE",                                                                  //5 PSHP
                    "CLOSE-FILE",                                                                 //5 PSHP
                    "FILE-POSITION",                                                              //5 PSHP
                    "FILE-SIZE",                                                                  //5 PSHP
                    "READ-FILE",                                                                  //5 PSHP
                    "READ-LINE",                                                                  //5 PSHP
                    "REPOSITION-FILE",                                                            //5 PSHP
                    "FILE-NAME",                                                                  //5 PSHP
                    "LINE-NUMBER",                                                                //5 PSHP
                    "SET-LINE-NUMBER",                                                            //5 PSHP
                    "REPEAT-LINE",                                                                //4 PSHP-2
                    "KEY?",                                                                       //5 CMJ
                    "GET-XY",                                                                     //5 CMJ
                    "NAME-SEARCH"                                                                 //5 CMJ
            };
    // 256-511 used for non-graphic keys
    private final char[] keybuf;   // Circular buffer
    private final int[] forthStack;          // Accessible eForth memory
    private final char[] blkfile;  // simulated file
    // Reference to Applet
    private final App app;
    private final Hashtable<Integer, WebForthFile> files = new Hashtable<>(); // holds references to open files               //5 PSHP
//    protected int blocks;
    // --Commented out by Inspection (11/30/2018 8:34 AM):protected int _CODE = CODEE;    // initialize code pointer
//    protected int _USER = 4 * CELLL;  // first user variable offset
    private int keyp;     // "Put" buffer location
    private int keyg;     // "Get" buffer location
//    private char[] outbuf;   // Circular buffer
    private int outp;     // "Put" buffer location
    private int outg;     // "Get" buffer location
    private int ip;         // instruction pointer
    private int sp;         // parameter stack pointer
    private int rp;         // return stack pointer
    private volatile boolean bGone;    // Indicates time to leave
    // Kernel markers
    private int _LINK = 0;        // force a null link
    private int _NAME = NAMEE;    // initialize name pointer
    // File input data                                                                 //5 PSHP
    private int nextFileid = 1;        // used to track assignment of fileids          //5 PSHP

    public WebForthVM (App noWebForthApp)
    {
        app = noWebForthApp;
        forthStack = new int[MEMORY_SIZE];
        keybuf = new char[KEYBUFLEN];
        keyg = 0;
        keyp = 0;
        bGone = false;
        forthStack[BASE] = 10;
        blkfile = new char[NUMBLOCKS * BLKLEN]; // Store up to 100 blocks
        for (int i = 0; i < blkfile.length; i++)
        {
            blkfile[i] = ' ';
        }
    }

    // Unsigned comparison function
    private static boolean lessThan (long a, long b)
    {
        if (a < 0)
        {
            return b < 0 && (a < b);
        }
        else
        {
            return b < 0 || (a < b);
        }
    }

    private synchronized int keys ()
    {
        if ((keyp - keyg) >= 0)
        {
            return (keyp - keyg);
        }
        else
        {
            return (keyp + KEYBUFLEN - keyg);
        }
    }

    public synchronized void enqueueKey (char key)
    {
        keybuf[keyp] = key;
        keyp = (keyp + 1) % KEYBUFLEN;
    }

    private synchronized char dequeueKey ()
    {
        char key = keybuf[keyg];
        keyg = (keyg + 1) % KEYBUFLEN;
        return (key);
    }

// --Commented out by Inspection START (11/30/2018 8:43 AM):
//    public synchronized int outs ()
//    {
//        if ((outp - outg) >= 0)
//        {
//            return (outp - outg);
//        }
//        else
//        {
//            return (outp + OUTBUFLEN - outg);
//        }
//    }
// --Commented out by Inspection STOP (11/30/2018 8:43 AM)

// --Commented out by Inspection START (11/30/2018 8:42 AM):
//    public synchronized void enqueueOutChar (char c)
//    {
//        outbuf[outp] = c;
//        outp = (outp + 1) % OUTBUFLEN;
//    }
// --Commented out by Inspection STOP (11/30/2018 8:42 AM)

// This version reads all the source (up to the limit NumBlocks) from the <applet>.HTML file into
// memory when the applet starts. It is then served up through a set of 8 buffers in classical
// Forth "least-recently used" fashion.
//
// A cleaner way would be to read on demand, which would avoid a limit and justify the 8 buffer
// mechanism. Philip Preston has supplied source to do this. It hasn't been implemented yet as
// reading is so quick anyway and there are more urgent things than improving purity to do.

// --Commented out by Inspection START (11/30/2018 8:40 AM):
//    public synchronized char dequeueOutChar ()
//    {
//        char out = outbuf[outg];
//        outg = (outg + 1) % OUTBUFLEN;
//        return (out);
//    }
// --Commented out by Inspection STOP (11/30/2018 8:40 AM)

// --Commented out by Inspection START (11/30/2018 8:41 AM):
//    public void LoadSource (int block, int line, String source)
//    {
//        //Here is an extension that allows an extra character at
//        //the beginning of HTML source blocks so the browser does
//        //not strip leading blanks.
//
//        if (source.charAt(0) == '~')
//        {
//            source = source.substring(1);
//        }
//        block--;                    // Blocks start with 1
//        int i;
////4 CMJ        for (i = 0 ; i < 64 ; i++)  // Clear the line
//        for (i = source.length(); i < 64; i++)  // Clear the line
//        {
//            blkfile[(block * BLKLEN) + (line * 64) + i] = ' ';
//        }
//        for (i = 0; i < source.length(); i++)
//        {
//            blkfile[(block * BLKLEN) + (line * 64) + i] = source.charAt(i);
//        }
//    }
// --Commented out by Inspection STOP (11/30/2018 8:41 AM)

// --Commented out by Inspection START (11/30/2018 8:43 AM):
//    protected int name ()    // Opening for new word header
//    {
//        return forthStack[NP];
//    }
// --Commented out by Inspection STOP (11/30/2018 8:43 AM)

// --Commented out by Inspection START (11/30/2018 8:42 AM):
//    protected int last ()    // Last word added
//    {
//        return forthStack[LAST];
//    }
// --Commented out by Inspection STOP (11/30/2018 8:42 AM)

    private void push (int n)
    {
        forthStack[sp] = n;
        sp--;
    }

    private int top ()
    {
        return forthStack[sp + 1];
    }

    private void dup ()
    {
        forthStack[sp] = forthStack[sp + 1];
        sp--;
    }

    private void swap ()
    {
        int n = forthStack[sp + 1];
        forthStack[sp + 1] = forthStack[sp + 2];
        forthStack[sp + 2] = n;
    }

    private void over ()
    {
        forthStack[sp] = forthStack[sp + 2];
        sp--;
    }

    private void rot ()
    {
        int n = forthStack[sp + 1];
        forthStack[sp + 1] = forthStack[sp + 3];
        forthStack[sp + 3] = forthStack[sp + 2];
        forthStack[sp + 2] = n;
    }

// --Commented out by Inspection START (11/30/2018 8:42 AM):
//    public int index ()
//    {
//        return SPP - sp;
//    }
// --Commented out by Inspection STOP (11/30/2018 8:42 AM)

// --Commented out by Inspection START (11/30/2018 8:42 AM):
//    public void setIndex (int n)
//    {
//        sp = SPP - n;
//    }
// --Commented out by Inspection STOP (11/30/2018 8:42 AM)

    private void pick (int n)
    {
        forthStack[sp] = forthStack[sp + n + 1];
        sp--;
    }

    //Initialize User Variables
    private void UInit ()
    {
        int i;
        int CTOP = forthStack[CP];
        for (i = UP; i < LAST; i++)
        {
            forthStack[i] = 0;
        }
        forthStack[SZERO] = SPP;
        forthStack[RZERO] = RPP;
        forthStack[BASE] = 10;
        forthStack[NTIB + 1] = TIBB;
        forthStack[CP] = CTOP;
        forthStack[NP] = _NAME;
        forthStack[LAST] = _NAME + (2 * CELLL);
        forthStack[BLK] = 0;
        for (i = 0; i < BUFS; i++)
        {
            forthStack[BUF0 + i] = -1;   // buffer unused
            forthStack[UPD0 + i] = 0;    // buffer not updated
        }
        forthStack[LSTBUF] = BUFS - 1;
        forthStack[BLOCKS] = blkfile.length / BLKLEN;
        forthStack[CURBLK] = -1;
        forthStack[CURPOS] = 0;
        forthStack[TRACING] = NOTRACE;
        forthStack[NULLSTR] = 0;
        forthStack[CAPS] = 0;  // not case-sensitive by default
        forthStack[DEF_BG_CLR] = app.paramDEF_BG_CLR;
        forthStack[FORTH_CLR] = app.paramFORTH_CLR;
        forthStack[CURSOR_CLR] = app.paramCURSOR_CLR;
        forthStack[ERROR_CLR] = app.paramERROR_CLR;
        forthStack[USER_CLR] = app.paramUSER_CLR;
        forthStack[FG_CLR] = forthStack[FORTH_CLR];   //3 CMJ Initial values
        forthStack[BG_CLR] = forthStack[DEF_BG_CLR];  //3 CMJ Initial values
        forthStack[ROWS] = app.paramROWS;  //5 CMJ
        forthStack[COLS] = app.paramCOLS;  //5 CMJ
    }

    //Initialize Vectored Routines
    private void VInit ()
    {
        forthStack[TQKEY] = doFind("?RX");
        forthStack[TEMIT] = doFind("TX!");
        forthStack[TEXPE] = doFind("accept");
        forthStack[TTAP] = doFind("kTAP");
        forthStack[TECHO] = doFind("TX!");
        forthStack[TPROM] = doFind(".OK");
        forthStack[TEVAL] = doFind("$INTERPRET");
        forthStack[TNUMB] = doFind("NUMBER?");
    }

    private void header (String name)
    {
        // A header is:
        //   1 cell for Code Address (CA), given my here()
        //   1 cell for Link (Name Address (NA) of previous name)
        //   1 cell for Name Length (in chars) and flags
        //   n cells, one for each char in name

        int _LEN = (name.length() & 0x1F)
                / CELLL;    // string cell count, round down
        _NAME = _NAME - ((_LEN + 3) * CELLL);
        // new header on cell boundary
        int na = _NAME;         // pointer within name definition
        forthStack[na++] = here();       // set token pointer
        forthStack[na++] = _LINK;        // set link to previous name
        _LINK = na;             // next link points to this name string
        forthStack[na++] = _LEN;
        for (int i = 0; i < _LEN; i++)
        {                       // lay down letters of name
            forthStack[na++] = (int) (name.charAt(i));
        }
        forthStack[LAST] = _LINK;        // Update LAST user variable
    }

    // Build entry for primitive routine in dictionary
    private void primitive (String name, int inst)
    {
        header(name);                   // Build header
        forthStack[_NAME] = inst + PRIMITIVE;    // Set CA to token
    }

    //////////////////////////////////////////////////////////
    // Java to Forth "Meta" compiler
    // These routines allow this Java class to build an
    // ANS Forth in the memory array forthStack[]
    //////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////
    // Dictionary entry management routines
    //////////////////////////////////////////////////////////

    // Mark the last definition as an IMMEDIATE word
    private void immed ()
    {
        forthStack[_LINK] |= IMEDD;
    }

    // Mark the last definition as a COMPILE-ONLY word
    private void compo ()
    {
        forthStack[_LINK] |= COMPO;
    }

    // Compiles code in code dictionary
    private void code (int inst)
    {
        if (inst >= 0 && inst < __the_last__)
        {
            forthStack[here()] = inst + PRIMITIVE;   // Write token
            forthStack[CP]++;                        // Increment HERE
        }
        else
        {
            String s = "Bad Primitive " + inst
                    + " in definition of "
                    + makeString(_LINK);
            throw new RuntimeException(s);
        }
    }

    // Compiles data in code dictionary
    private void comma (int i)
    {
        forthStack[here()] = i;                  // Write data
        forthStack[CP]++;                        // Increment HERE
    }

    // Compiles code and data to put a literal number
    // on data stack at execution time
    private void literal (int lit)
    {
        code(DOLIT);
        comma(lit);
    }

    // Compiles counted literal strings in code dictionary
    private void strlit (String s)
    {
        forthStack[here()] = s.length();
        forthStack[CP]++;                        // Increment "HERE"
        for (int i = 0; i < s.length(); i++)
        {
            forthStack[here() + i] = (int) (s.charAt(i));
        }
        forthStack[CP] += s.length();            // Update HERE
    }

    //////////////////////////////////////////////////////////
    // Compiling routines
    //////////////////////////////////////////////////////////

    // Allot data space                                                            //5 PSHP
    private void allot (int n)
    {                                                  //5 PSHP
        forthStack[CP] += n;                                                                //5 PSHP
    }                                                                              //5 PSHP

    // Determines if name in dictionary matches a given string
    private boolean matched (String str, int s)
    {
        // get length from lower 5 bits (mask off lexicon bits)
        int l = 0x1F & forthStack[s];
        if (l != str.length())
        {
            return false;
        }
        boolean match = true;
        for (int i = 0; i < l && match; i++)
        {
            if (forthStack[s + 1 + i] != (int) (str.charAt(i)))
            {
                match = false;
            }
        }
        return match;
    }

    // Searches for name, and returns CA if found, -1 if not
    private int doFind (String name, int na)
    {
        int ca = -1;
        int w = na;
        boolean found = false;
        do
        {
            if (matched(name, w))
            {
                found = true;
                ca = forthStack[codeField(w)];
            }
            else
            {   // get next word
                w = forthStack[linkField(w)];
            }
        } while (w != 0 && !found);
        return ca;
    }

    // Searches for name, and returns
    private int doFind (String name)
    {
        int na = _LINK;
        int ca = doFind(name, na);      // CA if found, -1 if not
        if (ca < 0)
        {
            String s = "??? --> " + name + " in definition of "
                    + makeString(_LINK);
            throw new RuntimeException(s);
        }
        return ca;
    }

    // Make a java.lang.String from a Forth string                                 //5 PSHP
    // described by its address and length                                         //5 PSHP
    private String addrLenToString (int addr, int len)
    {                                    //5 PSHP
        StringBuffer sb = new StringBuffer(len);                                   //5 PSHP
        for (int i = 0; i < len; i++)
        {                                            //5 PSHP
            sb.append((char) forthStack[addr + i]);                                         //5 PSHP
        }                                                                          //5 PSHP
        return new String(sb);                                                     //5 PSHP
    }                                                                              //5 PSHP

    //////////////////////////////////////////////////////////
    // Dictionary Searching routines
    //////////////////////////////////////////////////////////

    // Finds and compiles subroutine call in code dictionary
    private void call (String name)
    {
        int ca = doFind(name);
        if (ca < 0)
        {
            return;
        }
        forthStack[here()] = ca;
        forthStack[CP]++;            // Increment HERE
    }

// --Commented out by Inspection START (11/30/2018 8:44 AM):
//    // Simple code dump utility
//    protected void look ()
//    {
//        int start = pop();
//        int count = here() - start;
//        for (int i = 0; i < count; i++)
//        {
//            app.print("[");
//            app.print(String.valueOf(start + i));
//            app.print("]\t");
//            printName(forthStack[start + i]);
//            app.print("\t");
//            app.print(String.valueOf(forthStack[start + i]));
//            app.print("\t");
//            app.print(String.valueOf((char) forthStack[start + i]));
//            app.print("\n");
//        }
//    }
// --Commented out by Inspection STOP (11/30/2018 8:44 AM)

    // eForth Virtual Machine low level stack routines
    private int pop ()
    {
        sp++;
        return forthStack[sp];
    }

    // eForth Virtual Machine dictionary address routines
    private int here ()    // Opening for new code or data
    {
        return forthStack[CP];
    }

    private void printName (int inst)
    {
        String name;
        // Check for primitive instruction bit
        if (inst > PRIMITIVE)
        {
            // Strip off primitive instruction bit
            inst -= PRIMITIVE;
            name = primName[inst];
        }
        else
        {
            name = getName(inst);
        }
        app.print(name);
        if (name.length() < 8)
        {
            app.print("\t");
        }
    }

    //////////////////////////////////////////////////////////
    // Subroutine compiling
    //////////////////////////////////////////////////////////

    // Find a name based on its code address
    private String getName (int ca)
    {
        int w = forthStack[LAST];
        boolean found = false;
        String name = "( ??? )";
        do
        {
            if (ca == forthStack[codeField(w)])
            {
                found = true;
                name = makeString(w);
            }
            else
            {   // get next word
                w = forthStack[linkField(w)];
            }
        } while (w != 0 && !found);
        return name;
    }

    //////////////////////////////////////////////////////////
    // Metacompiling debug routines
    //////////////////////////////////////////////////////////

    private int codeField (int nameAddr)
    {
        return nameAddr - 2;
    }

    // makes a Java.lang.String from a counted Forth string                        //5 PSHP
    private String makeString (int n)                                             //5 PSHP
    {                                                                              //5 PSHP
        StringBuffer sb = new StringBuffer();                                      //5 PSHP
        for (int i = 0; i < forthStack[n]; i++)                                           //5 PSHP
        {
            sb.append((char) forthStack[n + 1 + i]);                                         //5 PSHP
        }
        return new String(sb);                                                                //5 PSHP
    }                                                                              //5 PSHP

    private int linkField (int nameAddr)
    {
        return nameAddr - 1;
    }

    // Display current state of eForth VM and
    // print name of next routine to execute
    private void inform (int inst)
    {
        if (inst == (QUIET + PRIMITIVE))
        {
            return;         // No need to show this
        }

        app.print("\nstack:\t");
        if ((SPP - sp) > 0)
        {
            app.print(String.valueOf(top()));
        }
        else
        {
            app.print("---");
        }
        app.print("   return:\t");
        if ((RPP - rp) > 0)
        {
            app.print(String.valueOf(forthStack[rp]));
        }
        else
        {
            app.print("---");
        }

        app.print("   ip:\t");
        app.print(String.valueOf(ip));
        if (inst > PRIMITIVE)
        {
            app.print("\t");
        }
        else
        {
            app.print("\t: ");
        }
        printName(inst);
        app.print("\t");
        if ((SPP - sp) > 0)
        {
            int times = Math.min(SPP - sp, 4);
            for (int i = 1; i < times; i++)
            {
                pick(i);
                app.print("\n");
                if (i == 1)
                {
                    app.print("(");
                    app.print(String.valueOf(SPP - sp - 1));
                    app.print(")");
                }
                app.print("\t");
                app.print(String.valueOf(pop()));
                if (i == 1)
                {
                    app.print("   (");
                    app.print(String.valueOf(RPP - rp + 1));
                    app.print(")");
                }
                else
                {
                    app.print("\t");
                }
                if ((RPP - rp + 1) > i)
                {
                    app.print("\t");
                    app.print(String.valueOf(forthStack[rp + i]));
                    app.print("\t\t\t\t");
                }
                else
                {
                    app.print("\t\t\t\t\t\t");
                }
            }
        }

        if (ip < CODEE)
        {
            bGone = true;
        }
        if ((forthStack[TRACING] & STEPPING) != 0)
        {
            while (keys() == 0)
            {

            }
            char c = dequeueKey();
            if (c == (char) 27)
            {
                forthStack[TRACING] &= ~SHOWING;
            }
        }
        else if (keys() != 0)
        {
            char c = dequeueKey();
            if (c == (char) 27)
            {
                forthStack[TRACING] &= ~SHOWING;
            }
        }
    }

// --Commented out by Inspection START (11/30/2018 8:43 AM):
//    public void printU (int n)
//    {
//        long l = n;
//        if (l < 0)
//        {
//            l += 0x100000000L;
//        }
//        app.print(String.valueOf(l));
//    }
// --Commented out by Inspection STOP (11/30/2018 8:43 AM)

    //////////////////////////////////////////////////////////
    // Logic Structure metacompiling routines
    //////////////////////////////////////////////////////////

    // save place to put branch target address
    private void mark ()
    {
        push(here());
    }

    private void compBEGIN ()
    {
        mark();
    }

    private void compUNTIL ()
    {
        code(QBRAN);
        comma(pop());  // lay down address of loop top
    }

    private void compAGAIN ()
    {
        code(BRAN);
        comma(pop());  // lay down address of loop top
    }

    private void compWHILE ()
    {
        compIF();
        swap();
    }

    private void compREPEAT ()
    {
        compAGAIN();
        forthStack[pop()] = here();
    }


    private void compFOR ()
    {
        code(TOR);     // save loop limit
        push(here());  // save this dictionary location as loop top
    }

    private void compNEXT ()
    {
        code(DONXT);
        comma(pop());  // lay down address of loop top
    }

    private void compIF ()
    {
        code(QBRAN);   // lay down a conditional branch
        push(here());  // remember place to put branch target
        comma(0);      // temporary branch target, fixed later
    }

    private void compAFT ()
    {
        code(BRAN);    // lay down an unconditional branch
        push(here());  // remember place to put branch target
        comma(0);      // temporary branch target, fixed later
    }

    private void compTHEN ()
    {
        forthStack[pop()] = here();  // Fix up a branch target
    }

    private void compELSE ()
    {
        code(BRAN);    // lay down an unconditional branch
        push(here());  // remember place to put branch target
        comma(0);      // temporary branch target, fixed later
        swap();        // get previous unresolved branch
        forthStack[pop()] = here(); // Fix it to branch here (the else clause)
    }

    //////////////////////////////////////////////////////////
    // eForth Kernel for VM
    //////////////////////////////////////////////////////////
    private void doPrim (int inst)
    {
        int a, b, c, i, n;     // temporary integers
        char ch;                // temporary character
        WebForthFile file;      // temporary file reference                        //5 PSHP

        switch (inst)
        {
            // BYE      ( -- )    Exit eForth.
            case BYE:
                app.print(" Goodbye...");
                //System.exit(0);
                bGone = true;
                break;

            // ?RX      ( -- c T | F )
            // Return input character and true, or a false if no input.
            case QRX:
                yield();
                if (keys() > 0)
                {
                    push(dequeueKey());
                    push(TRUE);
                }
                else
                {
                    push(FALSE);
                }
                break;

            // TX!      ( c -- )
            // Send character c to the output device.
            case TXSTO:
//4 CMJ                    ch = (char)(pop() & MAX_CHAR);
                ch = (char) pop();  //4 CMJ
                app.emitChar(ch);
                yield();
                break;

            // !IO      ( -- )  Initialize the serial I/O devices.
            case STOIO:
                break;

            //  doLIT   ( -- w )  Push an inline literal.
            // ip points to inline value,
            // push it on stack and advance ip
            case DOLIT:
                push(forthStack[ip++]);
                break;

            // PSEMI ( -- )    Terminate a colon definition.
            // EXIT  ( -- )    Exit a colon definition.
            // resume instruction at saved address
            // These two words execute the same code but have different
            // tokens so they can be distinguished by the decompiler. //1 PSHP
            case PSEMI: /* no break */
            case EXIT:
                ip = forthStack[rp];
                rp++;
                break;

            // EXECUTE  ( ca -- ) Execute the word at ca.
            case EXECU:
                rp--;
                forthStack[rp] = ip;         // save current ip
                i = pop();
                if (i > PRIMITIVE)
                {
                    // Execute Primitive
                    doPrim(i - PRIMITIVE);
                }
                else
                {   // point to new code so it will execute next
                    ip = i;
                }
                break;

            // EXEC-TRACE( ca -- )
            // Enable tracing, then execute the word at ca.
            case EXECTR:
                rp--;
                forthStack[rp] = ip;         // save current ip
                i = pop();
                forthStack[TRACING] |= SHOWING;
                inform(i);
                if (i > PRIMITIVE)
                {
                    // Execute Primitive
                    doPrim(i - PRIMITIVE);
                }
                else
                {   // point to new code so it will execute next
                    ip = i;
                }
                break;

            // next ( -- )    Run time code for the single index loop
            case DONXT:
                i = forthStack[rp];      // i is loop index
                rp++;
                if (i > 0)
                {
                    i--;        // decrement index
                    rp--;
                    forthStack[rp] = i;  // put back on stack
                    ip = forthStack[ip]; // go back to begining of loop
                }
                else
                {
                    ip++;
                }
                break;

            // ?branch  ( f -- )  Branch if flag is zero.
            case QBRAN:
                if (pop() == FALSE)
                {
                    ip = forthStack[ip];     // reset ip to inline address
                }
                else
                {
                    ip++;           // bump ip past branch addr
                }
                break;

            // branch   ( -- )    Branch to an inline address.
            case BRAN:
                ip = forthStack[ip];         // reset ip to inline address
                break;

            // !        ( w a -- )  Pop the data stack to memory.
            case STORE:
                a = pop();      // a is address
                forthStack[a] = pop();   // store number from stack in memory
                break;          // at address a

            // @        ( a -- w )  Push memory data onto data stack.
            case AT:
                a = pop();      // a is address
                push(forthStack[a]);     // fetch data at address a in memory
                break;          // and put on stack

            // C!       ( c b -- )  Pop the data stack to byte memory.
            case CSTOR:
                a = pop();      // a is address
                forthStack[a] = pop();   // store number from stack in memory
                break;          // at address a

            // C@       ( b -- c )  Push byte memory dat onto data stack.
            case CAT:
                a = pop();      // a is address
                push(forthStack[a]);     // fetch data at address a in memory
                break;          // and put on stack

            // R>       ( -- w )  Pop the return stack to the data stack.
            case RFROM:
                push(forthStack[rp]);
                rp++;
                break;

            // R@       ( -- w )  Copy top of return stack to the data stack.
            case RAT:
                push(forthStack[rp]);
                break;

            //  RP@     ( -- a )  Push the current RP to the data stack.
            case RPAT:
                push(rp);
                break;

            //  RP!     ( a -- )  Set the return stack pointer.
            case RPSTO:
                rp = pop();
                break;

            // >R       ( w -- )  Push the data stack to the return stack.
            case TOR:
                rp--;
                forthStack[rp] = pop();
                break;

            // SP@      ( -- a )  Push the current data stack pointer.
            case SPAT:
                push(sp);
                break;

            // SP!      ( a -- )  Set the data stack pointer.
            case SPSTO:
                sp = pop();
                break;

            // DROP ( w -- )  Discard top stack item.
            case DROP:
                pop();
                break;

            // DUP      ( w -- w w )  Duplicate the top stack item.
            case DUPP:
                dup();
                break;

            // SWAP ( w1 w2 -- w2 w1 )  Exchange top two stack items.
            case SWAP:
                swap();
                break;

            // OVER ( w1 w2 -- w1 w2 w1 )  Copy second stack item to top
            case OVER:
                over();
                break;

            // 0<       ( n -- t )  Return true if n is negative.
            case ZLESS:
                n = pop();
                if (n < 0)
                {
                    push(TRUE);
                }
                else
                {
                    push(FALSE);
                }
                break;

            // AND      ( w w -- w )  Bitwise AND.
            case ANDD:
                n = pop();
                n &= pop();
                push(n);
                break;

            // OR       ( w w -- w )  Bitwise inclusive OR.
            case ORR:
                n = pop();
                n |= pop();
                push(n);
                break;

            // XOR      ( w w -- w )  Bitwise exclusive OR.
            case XORR:
                n = pop();
                n ^= pop();
                push(n);
                break;

            // UM+      ( w w -- w cy )  Add two numbers, return the sum and carry flag.
            case UPLUS:
                long k = pop();
                long j = pop();
                if (k < 0)
                {
                    k += 0x100000000L;
                }
                if (j < 0)
                {
                    j += 0x100000000L;
                }
                long s = k + j;
                if (s > 0xFFFFFFFFL)
                {
                    push((int) (s & 0xFFFFFFFFL));
                    push(1);
                }
                else
                {
                    push((int) s);
                    push(0);
                }
                break;

            // ROT  ( w3 w2 w1 -- w2 w1 w3 )
            // Rotate third item to top of stack.
            case ROT:
                rot();
                break;

            //  UM/MOD  ( udl udh u -- ur uq )
            //  Unsigned divide of a double by a single.
            //  Return mod and quotient.
            //  Maybe this could be replaced by a better algorithm
            case UMMOD:
                long dem = pop();       // denominator
                if (dem < 0)
                {
                    dem += 0x100000000L;// make unsigned
                }

                long nH = pop();        // high part of numerator
                if (nH < 0)
                {
                    nH += 0x100000000L; // make unsigned
                }

                long nL = pop();        // low part of numerator
                if (nL < 0)
                {
                    nL += 0x100000000L; // make unsigned
                }

                long num = nH << 32 | nL;
                long q = 0;
                long bit = 1;  // a 1 in the zero bit
                int bitPos = 0;

                while (!lessThan(num, dem))  // Find the MSB
                {
                    dem = dem << 1;
                    bit = bit << 1;
                    bitPos++;
                }

                long r = num;
                while (bitPos > 0)
                {
                    dem = dem >> 1 & 0x7FFFFFFFFFFFFFFFL; // Pull back one bit
                    bit = bit >> 1 & 0x7FFFFFFFFFFFFFFFL;
                    bitPos--;

                    if (!lessThan(r, dem))
                    {
                        r -= dem;
                        q += bit;
                    }
                }

                push((int) r);     // leave remainder on stack
                push((int) q);     // leave quotient on stack
                break;

            //  UM*     ( u u -- ud )
            //  Unsigned multiply. Return double product.
            case UMSTA:
                long m1 = pop();
                if (m1 < 0)
                {
                    m1 += 0x100000000L;
                }
                long m2 = pop();
                if (m2 < 0)
                {
                    m2 += 0x100000000L;
                }
                long prod = m1 * m2;
                push((int) (prod & 0xFFFFFFFFL)); // push low part on stack
                push((int) (prod >> 32 & 0xFFFFFFFFL));  // push high part
                break;

            //  1+       ( u -- u+1 )  // INC
            case INC:
                num = pop();
                if (num < 0)
                {
                    num += 0x100000000L;
                }
                num++;
                push((int) (num & 0xFFFFFFFFL));
                break;

            //  1-       ( u -- u-1 )  // DEC
            case DEC:
                num = pop();
                if (num < 0)
                {
                    num += 0x100000000L;
                }
                num--;
                push((int) (num & 0xFFFFFFFFL));
                break;

            //  DEPTH   ( -- n )  // Return depth of the data stack.
            case DEPTH:
                push(SPP - sp);
                break;

            //  PICK    ( ... +n -- ... w )
            // Copy the nth stack item to tos.
            case PICK:
                pick(pop());
                break;

            // initialize user area
            case UINIT:
                UInit();
                VInit();
                break;

            // print Forth VM state information
            case VERBO:
                forthStack[TRACING] = forthStack[TRACING] | SHOWING;
                break;

            // suppress printing Forth VM state information
            case QUIET:
                forthStack[TRACING] = forthStack[TRACING] & ~SHOWING;
                break;

            // block@ ( n addr -- f)
            // Fetch block n from block file and store at addr
            case BLOCKRD:
                a = pop();
                n = pop() - 1;
                if ((n * BLKLEN) <= blkfile.length)
                {
                    for (i = 0; i < BLKLEN; i++)
                    {
                        forthStack[a + i] = (int) blkfile[(n * BLKLEN) + i];
                    }
                    push(TRUE);
                }
                else
                {
                    push(FALSE);
                }
                break;

            // BLOCK! ( n addr -- )
            // Write buffer from addr to block n of block file
            case BLOCKWR:
                a = pop();
                n = pop() - 1;
                for (i = 0; i < BLKLEN; i++)
                {
                    blkfile[(n * BLKLEN) + i] = (char) forthStack[a + i];
                }
                break;

            // screen-refresh ( f -- )
            // turns console screen refresh on or off based on flag
// CMJ Changed to paint rest of screen instead of whole screen.
            case SCRNREF:
                i = pop();
                if (i != 0)
                {
                    //app.refresh = true;
// CMJ repaint() replaced by paint_rest()
//                      app.repaint();
                    //app.paint_rest();
                    if (i < -1)
                    {
                        // app.blink_cursor();
                    }
                }
                else
                {
                    //app.refresh = false;
                }
                break;

            // +LOOP ( n -- )
            // Increments loop index by n, compares to limit to see
            // if it is time to fall out of loop.
            case PLOOP:
                i = forthStack[rp];
                rp++;    // get current index
// Preston ANS fix:   i += pop();         // add loop increment
//                    if (i < forthStack[rp])      // is index less than limit?
                boolean f1 = ((forthStack[rp] - i) > 0);
                // old index less than limit?
                i += pop();         // add loop increment
                boolean f2 = ((forthStack[rp] - i) > 0);
                // new index less than limit?
                if (f1 == f2)       // index still same side of limit?
                {
                    ip = forthStack[ip];     // Yes: reset ip to branch
                    // back to inline address
                    rp--;
                    forthStack[rp] = i;  // save new index
                }
                else
                {
                    ip++;           // bump ip past branch addr
                    rp++;           // drop limit
                }
                break;

            // AT-XY ( x-coord y-coor -- )
            // Moves the console cursor to column "x-coord", row "y-coord"
            case ATXY:
                b = pop();
                a = pop();
                //app.setCur(a, b);
// CMJ Undesirable call to repaint screen removed
//                  app.repaint();
                break;

            // PAGE ( -- )
            // Clears console screen
            case PAGE:
                app.clear();
                break;

            // MS        ( a -- )  Pause 'a' milliseconds
            case MS:
// CMJ Added call to repaint screen
                //app.paint_rest();
                try
                {
                    sleep(pop());
                }
                catch (InterruptedException ignored)
                {
                }
                break;

//1 PSHP new primitives
            // doCON     (  -- x ) run-time for CONSTANT
            // doUSER    (  -- a ) run-time for user variable
            case doCON:  /* no break */
            case doUSER:
                push(forthStack[ip]);
                ip = forthStack[rp++];
                break;

            // 0=        ( x -- flag ) flag is true if x = zero.   //1 PSHP-5
            case ZEQUAL:                                           //1 PSHP-5
                forthStack[sp + 1] = (forthStack[sp + 1] == 0) ? TRUE : FALSE;       //1 PSHP-5
                break;                                             //1 PSHP-5

            // Catch any bad tokens
            default:
                doMorePrim(inst);
        }
    }

// Note: doPrim is spit into 2 parts to avoid an occasional problem that
// appears with Sun's AppletViewer v1.02 (and possibly other Java Virtual
// Machines too). The problem arises with this large case statement which
// occasionally leads to an exception at start-up.
// The problem does not appear on AppletViewer v1.16 and changing from Sun
// 'javac' compiler v1.02 to v1.16 makes no difference.
// Commenting out OPENFILE also avoids the problem (see below).
// It is not known whether the switch statement has too many cases or
// contains jumps which are too long or some other reason. CMJ 18-7-99


    private void doMorePrim (int inst)
    {
        int a, b, c, i, n;     // temporary integers
        char ch;                // temporary character
        WebForthFile file;      // temporary file reference                        //5 PSHP

        switch (inst)
        {

//4 PSHP-2                // OPEN-FILE       ( c-addr u fam -- fileid ior )                  //4 PSHP
//4 PSHP-2                // open file named in string c-addr u and return its fileid;       //4 PSHP
//4 PSHP-2                // fam is ignored since R/O is the only access method available    //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - file name not recognised                  //4 PSHP
//4 PSHP-2                //                 -37 - any other exception                       //4 PSHP
//4 PSHP-2                //                                                                 //4 PSHP
//4 PSHP-2                // Implementation details:                                         //4 PSHP
//4 PSHP-2                // OPEN-FILE reads and closes the external file, stores its        //4 PSHP
//4 PSHP-2                // data in a WebForthFile, and stores a reference to               //4 PSHP
//4 PSHP-2                // this in the files Hashtable keyed by the fileid value;          //4 PSHP
//4 PSHP-2                // fileids are assigned as consecutive integers starting at 1.     //4 PSHP
//4 PSHP-2                //                                                                 //4 PSHP
//4 PSHP-2                case OPENFILE:                                                     //4 PSHP
//4 PSHP-2                    pop();                                                         //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    String fileName = addrLenToString(b, a);                       //4 PSHP
//4 PSHP-2                    BufferedInputStream is = null;                                 //4 PSHP
//4 PSHP-2                    ByteArrayOutputStream os = new ByteArrayOutputStream();        //4 PSHP
//4 PSHP-2                    try {                                                          //4 PSHP
//4 PSHP-2                        is = new BufferedInputStream(                              //4 PSHP
//4 PSHP-2                          new URL(app.getCodeBase(), fileName).openStream());      //4 PSHP
//4 PSHP-2                        while ((c = is.read()) != -1) {                            //4 PSHP
//4 PSHP-2                            os.write(c);                                           //4 PSHP
//4 PSHP-2                        }                                                          //4 PSHP
//4 PSHP-2                        n = nextFileid++;                                          //4 PSHP
//4 PSHP-2                        file = new WebForthFile(os.toByteArray(), fileName);       //4 PSHP
//4 PSHP-2                        files.put(new Integer(n), file);                           //4 PSHP
//4 PSHP-2                        push(n); push(0);                                          //4 PSHP
//4 PSHP-2                    } catch (FileNotFoundException e) {                            //4 PSHP
//4 PSHP-2                        push(0); push(-38);                                        //4 PSHP
//4 PSHP-2                    } catch (Exception e) {                                        //4 PSHP
//4 PSHP-2                        push(0); push(-37);                                        //4 PSHP
//4 PSHP-2                    } finally {                                                    //4 PSHP
//4 PSHP-2                        try {                                                      //4 PSHP
//4 PSHP-2                            is.close();                                            //4 PSHP
//4 PSHP-2                        } catch (Exception e) {                                    //4 PSHP
//4 PSHP-2                        }                                                          //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // CLOSE-FILE      ( fileid -- ior )                               //4 PSHP
//4 PSHP-2                // close file identified by fileid.                                //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                //                                                                 //4 PSHP
//4 PSHP-2                // Implementation details:                                         //4 PSHP
//4 PSHP-2                // CLOSE-FILE removes from the files Hashtable the reference       //4 PSHP
//4 PSHP-2                // to the WebForthFile keyed by the fileid value (see OPEN-FILE    //4 PSHP
//4 PSHP-2                // above), so making the file eligible for garbage collection.     //4 PSHP
//4 PSHP-2                //                                                                 //4 PSHP
//4 PSHP-2                case CLOSEFILE:                                                    //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    push ((files.remove(new Integer(a)) == null) ? -38 : 0);       //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // FILE-POSITION   ( fileid -- ud ior )                            //4 PSHP
//4 PSHP-2                // return current position of file identified by fileid            //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case FILEPOSITION:                                                 //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(0); push(-38);                               //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push(file.getPosition()); push(0); push(0);                //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // FILE-SIZE       ( fileid -- ud ior )                            //4 PSHP
//4 PSHP-2                // return size of file identified by fileid                        //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case FILESIZE:                                                     //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(0); push(-38);                               //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push(file.getSize()); push(0); push(0);                    //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // READ-FILE       (c-addr u1 fileid -- u2 ior )                   //4 PSHP
//4 PSHP-2                // read till end of file up to u1 bytes from file identified       //4 PSHP
//4 PSHP-2                // by fileid into memory starting at c-addr; u2 is number of       //4 PSHP
//4 PSHP-2                // bytes read                                                      //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case READFILE:                                                     //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    c = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(-38);                                        //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push(file.readFile(forthStack, c, b)); push(0);                     //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // READ-LINE       (c-addr u1 fileid -- u2 flag ior )              //4 PSHP
//4 PSHP-2                // read till end of line up to u1 bytes from file identified       //4 PSHP
//4 PSHP-2                // by fileid into memory starting at c-addr; up to 2               //4 PSHP
//4 PSHP-2                // line-terminating characters may also be read; recognised line   //4 PSHP
//4 PSHP-2                // terminators are CR (0x0D) or LF (0x0A) singly or together in    //4 PSHP
//4 PSHP-2                // either order; u2 is the number of bytes read excluding line     //4 PSHP
//4 PSHP-2                // terminator(s); flag is false if operation fails (ior is         //4 PSHP
//4 PSHP-2                // non-zero) or if operation is initiated when file position       //4 PSHP
//4 PSHP-2                // equals file size (ior is zero) otherwise flag is true           //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case READLINE:                                                     //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    c = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(FALSE); push(-38);                           //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        if (file.getPosition() == file.getSize()) {                //4 PSHP
//4 PSHP-2                            push(0); push(FALSE); push(0);                         //4 PSHP
//4 PSHP-2                        } else {                                                   //4 PSHP
//4 PSHP-2                            push(file.readLine(forthStack, c, b)); push(TRUE); push(0);     //4 PSHP
//4 PSHP-2                        }                                                          //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // REPOSITION-FILE ( ud fileid -- ior )                            //4 PSHP
//4 PSHP-2                // set position of file identified by fileid to ud                 //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                //                 -36 - ud is an invalid file position            //4 PSHP
//4 PSHP-2                case REPOSITIONFILE:                                               //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    c = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(-38);                                                 //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push((file.setPosition(c) == c) ? 0 : -36);                //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // FILE-NAME       (c-addr u1 fileid -- u2 ior )                   //4 PSHP
//4 PSHP-2                // read up to u1 characters of name of file identified by fileid   //4 PSHP
//4 PSHP-2                // into memory starting at c-addr; u2 is number of characters      //4 PSHP
//4 PSHP-2                // read; if file name is longer than u1 characters then the        //4 PSHP
//4 PSHP-2                // leading characters are truncated                                //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case FILENAME:                                                     //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    c = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(-38);                                        //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push(file.readName(forthStack, c, b)); push(0);                     //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // LINE-NUMBER     ( fileid -- n ior )                             //4 PSHP
//4 PSHP-2                // return current line number of file identified by fileid or      //4 PSHP
//4 PSHP-2                // -1 if it is unknown (it becomes unknown after the use of        //4 PSHP
//4 PSHP-2                // READ-FILE or REPOSITION-FILE); note that after READ-LINE has    //4 PSHP
//4 PSHP-2                // read a complete line the current line number is one greater     //4 PSHP
//4 PSHP-2                // than the line number of the line just read                      //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case LINENUMBER:                                                   //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(0); push(-38);                                        //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        push(file.getLineNumber()); push(0);                       //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                // SET-LINE-NUMBER ( n fileid -- ior )                             //4 PSHP
//4 PSHP-2                // set current line number of file identified by fileid to n       //4 PSHP
//4 PSHP-2                // ior values are:   0 - operatiion succeeded                      //4 PSHP
//4 PSHP-2                //                 -38 - fileid not recognised                     //4 PSHP
//4 PSHP-2                case SETLINENUMBER:                                                //4 PSHP
//4 PSHP-2                    a = pop();                                                     //4 PSHP
//4 PSHP-2                    b = pop();                                                     //4 PSHP
//4 PSHP-2                    file = (WebForthFile)files.get(new Integer(a));                //4 PSHP
//4 PSHP-2                    if (file == null) {                                            //4 PSHP
//4 PSHP-2                        push(-38);                                                 //4 PSHP
//4 PSHP-2                    } else {                                                       //4 PSHP
//4 PSHP-2                        file.setLineNumber(b);                                     //4 PSHP
//4 PSHP-2                        push(0);                                                   //4 PSHP
//4 PSHP-2                    }                                                              //4 PSHP
//4 PSHP-2                    break;                                                         //4 PSHP
//4 PSHP-2
            // OPEN-FILE       ( c-addr u fam -- fileid ior )                  //4 PSHP
            // Open the file named in string c-addr u and return its fileid    //4 PSHP-2
            // and set the file position to the start of the file. fam (file   //4 PSHP-2
            // access method) must equal R/O as this is the only access        //4 PSHP-2
            // method available.                                               //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - file name not recognised                  //4 PSHP
            //                 -37 - any other exception                       //4 PSHP
            //                                                                 //4 PSHP
            // Implementation details:                                         //4 PSHP
            // OPEN-FILE reads and closes the external file, stores its        //4 PSHP
            // data in a WebForthFile, and stores a reference to               //4 PSHP
            // this in the files Hashtable keyed by the fileid value.          //4 PSHP-2
            // fileids are assigned as consecutive integers starting at 1.     //4 PSHP
            //                                                                 //4 PSHP
            case OPENFILE:                                                     //4 PSHP
                if (pop() == RSLASHO)
                {                                        //4 PSHP-2
                    a = pop();                                                 //4 PSHP-2
                    b = pop();                                                 //4 PSHP-2
                    String fileName = addrLenToString(b, a);                   //4 PSHP-2
                    //4 PSHP-2
                    ByteArrayOutputStream os = new ByteArrayOutputStream();    //4 PSHP-2
                    try (BufferedInputStream is = null)
                    {                                                      //4 PSHP-2
                        //                        is = new BufferedInputStream(                          //4 PSHP-2
//                                new URL(app.getCodeBase(), fileName).openStream());  //4 PSHP-2
                        while ((c = is.read()) != -1)
                        {                        //4 PSHP-2
                            os.write(c);                                       //4 PSHP-2
                        }                                                      //4 PSHP-2
                        n = nextFileid++;                                      //4 PSHP-2
                        file = new WebForthFile(os.toByteArray(), fileName);   //4 PSHP-2
                        files.put(n, file);                       //4 PSHP-2
                        push(n);
                        push(0);                                      //4 PSHP-2
                    }
                    catch (Exception e)
                    {                                    //4 PSHP-2
                        push(0);
                        push(-37);                                    //4 PSHP-2
                    }                                                //4 PSHP-2
                    //4 PSHP-2
                    //4 PSHP-2
                    //4 PSHP-2
                    //4 PSHP-2
                }
                else
                {                                                       //4 PSHP-2
                    push(0);
                    push(-37);                                        //4 PSHP-2
                }                                                              //4 PSHP-2
                break;                                                         //4 PSHP
            //4 PSHP
            // CLOSE-FILE      ( fileid -- ior )                               //4 PSHP
            // Close the file identified by fileid.                            //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            //                                                                 //4 PSHP
            // Implementation details:                                         //4 PSHP
            // CLOSE-FILE removes from the files Hashtable the reference       //4 PSHP
            // to the WebForthFile keyed by the fileid value (see OPEN-FILE    //4 PSHP
            // above), so making the file eligible for garbage collection.     //4 PSHP
            //                                                                 //4 PSHP
            case CLOSEFILE:                                                    //4 PSHP
                a = pop();                                                     //4 PSHP
                push((files.remove(a) == null) ? -38 : 0);       //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // FILE-POSITION   ( fileid -- ud ior )                            //4 PSHP
            // Return current position of file identified by fileid.           //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case FILEPOSITION:                                                 //4 PSHP
                a = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(0);
                    push(-38);                               //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push(file.getPosition());
                    push(0);
                    push(0);                //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // FILE-SIZE       ( fileid -- ud ior )                            //4 PSHP
            // Return size of file identified by fileid.                       //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case FILESIZE:                                                     //4 PSHP
                a = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(0);
                    push(-38);                               //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push(file.getSize());
                    push(0);
                    push(0);                    //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // READ-FILE       (c-addr u1 fileid -- u2 ior )                   //4 PSHP
            // From the current position of the file identified by fileid      //4 PSHP-2
            // read the lesser of u1 bytes or until end of file is reached     //4 PSHP-2
            // into memory starting at c-addr. u2 is the number of bytes       //4 PSHP-2
            // read. Leave the file position at the next byte following the    //4 PSHP-2
            // last to be read.                                                //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case READFILE:                                                     //4 PSHP
                a = pop();                                                     //4 PSHP
                b = pop();                                                     //4 PSHP
                c = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(-38);                                        //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push(file.readFile(forthStack, c, b));
                    push(0);                     //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // READ-LINE       (c-addr u1 fileid -- u2 flag ior )              //4 PSHP
            // From the current position of the file identified by fileid      //4 PSHP-2
            // read the lesser of u1 characters or until end of line is        //4 PSHP-2
            // reached into memory starting at c-addr; u2 is the number of     //4 PSHP-2
            // characters read. If end of line is reached (u2 < u1) leave the  //4 PSHP-2
            // file position at the first character of the following line      //4 PSHP-2
            // otherwise (u2 = u1) leave it at the next character following    //4 PSHP-2
            // the last to be read. NB when a line containing exactly u1       //4 PSHP-2
            // characters is read u2 will equal u1 and the end of line will    //4 PSHP-2
            // not have been reached so the next call to READ-LINE will        //4 PSHP-2
            // return an empty string (u2 = 0). flag is false if the           //4 PSHP-2
            // operation fails (ior is non-zero) or if the operation is        //4 PSHP-2
            // initiated when file position is past the last character in the  //4 PSHP-2
            // file (ior is zero) otherwise flag is true.                      //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case READLINE:                                                     //4 PSHP
                a = pop();                                                     //4 PSHP
                b = pop();                                                     //4 PSHP
                c = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(FALSE);
                    push(-38);                           //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    if (file.getPosition() == file.getSize())
                    {                //4 PSHP
                        push(0);
                        push(FALSE);
                        push(0);                         //4 PSHP
                    }
                    else
                    {                                                   //4 PSHP
                        push(file.readLine(forthStack, c, b));
                        push(TRUE);
                        push(0);     //4 PSHP
                    }                                                          //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // REPOSITION-FILE ( ud fileid -- ior )                            //4 PSHP
            // Set position of file identified by fileid to ud.                //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            //                 -36 - ud is an invalid file position            //4 PSHP
            case REPOSITIONFILE:                                               //4 PSHP
                a = pop();                                                     //4 PSHP
                b = pop();                                                     //4 PSHP
                c = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(-38);                                                 //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push((file.setPosition(c) == c) ? 0 : -36);                //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // FILE-NAME       (c-addr u1 fileid -- u2 ior )                   //4 PSHP
            // Read up to u1 characters of name of file identified by fileid   //4 PSHP-2
            // into memory starting at c-addr. u2 is number of characters      //4 PSHP-2
            // read. if file name is longer than u1 characters then the        //4 PSHP-2
            // leading characters are truncated.                               //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case FILENAME:                                                     //4 PSHP
                a = pop();                                                     //4 PSHP
                b = pop();                                                     //4 PSHP
                c = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(-38);                                        //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push(file.readName(forthStack, c, b));
                    push(0);                     //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // LINE-NUMBER     ( fileid -- n ior )                             //4 PSHP
            // Return current line number of file identified by fileid or      //4 PSHP-2
            // -1 if it is unknown (it becomes unknown after the use of        //4 PSHP
            // READ-FILE or REPOSITION-FILE). Note that after READ-LINE has    //4 PSHP-2
            // read a complete line the current line number is one greater     //4 PSHP
            // than the line number of the line just read.                     //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case LINENUMBER:                                                   //4 PSHP
                a = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(0);
                    push(-38);                                        //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    push(file.getLineNumber());
                    push(0);                       //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP
            // SET-LINE-NUMBER ( n fileid -- ior )                             //4 PSHP
            // Set current line number of file identified by fileid to n.      //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP
            case SETLINENUMBER:                                                //4 PSHP
                a = pop();                                                     //4 PSHP
                b = pop();                                                     //4 PSHP
                file = files.get(a);                //4 PSHP
                if (file == null)
                {                                            //4 PSHP
                    push(-38);                                                 //4 PSHP
                }
                else
                {                                                       //4 PSHP
                    file.setLineNumber(b);                                     //4 PSHP
                    push(0);                                                   //4 PSHP
                }                                                              //4 PSHP
                break;                                                         //4 PSHP
            //4 PSHP-2
            // REPEAT-LINE     ( c-addr fileid -- u2 ior )                     //4 PSHP-2
            // Repeat read of the same characters from the file identified by  //4 PSHP-2
            // fileid into memory starting at c-addr that were read by the     //4 PSHP-2
            // most recent call to READ-LINE on this file. u2 is the number    //4 PSHP-2
            // of characters read.                                             //4 PSHP-2
            // ior values are:   0 - operation succeeded                       //4 PSHP-2
            //                 -38 - fileid not recognised                     //4 PSHP-2
            case REPEATLINE:                                                   //4 PSHP-2
                a = pop();                                                     //4 PSHP-2
                b = pop();                                                     //4 PSHP-2
                file = files.get(a);                //4 PSHP-2
                if (file == null)
                {                                            //4 PSHP-2
                    push(0);
                    push(-38);                                        //4 PSHP-2
                }
                else
                {                                                       //4 PSHP-2
                    push(file.repeatLine(forthStack, b));
                    push(0);                      //4 PSHP-2
                }                                                              //4 PSHP-2
                break;                                                         //4 PSHP-2
            //4 PSHP-2
            // KEY?      ( -- T | F )         //4 CMJ
            // Return true if input pending.  //4 CMJ
            case KEYQ:                        //4 CMJ
                yield();                      //4 CMJ
                if (keys() > 0)               //4 CMJ
                {                             //4 CMJ
                    push(TRUE);               //4 CMJ
                }                             //4 CMJ
                else                          //4 CMJ
                {                             //4 CMJ
                    push(FALSE);              //4 CMJ
                }                             //4 CMJ
                break;                        //4 CMJ

            // GET-XY ( -- x-coord y-coor )   //5 CMJ
            // Returns location of console cursor at column "x-coord", row "y-coord"
            case GETXY:                       //5 CMJ
                push(app.cursor() / forthStack[COLS]);   //5 CMJ
                push(app.cursor() % forthStack[COLS]);   //5 CMJ
                break;                        //5 CMJ

            //  NAME-SEARCH ( c-addr1 u wid -- 0 | xt c-addr2 )                //4 PSHP-3
            //  Find the definition identified by the string                   //4 PSHP-3
            //  c-addr1 u in the word list identified by wid.                  //4 PSHP-3
            //  If the definition is found return its execution                //4 PSHP-3
            //  token xt and its name address c-addr2, otherwise               //4 PSHP-3
            //  return zero. Name matching is case insensitive                 //4 PSHP-3
            //  if CAPS is zero, otherwise it is case sensitive.               //4 PSHP-3
            case NAMESEARCH:                                                   //4 PSHP-3
                a = pop();                                                     //4 PSHP-3
                b = pop();                                                     //4 PSHP-3
                c = pop();                                                     //4 PSHP-3
                n = 0;                                                         //4 PSHP-3
                if (forthStack[CAPS] == 0)
                {                                            //4 PSHP-3
                    char[] name = new char[b];                                 //4 PSHP-3
                    for (i = 0; i < b; i++)
                    {                                  //4 PSHP-3
                        name[i] = Character.toUpperCase((char) (forthStack[c + i]));     //4 PSHP-3
                    }                                                          //4 PSHP-3
                    caseInsensitiveSearch:
                    //4 PSHP-3
                    for (; forthStack[a] != 0; )
                    {                                     //4 PSHP-3
                        a = forthStack[a] - 1;                                          //4 PSHP-3
                        if (b == (forthStack[a + 1] & MASKK))
                        {                         //4 PSHP-3
                            for (i = 0; i < b; i++)
                            {                          //4 PSHP-3
                                if (Character.toUpperCase((char) (forthStack[a + 2 + i])) != //4 PSHP-3
                                        name[i])                                   //4 PSHP-3
                                {
                                    continue caseInsensitiveSearch;            //4 PSHP-3
                                }
                            }                                                  //4 PSHP-3
                            n = a + 1;                                         //4 PSHP-3
                            break;                       //4 PSHP-3
                        }                                                      //4 PSHP-3
                    }                                                          //4 PSHP-3
                }
                else
                {                                                       //4 PSHP-3
                    caseSensitiveSearch:
                    //4 PSHP-3
                    for (; forthStack[a] != 0; )
                    {                                     //4 PSHP-3
                        a = forthStack[a] - 1;                                          //4 PSHP-3
                        if (b == (forthStack[a + 1] & MASKK))
                        {                         //4 PSHP-3
                            for (i = 0; i < b; i++)
                            {                          //4 PSHP-3
                                if (forthStack[a + 2 + i] != forthStack[c + i])                  //4 PSHP-3
                                {
                                    continue caseSensitiveSearch;              //4 PSHP-3
                                }
                            }                                                  //4 PSHP-3
                            n = a + 1;                                         //4 PSHP-3
                            break;                         //4 PSHP-3
                        }                                                      //4 PSHP-3
                    }                                                          //4 PSHP-3
                }                                                              //4 PSHP-3
                if (n != 0)                                                    //4 PSHP-3
                {
                    push(forthStack[n - 2]);                                            //4 PSHP-3
                }
                push(n);                                                       //4 PSHP-3
                break;                                                         //4 PSHP-3

            // Catch any bad tokens
            default:
                String s = "*** Illegal Instruction. ***" + inst;
                throw new RuntimeException(s);
        }
    }

    //////////////////////////////////////////////////////////
    // Build eForth dictionary and high-level words
    //////////////////////////////////////////////////////////
    private void loadDictionary ()
    {
        UInit();            // Initialize User Variables
        sp = SPP;
        rp = RPP;

        // User variable words
        forthStack[CP] = CODEE;
        header("UP");
        code(doUSER);
        comma(0);
        header("'?KEY");
        code(doUSER);
        comma(TQKEY);
        header("?KEY");
        code(doUSER);
        comma(TQKEY);
        header("'EMIT");
        code(doUSER);
        comma(TEMIT);
        header("'EXPECT");
        code(doUSER);
        comma(TEXPE);
        header("'TAP");
        code(doUSER);
        comma(TTAP);
        header("'ECHO");
        code(doUSER);
        comma(TECHO);
        header("'PROMPT");
        code(doUSER);
        comma(TPROM);
        header("BASE");
        code(doUSER);
        comma(BASE);
        header("tmp");
        code(doUSER);
        comma(TEMP);
        header("SPAN");
        code(doUSER);
        comma(SPAN);
        header(">IN");
        code(doUSER);
        comma(INN);
        header("#TIB");
        code(doUSER);
        comma(NTIB);
        header("CSP");
        code(doUSER);
        comma(CSP);
        header("'EVAL");
        code(doUSER);
        comma(TEVAL);
        header("'NUMBER");
        code(doUSER);
        comma(TNUMB);
        header("HLD");
        code(doUSER);
        comma(HLD);
        header("HANDLER");
        code(doUSER);
        comma(HANDL);
        header("CONTEXT");
        code(doUSER);
        comma(CNTXT);
        header("CURRENT");
        code(doUSER);
        comma(CRRNT);
        header("CP");
        code(doUSER);
        comma(CP);
        header("NP");
        code(doUSER);
        comma(NP);
        header("LAST");
        code(doUSER);
        comma(LAST);
        header("BUFFER0");
        code(doUSER);
        comma(BUFFER0);
        header("BLK");
        code(doUSER);
        comma(BLK);
        header("BUF0");
        code(doUSER);
        comma(BUF0);
        header("UPD0");
        code(doUSER);
        comma(UPD0);
        header("LSTBUF");
        code(doUSER);
        comma(LSTBUF);
        header("BLOCKS");
        code(doUSER);
        comma(BLOCKS);
        header("CURBLK");
        code(doUSER);
        comma(CURBLK);
        header("CURPOS");
        code(doUSER);
        comma(CURPOS);
        header("TRACING");
        code(doUSER);
        comma(TRACING);
        header("SHOWING");
        code(doUSER);
        comma(SHOWING);
        header("STEPPING");
        code(doUSER);
        comma(STEPPING);
        header("CAPS");
        code(doUSER);
        comma(CAPS);
        header("FG-CLR");
        code(doUSER);
        comma(FG_CLR);
        header("BG-CLR");
        code(doUSER);
        comma(BG_CLR);
        header("BLACK");
        code(doCON);
        comma(0x00000000);
        header("WHITE");
        code(doCON);
        comma(0x00FFFFFF);  //1 PSHP
        header("RED");
        code(doCON);
        comma(0x00FF0000);
        header("GREEN");
        code(doCON);
        comma(0x0000FF00);
        header("BLUE");
        code(doCON);
        comma(0x000000FF);
        header("YELLOW");
        code(doCON);
        comma(0x00FFFF00);
        header("CYAN");
        code(doCON);
        comma(0x0000FFFF);
        header("MAGENTA");
        code(doCON);
        comma(0x00FF00FF);
//1 PSHP changed the following colours to browser safe values
        header("GRAY");
        code(doCON);
        comma(0x00999999);  //1 PSHP
        header("DARK-GRAY");
        code(doCON);
        comma(0x00666666);  //1 PSHP
        header("LIGHT-GRAY");
        code(doCON);
        comma(0x00CCCCCC);  //1 PSHP
        header("DARK-RED");
        code(doCON);
        comma(0x00660000);  //1 PSHP
        header("DARK-GREEN");
        code(doCON);
        comma(0x00006600);  //1 PSHP
        header("DARK-BLUE");
        code(doCON);
        comma(0x00000066);  //1 PSHP
        header("DARK-MAGENTA");
        code(doCON);
        comma(0x00660066);  //4 CMJ
        header("DARK-YELLOW");
        code(doCON);
        comma(0x00666600);  //4 CMJ
        header("DARK-CYAN");
        code(doCON);
        comma(0x00006666);  //4 CMJ
        header("STATE");
        code(doUSER);
        comma(STATE);
        header("LEAVES");
        code(doUSER);
        comma(LEAVES);
        header("DEF-BG-CLR");
        code(doUSER);
        comma(DEF_BG_CLR);
        header("FORTH-CLR");
        code(doUSER);
        comma(FORTH_CLR);
        header("CURSOR-CLR");
        code(doUSER);
        comma(CURSOR_CLR);
        header("ERROR-CLR");
        code(doUSER);
        comma(ERROR_CLR);
        header("USER-CLR");
        code(doUSER);
        comma(USER_CLR);
        header("ABORT$");
        code(doUSER);
        comma(ABORT$);       //1 PSHP
        header("NAME$");
        code(doUSER);
        comma(NAME$);        //1 PSHP
        header("DEEPBLK");
        code(doUSER);
        comma(DEEPBLK);      //1 PSHP
        header("DEEPPOS");
        code(doUSER);
        comma(DEEPPOS);      //1 PSHP
        header("SID");
        code(doUSER);
        comma(SID);          //4 PSHP-2
        header("DEEPSID");
        code(doUSER);
        comma(DEEPSID);      //4 PSHP-2
        header("DEEPLINE");
        code(doUSER);
        comma(DEEPLINE);     //4 PSHP-F
        header("ROWS");
        code(doUSER);
        comma(ROWS);         //5 CMJ
        header("COLS");
        code(doUSER);
        comma(COLS);         //5 CMJ

        // Primitive words
        primitive("BYE", BYE);
        primitive("?RX", QRX);
        primitive("TX!", TXSTO);
        primitive("!IO", STOIO);
        primitive("doLIT", DOLIT);
        compo();
        primitive("EXIT", EXIT);
        primitive("EXECUTE", EXECU);
        primitive("next", DONXT);
        primitive("?branch", QBRAN);
        compo();
        primitive("branch", BRAN);
        compo();
        primitive("!", STORE);
        primitive("@", AT);
        primitive("C!", CSTOR);
        primitive("C@", CAT);
        primitive("R>", RFROM);
        primitive(">R", TOR);
        primitive("R@", RAT);
        primitive("DROP", DROP);
        primitive("DUP", DUPP);
        primitive("SWAP", SWAP);
        primitive("OVER", OVER);
        primitive("ROT", ROT);
        primitive("0<", ZLESS);
        primitive("AND", ANDD);
        primitive("OR", ORR);
        primitive("XOR", XORR);
        primitive("UM+", UPLUS);
        primitive("UM/MOD", UMMOD);
        primitive("UM*", UMSTA);
        primitive("1+", INC);
        primitive("1-", DEC);
        primitive("DEPTH", DEPTH);
        primitive("PICK", PICK);
        primitive("block@", BLOCKRD);
        primitive("block!", BLOCKWR);
        primitive("screen-refresh", SCRNREF);
        primitive("+LOOP", PLOOP);
        primitive("VERBOSE", VERBO);
        primitive("QUIET", QUIET);
        primitive("AT-XY", ATXY);
        primitive("PAGE", PAGE);
        primitive("MS", MS);
        primitive("(;)", PSEMI);      //1 PSHP
        primitive("doCON", doCON);    //1 PSHP
        primitive("doUSER", doUSER);  //1 PSHP
        primitive("0=", ZEQUAL);      //1 PSHP-5
        primitive("OPEN-FILE", OPENFILE);                                          //4 PSHP
        primitive("CLOSE-FILE", CLOSEFILE);                                        //4 PSHP
        primitive("FILE-POSITION", FILEPOSITION);                                  //4 PSHP
        primitive("FILE-SIZE", FILESIZE);                                          //4 PSHP
        primitive("READ-FILE", READFILE);                                          //4 PSHP
        primitive("READ-LINE", READLINE);                                          //4 PSHP
        primitive("REPOSITION-FILE", REPOSITIONFILE);                              //4 PSHP
        primitive("FILE-NAME", FILENAME);                                          //4 PSHP
        primitive("LINE-NUMBER", LINENUMBER);                                      //4 PSHP
        primitive("SET-LINE-NUMBER", SETLINENUMBER);                               //4 PSHP
        primitive("REPEAT-LINE", REPEATLINE);                                      //4 PSHP-2
        primitive("KEY?", KEYQ);                                                   //4 CMJ
        primitive("GET-XY", GETXY);                                                //5 CMJ
        primitive("NAME-SEARCH", NAMESEARCH);                                      //5 CMJ

        // High-level R@ needs to reach one level farther to get
        // "top" of r stack
        header("R@");
        code(RFROM);
        code(RAT);
        code(SWAP);
        code(TOR);
        code(PSEMI);

        //  doVAR   ( -- )
        //  Run time action of VARIABLE
        header("doVAR");
        code(RFROM);
        code(PSEMI);
        compo();

        //  doVOC   ( -- )
        //  Run time action of VOCABULARY's.
        header("doVOC");
        code(RFROM);
        call("CONTEXT");
        code(STORE);
        code(PSEMI);
        compo();

        //  FORTH   ( -- )
        //  Make FORTH the context vocabulary.
        header("FORTH");
        call("doVOC");
        comma(0);
        comma(0);

        //  ?DUP    ( w -- w w | 0 )
        //  Dup tos if its is not zero.
        header("?DUP");
        code(DUPP);
        compIF();
        code(DUPP);
        compTHEN();
        code(PSEMI);

        //  2DROP   ( w w -- )
        //  Discard two items on stack.
        header("2DROP");
        code(DROP);
        code(DROP);
        code(PSEMI);

        //  2DUP    ( w1 w2 -- w1 w2 w1 w2 )
        //  Duplicate top two items.
        header("2DUP");
        code(OVER);
        code(OVER);
        code(PSEMI);

        //  +       ( w w -- sum )
        //  Add top two items.
        header("+");
        code(UPLUS);
        code(DROP);
        code(PSEMI);

        //  D+      ( d d -- d )
        //  Double addition, as an example using UM+.
        header("D+");
        code(TOR);
        code(SWAP);
        code(TOR);
        code(UPLUS);
        code(RFROM);
        code(RFROM);
        call("+");
        call("+");
        code(PSEMI);

        //  NOT     ( w -- w )  // INVER
        //  One's complement of tos.
        header("NOT");
        literal(-1);
        code(XORR);
        code(PSEMI);

        //  NEGATE  ( n -- -n )
        //  Two's complement of tos.
        header("NEGATE");
        call("NOT");
        code(INC);
        code(PSEMI);

        //  DNEGATE ( d -- -d )
        //  Two's complement of top double.
        header("DNEGATE");
        call("NOT");
        code(TOR);
        call("NOT");
        literal(1);
        code(UPLUS);
        code(RFROM);
        call("+");
        code(PSEMI);

        //  -       ( n1 n2 -- n1-n2 )
        //  Subtraction.
        header("-");
        call("NEGATE");
        call("+");
        code(PSEMI);


        //  ABS     ( n -- n )
        //  Return the absolute value of n.
        header("ABS");
        code(DUPP);
        code(ZLESS);
        compIF();
        call("NEGATE");
        compTHEN();
        code(PSEMI);

        //  FALSE   ( -- t )   // FALSE						//6 CJ
        //  Return 0, the false flag.						//6 CJ
        header("FALSE");
        literal(FALSE);
        code(PSEMI);    //6 CJ

        //  TRUE    ( -- t )   // TRUE						//6 CJ
        //  Return -1, the true flag.						//6 CJ
        header("TRUE");
        literal(TRUE);
        code(PSEMI);        //6 CJ

        //  =       ( w w -- t )
//1 PSHP-5        header("=");        code(XORR);
//1 PSHP-5                            compIF();   literal(FALSE); code(EXIT);
//1 PSHP-5                            compTHEN(); literal(TRUE);  code(PSEMI);
        header("=");
        code(XORR);
        code(ZEQUAL);
        code(PSEMI);     //1 PSHP-5

        //  U<      ( u u -- t )
        //  Unsigned compare of top two items.
        header("U<");
        call("2DUP");
        code(XORR);
        code(ZLESS);
        compIF();
        code(SWAP);
        code(DROP);
        code(ZLESS);
        code(EXIT);
        compTHEN();
        call("-");
        code(ZLESS);
        code(PSEMI);

        //  <       ( n1 n2 -- t )
        //  Signed compare of top two items.
        header("<");
        call("2DUP");
        code(XORR);
        code(ZLESS);
        compIF();
        code(DROP);
        code(ZLESS);
        code(EXIT);
        compTHEN();
        call("-");
        code(ZLESS);
        code(PSEMI);

        //  MAX     ( n n -- n )
        //  Return the greater of two top stack items.
        header("MAX");
        call("2DUP");
        call("<");
        compIF();
        code(SWAP);
        compTHEN();
        code(DROP);
        code(PSEMI);

        //  MIN     ( n n -- n )
        //  Return the smaller of top two stack items.
        header("MIN");
        call("2DUP");
        code(SWAP);
        call("<");
        compIF();
        code(SWAP);
        compTHEN();
        code(DROP);
        code(PSEMI);

        //  WITHIN  ( u ul uh -- t )
        //  Return true if u is within the range of ul and uh.
        header("WITHIN");
        code(OVER);
        call("-");
        code(TOR); //ul <= u < uh
        call("-");
        code(RFROM);
        call("U<");
        code(PSEMI);

        //  Divide

        //  M/MOD   ( d n -- r q )
        //  Signed floored divide of double by single. Return mod and quotient.
        header("M/MOD");
        code(DUPP);
        code(ZLESS);
        code(DUPP);
        code(TOR);
        compIF();
        call("NEGATE");
        code(TOR);
        call("DNEGATE");
        code(RFROM);
        compTHEN();
        code(TOR);
        code(DUPP);
        code(ZLESS);
        compIF();
        code(RAT);
        call("+");
        compTHEN();
        code(RFROM);
        code(UMMOD);
        code(RFROM);
        compIF();
        code(SWAP);
        call("NEGATE");
        code(SWAP);
        compTHEN();
        code(PSEMI);


        //  /MOD    ( n n -- r q )
        //  Signed divide. Return mod and quotient.
        header("/MOD");
        code(OVER);
        code(ZLESS);
        code(SWAP);
        call("M/MOD");
        code(PSEMI);

        //  MOD     ( n n -- r )
        //  Signed divide. Return mod only.
        header("MOD");
        call("/MOD");
        code(DROP);
        code(PSEMI);

        //  /       ( n n -- q )
        //  Signed divide. Return quotient only.
        header("/");
        call("/MOD");
        code(SWAP);
        code(DROP);
        code(PSEMI);


        //  Multiply

        //  *       ( n n -- n )  // STAR
        //  Signed multiply. Return single product.
        header("*");
        call("UM*");
        code(DROP);
        code(PSEMI);

        //  M*      ( n n -- d )  // MSTAR
        //  Signed multiply. Return double product.
        header("M*");
        call("2DUP");
        code(XORR);
        code(ZLESS);
        code(TOR);
        call("ABS");
        code(SWAP);
        call("ABS");
        call("UM*");
        code(RFROM);
        compIF();
        call("DNEGATE");
        compTHEN();
        code(PSEMI);

        //  * / MOD ( n1 n2 n3 -- r q )  // SSMOD
        //  Multiply n1 and n2, then divide by n3. Return mod and quotient.
        header("*/MOD");
        code(TOR);
        call("M*");
        code(RFROM);
        call("M/MOD");
        code(PSEMI);

        //  * /     ( n1 n2 n3 -- q )  // STASL
        //  Multiply n1 by n2, then divide by n3. Return quotient only.
        header("*/");
        call("*/MOD");
        code(SWAP);
        code(DROP);
        code(PSEMI);

        //  CELL+   ( a -- a )  // CELLP
        //  Add cell size in address units to address.
        header("CELL+");
        code(INC);
        code(PSEMI);  //Address unit = cell

        //  CELL-   ( a -- a )  // CELLM
        //  Subtract cell size in address units from address.
        header("CELL-");
        code(DEC);
        code(PSEMI);  //Address unit = cell

        //  CELLS   ( n -- n )
        //  Multiply tos by cell size in addrss units.
        header("CELLS");
        code(PSEMI);
        immed();    //Address unit = cell

        //  ALIGNED ( b -- a )
        //  Align address to the cell boundary.
        header("ALIGNED");
        code(PSEMI);
        immed();    //Address unit = cell

        //  BL      ( -- 32 )   // BLANK
        //  Return 32, the blank character.
        header("BL");
        literal(32);
        code(PSEMI);

        //  >CHAR   ( c -- c )  // TCHAR
        //  Filter non-printing characters.
        header(">CHAR");
        literal(0x7F);
        code(ANDD);  //mask msb
        code(DUPP);
        literal(127);
        call("BL");
        call("WITHIN");     //check for printable
        compIF();
        code(DROP);
        literal('_');       //replace non-printables
        compTHEN();
        code(PSEMI);

        // Memory access

        //  +!      ( n a -- )  // PSTOR
        //  Add n to the contents at address a.
        header("+!");
        code(SWAP);
        code(OVER);
        code(AT);
        call("+");
        code(SWAP);
        code(STORE);
        code(PSEMI);

        //  2!      ( d a -- )  // DSTOR
        //  Store the double integer to address a.
        header("2!");
        code(SWAP);
        code(OVER);
        code(STORE);
        call("CELL+");
        code(STORE);
        code(PSEMI);

        //  2@      ( a -- d )  // DAT
        //  Fetch double integer from address a.
        header("2@");
        code(DUPP);
        call("CELL+");
        code(AT);
        code(SWAP);
        code(AT);
        code(PSEMI);

        //  COUNT   ( b -- b +n )
        //  Return count byte of a string and add 1 to byte address.
        header("COUNT");
        code(DUPP);
        code(INC);
        code(SWAP);
        code(CAT);
        code(PSEMI);

        //  HERE    ( -- a )
        //  Return the top of the code dictionary.
        header("HERE");
        call("CP");
        code(AT);
        code(PSEMI);

        //  PAD     ( -- a )
        //  Return the address of a temporary buffer.
//1 PSHP-5        header("PAD");      call("HERE"); literal(80);
        header("PAD");
        call("HERE");
        literal(PAD_OFFSET);         //1 PSHP-5
        call("+");
        code(PSEMI);

        //  TIB     ( -- a )
        //  Return the address of the terminal input buffer.
        header("TIB");
        call("#TIB");
        call("CELL+");
        code(AT);
        code(PSEMI);

        //  @EXECUTE    ( a -- )  // ATEXE
        //  Execute vector stored in address a.
        header("@EXECUTE");
        code(AT);
        call("?DUP"); //?address or zero
        compIF();
        code(EXECU);        //execute if non-zero
        compTHEN();
        code(PSEMI); //do nothing if zero

        //  CMOVE   ( b1 b2 u -- )
        //  Copy u bytes from b1 to b2.
        header("CMOVE");
        code(TOR);
        compAFT(); // branch to CMOV2
        mark(); // start of FOR loop
        code(TOR);
        code(DUPP);
        code(CAT);
        code(RAT);
        code(CSTOR);
        code(INC);
        code(RFROM);
        code(INC);
        /*CMOV2:*/
        swap();
        compTHEN();
        compNEXT();
        call("2DROP");
        code(PSEMI);

        //  CMOVE>  ( b1 b2 u -- )
        //  Copy u bytes from b1 to b2, starting at ends and
        //  working to front.
        header("CMOVE>");
        code(DEC);
        code(TOR);
        code(RAT);
        call("+");              // -- b1 b2'
        code(SWAP);
        code(RAT);
        call("+");              // -- b2' b1'
        code(SWAP);
        mark(); // start of FOR loop
        code(TOR);
        code(DUPP);
        code(CAT);
        code(RAT);
        code(CSTOR);
        code(DEC);
        code(RFROM);
        code(DEC);
        compNEXT();
        call("2DROP");
        code(PSEMI);

        //  FILL    ( b u c -- )
        //  Fill u bytes of character c to area beginning at b.
        header("FILL");
        code(SWAP);
        code(TOR);
        code(SWAP);
        compAFT(); // branch to FILL2
        mark();    // start of FOR loop
        /*FILL1:*/
        call("2DUP");
        code(CSTOR);
        code(INC);
        /*FILL2:*/
        swap();
        compTHEN();
        compNEXT();
        call("2DROP");
        code(PSEMI);

        //  -TRAILING   ( b u -- b u )  // DTRAI
        //  Adjust the count to eliminate trailing white space.
        header("-TRAILING");
        code(TOR);
        compAFT(); // branch to DTRA2
        mark();  // start of FOR loop
        /*DTRA1:*/
        call("BL");
        code(OVER);
        code(RAT);
        call("+");
        code(CAT);
        call("<");
        compIF();
        code(RFROM);
        code(INC);
        code(EXIT);  //adjusted count
        compTHEN();
        swap();
        compTHEN();
        /*DTRA2:*/
        compNEXT();
        literal(0);
        code(PSEMI); // count=0

        //  PACK$   ( b u a -- a )  // PACKS
        //  Build a counted string with u characters from b. Null fill.
        header("PACK$");
        code(DUPP);
        code(TOR);     // save a
        call("2DUP");
        code(STORE); // store count
        code(INC);
        code(SWAP);     // ( -- b a+1 u )
        call("CMOVE");
        code(RFROM);
        code(PSEMI);


        //  Numeric output, single precision

        //  DIGIT   ( u -- c )
        //  Convert digit u to a character.
        header("DIGIT");
        literal(9);
        code(OVER);
        call("<");
        literal(7);
        code(ANDD);
        call("+");
        literal('0');
        call("+");
        code(PSEMI);

// This is no longer used in Preston's ANS num. output code:
//        //  EXTRACT ( n base -- n c )  // EXTRC
//        //  Extract the least significant digit from n.
//        header("EXTRACT");  literal(0); code(SWAP); code(UMMOD);
//                            code(SWAP); call("DIGIT"); code(PSEMI);

        //  <#      ( -- )  // BDIGS
        //  Initiate the numeric output process.
        header("<#");
        call("PAD");
        call("HLD");
        code(STORE);
        code(PSEMI);

        //  HOLD    ( c -- )
        //  Insert a character into the numeric output string.
        header("HOLD");
        call("HLD");
        code(AT);
        code(DEC);
        code(DUPP);
        call("HLD");
        code(STORE);
        code(CSTOR);
        code(PSEMI);
// Preston's ANS Fixes:
//        //  #       ( u -- u )  // DIG
//        //  Extract one digit from u and append it to output string.
//        header("#");        call("BASE"); code(AT); call("EXTRACT");
//                            call("HOLD"); code(PSEMI);

//        //  #S      ( u -- 0 )  // DIGS
//        //  Convert u until all digits are added to the output string.
//        header("#S");       compBEGIN();
//                               call("#"); code(DUPP);
//                                compWHILE();
//                            compREPEAT(); code(PSEMI);

        //  #       ( ud1 -- ud2 )  // DIG
        //  Extract one digit from ud1 and append it to output string.
        header("#");
        literal(0);
        call("BASE");
        code(AT);
        code(UMMOD);
        code(TOR);
        call("BASE");
        code(AT);
        code(UMMOD);
        code(RFROM);
        code(ROT);
        call("DIGIT");
        call("HOLD");
        code(PSEMI);

        //  #S      ( ud1 -- ud2 )  // DIGS
        //  Convert ud until all digits are added to the output string
        //  and ud2 equals zero.
        header("#S");
        compBEGIN();
        call("#");
        call("2DUP");
        code(ORR);
        compWHILE();
        compREPEAT();
        code(PSEMI);


        //  SIGN    ( n -- )
        //  Add a minus sign to the numeric output string.
        header("SIGN");
        code(ZLESS);
        compIF();
        literal('-');
        call("HOLD");
        compTHEN();
        code(PSEMI);

// Preston's ANS Fixes:
//        //  #>      ( w -- b u )  // EDIGS
//        //  Prepare the output string to be TYPE'd.
//        header("#>");       code(DROP); call("HLD"); code(AT);
//                            call("PAD"); code(OVER);
//                            call("-"); code(PSEMI);

        //  #>      ( xd -- c-addr u )  // EDIGS
        //  Prepare the output string to be TYPE'd.
        header("#>");
        call("2DROP");
        call("HLD");
        code(AT);
        call("PAD");
        code(OVER);
        call("-");
        code(PSEMI);


        //  str     ( n -- b u )  // STR
        //  Convert a signed integer to a numeric string.
        header("str");
        code(DUPP);
        code(TOR);
        call("ABS");
        literal(0); // Preston's ANS Fix
        call("<#");
        call("#S");
        code(RFROM);
        call("SIGN");
        call("#>");
        code(PSEMI);

        //  HEX     ( -- )
        //  Use radix 16 as base for numeric conversions.
        header("HEX");
        literal(16);
        call("BASE");
        code(STORE);
        code(PSEMI);

        //  DECIMAL ( -- )  // DECIM
        //  Use radix 10 as base for numeric conversions.
        header("DECIMAL");
        literal(10);
        call("BASE");
        code(STORE);
        code(PSEMI);

        //  Numeric input, single precision

        //  DIGIT?  ( c base -- u t )  // DIGTQ
        //  Convert a character to its numeric value.
        //  A flag indicates success.
        header("DIGIT?");
        code(TOR);
        literal('0');
        call("-");
        literal(9);
        code(OVER);
        call("<");
        compIF();
        literal(7);
        call("-");
        code(DUPP);
        literal(10);
        call("<");
        code(ORR);
        compTHEN();
        code(DUPP);
        code(RFROM);
        call("U<");
        code(PSEMI);

        //  NUMBER? ( a -- n a | a F )  // NUMBQ
        //  Convert a number string to integer. Push a flag on tos.
        //  Note: "TRUE" flag is actualy the inital address.
        header("NUMBER?");
        call("BASE");
        code(AT);
        code(TOR);
        literal(0);
        code(OVER);
        call("COUNT");
        code(OVER);
        code(CAT);
        literal('$');
        call("=");
        compIF();
        call("HEX");
        code(SWAP);
        code(INC);
        code(SWAP);
        code(DEC);
        compTHEN();
        code(OVER);
        code(CAT);
        literal('-');
        call("=");
        code(TOR);
        code(SWAP);
        code(RAT);
        call("-");
        code(SWAP);
        code(RAT);
        call("+");
        call("?DUP");
        compIF(); //to NUMQ6
        code(DEC);
        compFOR();
        code(DUPP);
        code(TOR);
        code(CAT);
        call("BASE");
        code(AT);
        call("DIGIT?");
        compWHILE(); //to NUMQ4
        code(SWAP);
        call("BASE");
        code(AT);
        call("*");
        call("+");
        code(RFROM);
        code(INC);
        compNEXT();
        code(RAT);
        code(SWAP);
        code(DROP);
        compIF();
        call("NEGATE");
        compTHEN();
        code(SWAP);
        compELSE(); //to NUMQ5
        /*NUMQ4:*/
        code(RFROM);
        code(RFROM);
        call("2DROP");
        call("2DROP");
        literal(0);
        compTHEN();
        /*NUMQ5:*/
        code(DUPP);
        /*NUMQ6:*/
        compTHEN();
        code(RFROM);
        call("2DROP");
        code(RFROM);
        call("BASE");
        code(STORE);
        code(PSEMI);


        //  Basic I/O

        //  ?KEY    ( -- c T | F )  // QKEY
        //  Return input character and true, or a false if no input (from eForth)
        //  Note: The eForth ?KEY is not ANS and is used by KEY, NUF? and CONSOLE
        header("?KEY");
        call("'?KEY");
        call("@EXECUTE");
        code(PSEMI);

        //  KEY     ( -- c )  BEGIN ?KEY NOT WHILE -2 SCRNREF REPEAT ;
        //  Wait for and return an input character.
        header("KEY");
// CMJ If a key is already waiting in the buffer, don't repaint the screen
        compBEGIN();
        call("?KEY");
        call("NOT");
        compWHILE();
// CMJ Added line to force an update of the display and a blink of cursor
        literal(-2);
        code(SCRNREF); // force a repaint of screen
        compREPEAT();
        code(PSEMI);

        //  EMIT    ( c -- )
        //  Send a character to the output device.
        header("EMIT");
        call("'EMIT");
        call("@EXECUTE");
        code(PSEMI);

        //  NUF?    ( -- t )  // NUFQ
        //  Return false if no input, else pause and if CR return true.
        header("NUF?");
        call("?KEY");
        code(DUPP);
        compIF();
        call("2DROP");
        call("KEY");
        literal('\n');
        call("=");
        compTHEN();
        code(PSEMI);

        //  PACE    ( -- )
        //  Send a pace character for the file downloading process.
        header("PACE");
        literal(11);
        call("EMIT");
        code(PSEMI);

        //  SPACE   ( -- )
        //  Send the blank character to the output device.
        header("SPACE");
        call("BL");
        call("EMIT");
        code(PSEMI);

        //  SPACES  ( +n -- )  // SPACS
        //  Send n spaces to the output device.
        header("SPACES");
        literal(0);
        call("MAX");
        code(TOR);
        compAFT();
        mark(); // Top of FOR loop
        call("SPACE");
        swap();
        compTHEN();
        compNEXT();
        code(PSEMI);

        //  TYPE    ( b u -- )  // TYPEE
        //  Output u characters from b.
        header("TYPE");
        code(TOR);
        compAFT();
        mark(); // Top of FOR loop
        code(DUPP);
        code(CAT);
        call("EMIT");
        code(INC);
        swap();
        compTHEN();
        compNEXT();
        code(DROP);
        code(PSEMI);

        //  CR      ( -- )
        //  Output a carriage return and a line feed.
        header("CR");
        literal('\n');
        call("EMIT");
        code(PSEMI);

        //  do$     ( -- a )  // DOSTR
        //  Return the address of a compiled string.
        header("do$");
        code(RFROM);
        code(RAT);
        code(RFROM);
        call("COUNT");
        call("+"); // call("ALIGNED");
        code(TOR);
        code(SWAP);
        code(TOR);
        code(PSEMI);
        compo();

        //  $"|		( -- a )  // STRQP
        //  Run time routine compiled by $". Return address of a compiled string.
        //  $COLON  COMPO+3,'$"|',STRQP
        header("$\"|");
        call("do$"); //force a call to do$
        code(PSEMI);
        compo();

        //  ."|		( -- )  // DOTQP
        //  Run time routine of ." . Output a compiled string.
        header(".\"|");
        call("do$");
        call("COUNT");
        call("TYPE");
        code(PSEMI);
        compo();

        //  .R      ( n +n -- )  // DOTR
        //  Display an integer in a field of n columns, right justified.
        header(".R");
        code(TOR);
        call("str");
        code(RFROM);
        code(OVER);
        call("-");
        call("SPACES");
        call("TYPE");
        code(PSEMI);

        //  U.R     ( u +n -- )  // UDOTR
        //  Display an unsigned integer in n column, right justified.
        header("U.R");
        code(TOR);
        literal(0); // Preston's ANS Fix
        call("<#");
        call("#S");
        call("#>");
        code(RFROM);
        code(OVER);
        call("-");
        call("SPACES");
        call("TYPE");
        code(PSEMI);

        //  U.      ( u -- )  // UDOT
        //  Display an unsigned integer in free format.
        header("U.");
        literal(0); // Preston's ANS Fix
        call("<#");
        call("#S");
        call("#>");
        call("SPACE");
        call("TYPE");
        code(PSEMI);

        //  .       ( w -- )  // DOT
        //  Display an integer in free format, preceeded by a space.
        header(".");
        call("BASE");
        code(AT);
        literal(10);
        code(XORR);
        compIF();       // ?decimal
        call("U.");     // no, display unsigned
        code(EXIT);
        compTHEN();
        call("str");
        call("SPACE");
        call("TYPE");       // yes, display signed
        code(PSEMI);

        //  ?       ( a -- )  // QUEST
        //  Display the contents in a memory cell.
        header("?");
        code(AT);
        call(".");
        code(PSEMI);

        //  Parsing
        //  parse   ( b u c -- b u delta ; <string> )  // PARS
        //  Scan string delimited by c. Return found string and its offset.
        header("parse");
        call("tmp");
        code(STORE);
        code(OVER);
        code(TOR);
        code(DUPP);
        compIF();
        code(DEC);
        call("tmp");
        code(AT);
        call("BL");
        call("=");
        compIF();
        compFOR();
        call("BL");
        code(OVER);
        code(CAT);  //skip leading blanks ONLY
        call("-");
        code(ZLESS);
        call("NOT");
        compWHILE();
        code(INC);
        compNEXT();
        code(RFROM);
        code(DROP);
        literal(0);
        code(DUPP);
        code(EXIT);
        compTHEN();
        code(RFROM);
        compTHEN();
        code(OVER);
        code(SWAP);
        compFOR();
        call("tmp");
        code(AT);
        code(OVER);
        code(CAT);
        call("-");   //scan for delimiter
        call("tmp");
        code(AT);
        call("BL");
        call("=");
        compIF();
        code(ZLESS);
        compTHEN();
        compWHILE();
        code(INC);
        compNEXT();
        code(DUPP);
        code(TOR);
        compELSE();
        code(RFROM);
        code(DROP);
        code(DUPP);
        code(INC);
        code(TOR);
        compTHEN();
        code(OVER);
        call("-");
        code(RFROM);
        code(RFROM);
        call("-");
        code(EXIT);
        compTHEN();
        code(OVER);
        code(RFROM);
        call("-");
        code(PSEMI);

        //  PARSE   ( c -- b u ; <string> )
        //  Scan input stream and return counted string delimited by c.
        header("PARSE");
        code(TOR);
        call("TIB");
        call(">IN");
        code(AT);
        call("+"); // current input buffer pointer
        call("#TIB");
        code(AT);
        call(">IN");
        code(AT);
        call("-");  // remaining count
        code(RFROM);
        call("parse");
        call(">IN");
        call("+!");
        code(PSEMI);

        //  .(      ( -- )  // DOTPR , IMMED
        //  Output following string up to next ) .
        header(".(");
        literal(')');
        call("PARSE");
        call("TYPE");
        code(PSEMI);
        immed();

        //  (       ( -- )  // PAREN , IMMED
        //  Ignore following string up to next ) . A comment.
        header("(");
        literal(')');
        call("PARSE");
        call("2DROP");
        code(PSEMI);
        immed();

        //  \       ( -- )  // BKSLA , IMMED
        //  Ignore following text till the end of line.
//1 PSHP-5        header("\\");		literal(80); call("#TIB"); code(AT);
//4 PSHP-2        header("\\");		literal(TIB_SIZE); call("#TIB"); code(AT);   //1 PSHP-5
//4 PSHP-2                            call("<");
        header("\\");
        call("BLK");
        code(AT);                                 //4 PSHP-2
        compIF();
        call(">IN");
        code(AT);
        literal(64);
        call("+");
        literal(0xFFFFFFC0);
        code(ANDD);
        compELSE();
        call("#TIB");
        code(AT);
        compTHEN();
        call(">IN");
        code(STORE);
        code(PSEMI);
        immed();

        //  <!      ( -- )  // BKSLA , IMMED
        //  Same as \. Provided to allow HTML files to contain NoWebForth source.
        header("<!");
        call("\\");
        code(PSEMI);
        immed();

        //  The following words are no-ops, provided to allow HTML markup to be interleaved in-line in source files.
        //  <B>     ( -- )  // BOLD , IMMED
        header("<B>");
        code(PSEMI);
        immed();

        //  </B>    ( -- )  // UNBOLD , IMMED
        header("</B>");
        code(PSEMI);
        immed();

        //  <I>     ( -- )  // BOLD , IMMED
        header("<I>");
        code(PSEMI);
        immed();

        //  </I>    ( -- )  // UNBOLD , IMMED
        header("</I>");
        code(PSEMI);
        immed();

//1 CMJ Redundant
//1 CMJ       //  CHAR    ( -- c )
//1 CMJ       //  Parse next word and return its first character.
//1 CMJ       header("CHAR'");    call("BL"); call("PARSE"); code(DROP);
//1 CMJ                            code(CAT); code(PSEMI);

        //  TOKEN   ( -- a ; <string> )
        //  Parse a word from input stream and copy it to name dictionary.
        header("TOKEN");
        call("BL");
        call("PARSE");
        literal(31);
        call("MIN");
        call("NP");
        code(AT);
        code(OVER);
        call("-");
        call("CELL-");
        call("PACK$");
        code(PSEMI);

        //  WORD    ( c -- a ; <string> )
        //  Parse a word from input stream and copy it to code dictionary.
        header("WORD");
        call("PARSE");
        call("HERE");
        call("PACK$");
        code(PSEMI);


        //  Dictionary search

        // A header in name space is:
        //   1 cell for Code Address (CA), given my here()
        //   1 cell for Link (Name Address (NA) of previous name)
        //   1 cell "Name Address" for Name Length (in chars)
        //   n cells, one for each char in name

        //  NAME>   ( na -- ca ) // NAMET
        //  Return a code address given a name address.
        header("NAME>");    /* call("CELL-"); call("CELL-"); */
        // Optimization: DEC equivalent to CELL- but faster
        code(DEC);
        code(DEC);
        code(AT);
        code(PSEMI);

        //  ISLOWER?   ( c -- f)
        //  returns TRUE if character is a lower case letter ('a' to 'z')
        header("ISLOWER?");
        literal('a' - 1);
        code(OVER);   // c 96 c
        // inlined "-" code starts...
        literal(-1);
        code(XORR);
        code(INC);
        code(UPLUS);
        code(DROP); // inlined "-" ends
        code(ZLESS);
        code(SWAP);
        literal('z' + 1);
        // inlined "-" code starts...
        literal(-1);
        code(XORR);
        code(INC);
        code(UPLUS);
        code(DROP); // inlined "-" ends
        code(ZLESS);
        code(ANDD);
        code(PSEMI);

        //  UCASE      ( c -- c')
        //  replaces the character on stack with upper case version
        //  if and only if it is a lower case letter ('a' to 'z')
        header("UCASE");
        code(DUPP);                          // -- c c
        call("ISLOWER?");
        compIF();          // -- a c
        literal('a' - 'A');
        code(XORR);  // -- a c'
        compTHEN();
        code(PSEMI);

        //  @UCASE!      ( a -- )
        //  replaces the character at addr. a with upper case version
        //  if and only if it is a lower case letter ('a' to 'z')
        header("@UCASE!");
        code(DUPP);
        code(CAT);               // -- a c
        call("UCASE");
        code(SWAP);           // -- c' a
        code(CSTOR);
        code(PSEMI);

        //  UCASE?      ( c -- c')
        //  replaces the character c with upper case version
        //  if and only if it is a lower case letter ('a' to 'z')
        //  and if the CAPS variable holds a zero
        header("UCASE?");
        call("CAPS");
        code(AT);
        call("NOT");
        compIF();
        call("UCASE");
        compTHEN();
        code(PSEMI);

        //  TOUPPER      ( c$ -- )
        //  replaces all lowercase letters in the counted string with
        //  upper case equivalents
        header("TOUPPER");
        call("COUNT");
        code(TOR);
        compBEGIN();
        code(RFROM);
        code(DEC);     // decr. count
        code(DUPP);
        code(TOR);
        code(ZLESS);
        literal(-1);
        code(XORR); // > 0?
        compWHILE();
        code(DUPP);
        call("@UCASE!");
        code(INC);
        compREPEAT();
        code(RFROM);
        code(DROP);
        code(DROP);
        code(PSEMI);

        //  SAME?   ( a a u -- a a f \ -0+ )  // SAMEQ
        //  Compare u cells in two strings. Return 0 if identical.
        header("SAME?");
        code(TOR);
        compAFT();
        mark();
        code(OVER);
        code(RAT); // call("CELLS");  <-- optimize
        call("+");
        code(AT);
        call("UCASE?");
        code(OVER);
        code(RAT); // call("CELLS");  <-- optimize
        call("+");
        code(AT);
        call("UCASE?");
        call("-");
        call("?DUP");
        compIF();
        code(RFROM);
        code(DROP);
        code(EXIT);     // strings not equal
        compTHEN();
        swap();
        compTHEN();
        compNEXT();
        literal(0);
        code(PSEMI); // strings equal

/// Replaced by a primitive for faster speed
//5 PSP        //  NAME-SEARCH ( c-addr1 u wid -- 0 | xt c-addr2 )          //1 PSHP-5
//5 PSP        //  Find the definition identified by the string
//5 PSP        //  c-addr1 u in the word list identified by wid.
//5 PSP        //  If the definition is not found return zero
//5 PSP        //  otherwise return its execution token xt and
//5 PSP        //  its name address c-addr2.
//5 PSP        header("NAME-SEARCH");
//5 PSP                            code(SWAP); code(TOR);
//5 PSP                            compBEGIN();
//5 PSP                                code(AT); code(DUPP);
//5 PSP                                compWHILE();
//5 PSP                                    code(DUPP); code(AT);
//5 PSP                                    literal(MASKK); code(ANDD);
//5 PSP                                    code(RAT); call("=");
//5 PSP                                    compIF();
//5 PSP                                        code(INC); code(RAT);
//5 PSP                                        call("SAME?"); code(ZEQUAL);
//5 PSP                                        compIF();
//5 PSP                                            code(SWAP); code(RFROM); call("2DROP");
//5 PSP                                            code(DEC); code(DUPP); call("NAME>");
//5 PSP                                            code(SWAP); code(EXIT);
//5 PSP                                        compTHEN();
//5 PSP                                        code(DEC);
//5 PSP                                    compTHEN();
//5 PSP                                    code(DEC);
//5 PSP                            compREPEAT();
//5 PSP                            code(SWAP); code(RFROM); call("2DROP");
//5 PSP                            code(PSEMI);

        //  IMMEDIATE?  ( 0 | c-addr -- 0 | 1 | -1 )                 //1 PSHP-5
        //  If input is zero return zero otherwise if the immediate flag
        //  at name address c-addr is set return 1 otherwise return -1.
        header("IMMEDIATE?");
        code(DUPP);
        compIF();
        code(AT);
        literal(0x80);
        code(ANDD);
        compIF();
        literal(1);
        compELSE();
        literal(-1);
        compTHEN();
        compTHEN();
        code(PSEMI);

        //  SEARCH-WORDLIST ( c-addr u wid -- 0 | xt 1 | xt -1 )     //1 PSHP-5
        //  Find the definition identified by the string
        //  c-addr u in the word list identified by wid.
        //  If the definition is not found, return zero.
        //  If the definition is found, return its execution
        //  token xt and 1 if the definition is immediate,
        //  -1 otherwise.
        header("SEARCH-WORDLIST");
        call("NAME-SEARCH");
        call("IMMEDIATE?");
        code(PSEMI);

//1 PSHP-5        //  find    ( a va -- ca na | a F )  // FIND
//1 PSHP-5        //  Search a vocabulary for a string. Return ca and na if succeeded.
//1 PSHP-5        header("find");     code(SWAP); code(DUPP); code(CAT);
//1 PSHP-5                            // Optimize: divide by cell size (1) not needed here
//1 PSHP-5                            // literal(CELLL); call("/");
//1 PSHP-5                            call("tmp"); code(STORE);
//1 PSHP-5                            code(DUPP); code(AT); code(TOR);
//1 PSHP-5                            /* call("CELL+"); opt->*/ code(INC);
//1 PSHP-5                            code(SWAP);
//1 PSHP-5        /*FIND1:*/          compBEGIN();
//1 PSHP-5                                code(AT); code(DUPP);
//1 PSHP-5                                compIF(); //        DW  QBRAN,FIND6
//1 PSHP-5                                    code(DUPP); code(AT);
//1 PSHP-5                                    literal(MASKK); code(ANDD);
//1 PSHP-5                                    code(RAT); code(XORR);
//1 PSHP-5                                    compIF();//     DW  QBRAN,FIND2
//1 PSHP-5                                        /*call("CELL+"); opt->*/ code(INC);
//1 PSHP-5                                        literal(-1);    // true flag
//1 PSHP-5                                    compELSE();         //DW    BRAN,FIND3
//1 PSHP-5        /*FIND2:*/                      /*call("CELL+"); opt->*/ code(INC);
//1 PSHP-5                                        call("tmp"); code(AT);
//1 PSHP-5                                        call("SAME?");
//1 PSHP-5        /*FIND3:*/                  compTHEN();
//1 PSHP-5                                compELSE(); //      DW  BRAN,FIND4
//1 PSHP-5        /*FIND6:*/                  code(RFROM); code(DROP); code(SWAP);
//1 PSHP-5                                    /*call("CELL-"); opt->*/ code(DEC);
//1 PSHP-5                                    code(SWAP); code(EXIT);
//1 PSHP-5                                compTHEN();
//1 PSHP-5        /*FIND4:*/          compWHILE();    //      DW  QBRAN,FIND5
//1 PSHP-5                                /*call("CELL-"); call("CELL-"); */
//1 PSHP-5                                // <--opt faster than CELL- CELL-
//1 PSHP-5                                code(DEC); code(DEC);
//1 PSHP-5                            compREPEAT();   //      DW  BRAN,FIND1
//1 PSHP-5        /*FIND5:*/          code(RFROM); code(DROP); code(SWAP);
//1 PSHP-5                            code(DROP); /*call("CELL-"); opt->*/ code(DEC);
//1 PSHP-5                            code(DUPP); call("NAME>");
//1 PSHP-5                            code(SWAP); code(PSEMI);

        //  NAME-FIND ( a va -- ca na | a F )  // NAME-FIND
        //  Search a vocabulary for a string. Return ca and na if succeeded.
//1 PSHP-5  This is functionally equivalent to the word "find" commented out
//1 PSHP-5  above. It is refactored to share common code with SEARCH-WORDLIST
//1 PSHP-5  and renamed to avoid conflict with the ANS definition of FIND.
        header("NAME-FIND");
        code(TOR);
        code(DUPP);
        call("COUNT");
        code(RFROM);
        call("NAME-SEARCH");
        code(DUPP);
        compIF();
        code(ROT);
        code(DROP);
        compTHEN();
        code(PSEMI);

        //  NAME?   ( a -- ca na | a F )  // NAMEQ
        //  Search all context vocabularies for a string.
        header("NAME?");
        call("CONTEXT");
        code(DUPP);
        call("2@");
        code(XORR); // ?context=also
        compIF();
        call("CELL-");// no, start with context
        compTHEN();
        code(TOR);
        compBEGIN();
        code(RFROM);
        call("CELL+");
        code(DUPP);
        code(TOR);
        // next in search order
        code(AT);
        call("?DUP");
        compWHILE();
//1 PSHP-5                                call("find");
        call("NAME-FIND");                   //1 PSHP-5
        call("?DUP");       // search vocabulary
        compUNTIL();
        code(RFROM);
        code(DROP);
        code(EXIT);         // found name
        compTHEN();
        code(RFROM);
        code(DROP);// name not found
        literal(0);
        code(PSEMI); // false flag

        //  Terminal response

        //  ^H      ( bot eot cur -- bot eot cur )  // BKSP
        //  Backup the cursor by one character.

        header("^H");
        code(TOR);
        code(OVER);
        code(RFROM);
        code(SWAP);
        code(OVER);
        code(XORR);
        compIF();//     DW  QBRAN,BACK1
        literal(BKSPP);
        call("'ECHO");
        call("@EXECUTE");
        code(DEC);
        call("BL");
        call("'ECHO");
        call("@EXECUTE");
        literal(BKSPP);
        call("'ECHO");
        call("@EXECUTE");
        compTHEN();
        code(PSEMI);

        //  TAP     ( bot eot cur c -- bot eot cur )
        //  Accept and echo the key stroke and bump the cursor.
        header("TAP");
        code(DUPP);
        call("'ECHO");
        call("@EXECUTE");
        code(OVER);
        code(CSTOR);
        code(INC);
        code(PSEMI);

        //  kTAP    ( bot eot cur c -- bot eot cur )    // KTAP
        //  Process a key stroke, CR or backspace.
        header("kTAP");
        code(DUPP);
        literal(LF/*CRR*/);
        code(XORR);
        compIF();//     DW  QBRAN,KTAP2
        literal(BKSPP);
        code(XORR);
        compIF();//     DW  QBRAN,KTAP1
        call("BL");
        call("TAP");
        code(EXIT);
        compTHEN();
        call("^H");
        code(EXIT);
        compTHEN();
        code(DROP);
        code(SWAP);
        code(DROP);
        code(DUPP);
        code(PSEMI);

        //  accept  ( b u -- b u )  // ACCEP
        //  Accept characters to input buffer. Return with actual count.
        header("accept");
        code(OVER);
        call("+");
        code(OVER);
        /*ACCP1:*/
        compBEGIN();
        call("2DUP");
        code(XORR);
        compWHILE();
        call("KEY");
        code(DUPP);
        call("BL");
        literal(127);
        call("WITHIN");
        compIF();//     DW  QBRAN,ACCP2
        call("TAP");
        compELSE(); // DW   BRAN,ACCP3
        /*ACCP2:*/
        call("'TAP");
        call("@EXECUTE");
        /*ACCP3:*/
        compTHEN();
        compREPEAT(); //DW  BRAN,ACCP1
        /*ACCP4:*/
        code(DROP);
        code(OVER);
        call("-");
        code(PSEMI);

        //  EXPECT  ( b u -- )  // EXPEC
        //  Accept input stream and store count in SPAN.
        header("EXPECT");
        call("'EXPECT");
        call("@EXECUTE");
        call("SPAN");
        code(STORE);
        code(DROP);
        code(PSEMI);

        //  QUERY   ( -- )
        //  Accept input stream to terminal input buffer.
//        header("QUERY");    call("TIB"); literal(80);
//                           call("'EXPECT"); call("@EXECUTE");
//                            call("#TIB"); code(STORE); code(DROP);
//                            literal(0); call(">IN"); code(STORE);
//                            code(EXIT);
        header("QUERY");
        literal(TIBB);
        call("#TIB");
        call("CELL+");
        code(STORE);
//1 PSHP-5                            call("TIB"); literal(80);
        call("TIB");
        literal(TIB_SIZE);            //1 PSHP-5
        call("'EXPECT");
        call("@EXECUTE");
        call("#TIB");
        code(STORE);
        code(DROP);
        literal(0);
        call(">IN");
        code(STORE);
        code(PSEMI);


        //  Error handling

        //  CATCH   ( ca -- 0 | err# )
        //  Execute word at ca and set up an error frame for it.
        header("CATCH");
        code(SPAT);
        code(TOR);
        call("HANDLER");
        code(AT);
        code(TOR);    // save error frame
        code(RPAT);
        call("HANDLER");
        code(STORE);
        code(EXECU);            // execute
        code(RFROM);
        call("HANDLER");
        code(STORE);            // restore error frame
        code(RFROM);
        code(DROP);
        literal(0);
        code(PSEMI); // no error

//1 PSHP        //  THROW   ( err# -- err# )
//1 PSHP        //  Reset system to current local error frame an update error flag.
//1 PSHP        header("THROW");    literal(HANDL); code(AT);
//1 PSHP                            code(RPSTO);            // restore return stack
//1 PSHP                            code(RFROM); literal(HANDL);
//1 PSHP                            code(STORE);            // restore handler frame
//1 PSHP                            code(RFROM); code(SWAP); code(TOR);
//1 PSHP                            code(SPSTO);            // restore data stack
//1 PSHP                            code(DROP); code(RFROM); code(EXIT);

        //  THROW   ( err# | 0 -- err# | )
        //  If err# is non-zero reset system to current local error frame.
        header("THROW");
        call("?DUP");
        compIF();
//4 PSHP-2                                call("DEEPBLK"); code(AT);
//4 PSHP-2                                literal(0); call("=");
//4 PSHP-2                                compIF();
//4 PSHP-2                                    call("BLK"); code(AT); call("?DUP");
//4 PSHP-2                                    compIF();
//4 PSHP-2                                        call("DEEPBLK"); code(STORE);
//4 PSHP-2                                        call(">IN"); code(AT);
//4 PSHP-2                                        call("DEEPPOS"); code(STORE);
//4 PSHP-2                                    compTHEN();
//4 PSHP-2                                compTHEN();
        call("DEEPBLK");
        code(AT);
        code(ZEQUAL);           //4 PSHP-2
        call("DEEPSID");
        code(AT);
        literal(1);
        call("<");  //4 PSHP-2
        code(ANDD);                                        //4 PSHP-2
        compIF();                                          //4 PSHP-2
        call("BLK");
        code(AT);                         //4 PSHP-2
        call("DEEPBLK");
        code(STORE);                  //4 PSHP-2
        call(">IN");
        code(AT);                         //4 PSHP-2
        call("DEEPPOS");
        code(STORE);                  //4 PSHP-2
        call("SID");
        code(AT);                         //4 PSHP-2
        call("DEEPSID");
        code(STORE);                  //4 PSHP-2
        compTHEN();                                        //4 PSHP-2
        call("HANDLER");
        code(AT);
        code(RPSTO);            // restore return stack
        code(RFROM);
        call("HANDLER");
        code(STORE);            // restore handler frame
        code(RFROM);
        code(SWAP);
        code(TOR);
        code(SPSTO);            // restore data stack
        code(DROP);
        code(RFROM);
        compTHEN();
        code(PSEMI);

        //  NULL$   ( -- a )  // NULLS
        //  Return address of a null string with zero count.
        header("NULL$");
        call("doVAR");
        comma(0);
        code(PSEMI);

//1 PSHP        //  ABORT   ( -- )
//1 PSHP        //  Reset data stack and jump to QUIT.
//1 PSHP        header("ABORT");    call("NULL$");  call("THROW");

        //  ABORT   ( -- )
        //  Reset data stack and jump to QUIT.
        header("ABORT");
        literal(-1);
        call("THROW");
        code(PSEMI);

//1 PSHP       //  abort" ( f -- )  // ABORQ
//1 PSHP       //  Run time routine of ABORT" . Abort with a message.
//1 PSHP       header("abort\"");  compIF();
//1 PSHP                               literal(-1); code(SCRNREF);
//1 PSHP                               call("do$");
//1 PSHP                               call("THROW"); // pass error string
//1 PSHP                           compTHEN();
//1 PSHP                           call("do$"); code(DROP);
//1 PSHP                           code(EXIT); // drop error
//1 PSHP                           compo();

        //  abort" ( f -- )  // ABORQ
        //  Run time routine of ABORT" . Abort with a message.
        header("abort\"");
        compIF();
        literal(-1);
        code(SCRNREF);
        call("do$");
        call("ABORT$");
        code(STORE);
        literal(-2);
        call("THROW"); // pass error string
        compTHEN();
        call("do$");
        code(DROP);
        code(PSEMI); // drop error
        compo();

        // MISSING      ( c-addr --  )
        // Store c-addr (address of undefined name) in NAME$
        // and perform -13 THROW
        header("MISSING");
        call("NAME$");
        code(STORE);
        literal(-13);
        call("THROW");
        code(PSEMI);

        //  The text interpreter

        //  $INTERPRET  ( a -- )  // INTER
        //  Interpret a word. If failed, try to convert it to an integer.
        header("$INTERPRET");
        mark(); // save ca for [ defined below
        call("NAME?");
        call("?DUP"); // ?defined
        compIF();
        code(AT);
        literal(COMPO);
        code(ANDD); // ?compile only lexicon bits
        call("abort\"");
        strlit(" compile only");
        code(EXECU);// execute defined word
        code(EXIT);
        compTHEN();
        call("'NUMBER");
        call("@EXECUTE");   // convert a number
        compIF();
        code(EXIT);
        compTHEN();
        call("MISSING");
        code(PSEMI);      // error //1 PSHP

        //  [       ( -- )  // LBRAC
        //  Start the text interpreter.
        //  store ca of $INTERPRET in 'EVAL vector
//        header("[");        literal(top()); call("'EVAL");
//                            code(STORE); code(PSEMI); immed();
        header("[");
        literal(top());
        call("'EVAL");
        code(STORE);
        literal(0);
        call("STATE");
        code(STORE);
        code(PSEMI);
        immed();

        //  .OK     ( -- )  // DOTOK
        //  Display 'ok' only while interpreting.
        header(".OK");
        literal(pop());
        call("'EVAL");
        code(AT);
        call("=");
        compIF();
        call("CURSOR-CLR");
        code(AT);      // show Forth text color
        call("FG-CLR");
        code(STORE);
//3 CMJ                         call(".\"|"); strlit(" ok");
        call(".\"|");
        strlit(" OK");
        compTHEN();
        call("CR");
        code(PSEMI);

        //  ?STACK  ( -- ) // QSTAC
        //  Abort if the data stack underflows.
        header("?STACK");
        code(DEPTH);
        code(ZLESS);
        call("abort\"");
        strlit(" underflow");
        code(PSEMI);

        //  EVAL    ( -- )
        //  Interpret the input stream.
        header("EVAL");
        compBEGIN();
        call("TOKEN");
        code(DUPP);
        code(CAT);  // ?input stream empty
        compWHILE();
        call("'EVAL");
        call("@EXECUTE");
        // evaluate input, check stack
        call("?STACK");
        compREPEAT();
        code(DROP);
//5 CMJ                            call("'PROMPT"); call("@EXECUTE"); // prompt
        literal(-1);
        code(SCRNREF); //5 CMJ force a repaint of screen
        code(PSEMI);

        //  Shell

        //  PRESET  ( -- )  // PRESE
        //  Reset data stack pointer and the terminal input buffer.
        header("PRESET");
        literal(SPP);
        code(SPSTO);
        literal(TIBB);
        call("#TIB");
        call("CELL+");
        code(STORE);
        code(PSEMI);

        //  xio     ( a a a -- )  // XIO
        //  Reset the I/O vectors 'EXPECT, 'TAP, 'ECHO and 'PROMPT.
        header("xio");
        literal(doFind("accept"));
        call("'EXPECT");
        code(STORE);
        call("'TAP");
        code(STORE);
        call("'ECHO");
        code(STORE);
        call("'PROMPT");
        code(STORE);
        code(PSEMI);
        compo();

        //  FILE    ( -- )
        //  Select I/O vectors for file download.
        header("FILE");
        literal(doFind("PACE"));
        literal(doFind("DROP"));
        literal(doFind("kTAP"));
        call("xio");
        code(PSEMI);

        //  HAND    ( -- )
        //  Select I/O vectors for terminal interface.
        header("HAND");
        literal(doFind(".OK"));
        literal(doFind("EMIT"));
        literal(doFind("kTAP"));
        call("xio");
        code(PSEMI);

        //  I/O     ( -- a )  // ISLO
        //  Array to store default I/O vectors.
        header("I/O");
        call("doVAR");          // emulate CREATE
        comma(QRX + PRIMITIVE);
        comma(TXSTO + PRIMITIVE); // default I/O vectors

        //  CONSOLE ( -- )  // CONSO
        //  Initiate terminal interface.
        header("CONSOLE");
        call("I/O");
        call("2@");
        call("'?KEY");
        call("2!"); // restore default I/O device
        call("HAND"); // keyboard input
        code(PSEMI);

//1 PSHP QUIT moved further on

        //  The compiler

        //  '       ( -- ca )  // TICK
        //  Search context vocabularies for the next word in input stream.
        header("'");
        call("TOKEN");
        call("NAME?");   // ?defined
        compIF();
        code(EXIT);     // yes, push code address
        compTHEN();
        call("MISSING");
        code(PSEMI);      // no, error //1 PSHP

        //  ALLOT   ( n -- )
        //  Allocate n bytes to the code dictionary.
        header("ALLOT");
        call("CP");
        call("+!");// adjust code pointer
        code(PSEMI);

        //  ,       ( w -- )  // COMMA
        //  Compile an integer into the code dictionary.
        header(",");
        call("HERE");
        code(DUPP);
        call("CELL+");          // cell boundary
        call("CP");
        code(STORE);// adjust code pointer
        code(STORE);
        code(PSEMI);// compile

        //  [COMPILE]   ( -- ; <string> )  // BCOMP
        //  Compile the next immediate word into code dictionary.
        header("[COMPILE]");
        call("'");
        call(",");
        code(PSEMI);
        immed();

        //  COMPILE ( -- )  // COMPI
        //  Compile the next address in colon list to code dictionary.
        header("COMPILE");
        code(RFROM);
        code(DUPP);
        code(AT);
        call(",");      // compile address
        call("CELL+");
        code(TOR);      // adjust return address
        code(PSEMI);
        compo();

        //  LITERAL ( w -- )  // LITER
        //  Compile tos to code dictionary as an integer literal.
        header("LITERAL");
        call("COMPILE");
        code(DOLIT);
        call(",");
        code(PSEMI);
        immed();

        //  $,"		( -- )  // STRCQ    TODO: inspect
        //  Compile a literal string up to next " .
        header("$,\"");
        literal('"');
        call("WORD");   // move string to code dictionary
        call("COUNT");
        call("+");
        // call("ALIGNED"); // align end of string
        call("CP");
        code(STORE);
        code(PSEMI);     // adjust the code pointer

        //  RECURSE ( -- ) RECUR
        //  Make the current word available for compilation.
        header("RECURSE");
        call("LAST");
        code(AT);
        call("NAME>");
        call(",");
        code(PSEMI);
        immed();

        //  Structures

        //  FOR     ( -- a )
        //  Start a FOR-NEXT loop structure in a colon definition.
        header("FOR");
        call("COMPILE");
        code(TOR);
        call("HERE");
        code(PSEMI);
        immed();

        //  BEGIN   ( -- a )
        //  Start an infinite or indefinite loop structure.
        header("BEGIN");
        call("HERE");
        code(PSEMI);
        immed();

        //  NEXT    ( a -- )
        //  Terminate a FOR-NEXT loop structure.
        header("NEXT");
        call("COMPILE");
        code(DONXT);
        call(",");
        code(PSEMI);
        immed();

        //  doDO    ( limit index -- )
        //  Prepare return stack to begin a DO-LOOP structure.
        header("doDO");
        code(RFROM);
        code(ROT);
        code(TOR);
        code(SWAP);
        code(TOR);
        code(TOR);
        code(PSEMI);

//        //  DO      ( -- a )
//        //  Start a DO-LOOP structure in a colon definition.
//        header("DO");       call("COMPILE"); call("doDO");
//                            call("HERE"); code(PSEMI); immed();

//        //  +LOOP   ( a -- )
//        //  Start a DO-LOOP structure in a colon definition.
//        header("+LOOP");    call("COMPILE"); code(PLOOP);
//                            call(","); code(PSEMI); immed();

//        //  LOOP    ( a -- )
//        //  Start a DO-LOOP structure in a colon definition.
//        header("LOOP");     call("COMPILE"); code(DOLIT);
//                            literal(1); call(",");
//                            call("COMPILE"); code(PLOOP);
//                            call(",");
//                            code(PSEMI); immed();

        //  DO      ( -- n a )
        //  Start a DO-LOOP structure in a colon definition.
        header("DO");
        call("LEAVES");
        code(AT);
        literal(0);
        call("LEAVES");
        code(STORE);
        call("COMPILE");
        call("doDO");
        call("HERE");
        code(PSEMI);
        immed();

        //  +LOOP   ( n a -- )
        //  Start a DO-LOOP structure in a colon definition.
        header("+LOOP");
        call("COMPILE");
        code(PLOOP);
        call(",");
        call("LEAVES");
        code(AT);
        compBEGIN();
        call("?DUP");
        compWHILE();
        code(DUPP);
        code(AT);
        call("HERE");
        code(ROT);
        code(STORE);
        compREPEAT();
        call("LEAVES");
        code(STORE);
        code(PSEMI);
        immed();

        //  LOOP    ( n a -- )
        //  Start a DO-LOOP structure in a colon definition.
        header("LOOP");
        call("COMPILE");
        code(DOLIT);
        literal(1);
        call(",");
        call("+LOOP");
        code(PSEMI);
        immed();

        // UNLOOP   (  --  )
        // Discard loop control parameters for current nesting level.
        header("UNLOOP");
        code(RFROM);
        code(RFROM);
        code(RFROM);
        call("2DROP");
        code(TOR);
        code(PSEMI);

        // LEAVE    (  --  )
        // Branch to end of enclosing DO-LOOP structure.
        header("LEAVE");
        call("COMPILE");
        call("UNLOOP");
        call("COMPILE");
        code(BRAN);
        call("HERE");
        call("LEAVES");
        code(DUPP);
        code(AT);
        call(",");
        code(STORE);
        code(PSEMI);
        immed();


        //  UNTIL   ( a -- )
        //  Terminate a BEGIN-UNTIL indefinite loop structure.
        header("UNTIL");
        call("COMPILE");
        code(QBRAN);
        call(",");
        code(PSEMI);
        immed();

        //  AGAIN   ( a -- )
        //  Terminate a BEGIN-AGAIN infinite loop structure.
        header("AGAIN");
        call("COMPILE");
        code(BRAN);
        call(",");
        code(PSEMI);
        immed();

        //  IF      ( -- A )  // IFF
        //  Begin a conditional branch structure.
        header("IF");
        call("COMPILE");
        code(QBRAN);
        call("HERE");
        literal(0);
        call(",");
        code(PSEMI);
        immed();
        compo();                            //1 CMJ

        //  AHEAD   ( -- A )
        //  Compile a forward branch instruction.
        header("AHEAD");
        call("COMPILE");
        code(BRAN);
        call("HERE");
        literal(0);
        call(",");
        code(PSEMI);
        immed();

        //  REPEAT  ( A a -- )  //
        //  Terminate a BEGIN-WHILE-REPEAT indefinite loop
        header("REPEAT");
        call("AGAIN");
        call("HERE");
        code(SWAP);
        code(STORE);
        code(PSEMI);
        immed();

        //  THEN    ( A -- )  // THENN
        //  Terminate a conditional branch structure.
        header("THEN");
        call("HERE");
        code(SWAP);
        code(STORE);
        code(PSEMI);
        immed();

        //  AFT     ( a -- a A )
        //  Jump to THEN in a FOR-AFT-THEN-NEXT loop the first time through.
        header("AFT");
        code(DROP);
        call("AHEAD");
        call("BEGIN");
        code(SWAP);
        code(PSEMI);
        immed();

        //  ELSE    ( A -- A )  // ELSEE
        //  Start the false clause in an IF-ELSE-THEN structure.
        header("ELSE");
        call("AHEAD");
        code(SWAP);
        call("THEN");
        code(PSEMI);
        immed();

        //  WHILE   ( a -- A a )
        //  Conditional branch out of a BEGIN-WHILE-REPEAT loop
        header("WHILE");
        call("IF");
        code(SWAP);
        code(PSEMI);
        immed();

        //  ABORT"	( -- ; <string> )  // ABRTQ
        //  Conditional abort with an error message.
        header("ABORT\"");
        call("COMPILE");
        call("abort\"");
        call("$,\"");
        code(PSEMI);
        immed();

        //  $"		( -- ; <string> )  // STRQ
        //  Compile an inline string literal.
        header("$\"");
        call("COMPILE");
        call("$\"|");
        call("$,\"");
        code(PSEMI);
        immed();

        //  ."		( -- ; <string> )  // DOTQ
        //  Compile an inline string literal to be typed out at run time.
        header(".\"");
        call("COMPILE");
        call(".\"|");
        call("$,\"");
        code(PSEMI);
        immed();

        //  Name compiler

        //  ?UNIQUE ( a -- a ) // UNIQU
        //  Display a warning message if the word already exists.
        header("?UNIQUE");
        code(DUPP);
        call("NAME?");  // ?name exists
        compIF();           // ?redefinitions are OK
        call(".\"|");
        strlit(" reDef ");  // but warn the user
        code(OVER);
        call("COUNT");
        call("TYPE");           // just in case its not planned
        compTHEN();
        code(DROP);
        code(PSEMI);

        //  $,n     ( na -- )  // SNAME
        //  Build a new dictionary name using the string at na.
        header("$,n");
        code(DUPP);
        code(CAT);      // ?null input
        compIF();
        call("?UNIQUE");        // ?redefinition
        code(DUPP);
        call("LAST");
        code(STORE);// save na for vocabulary link
        call("HERE"); /*call(ALGND);*/
        code(SWAP);         // align code address
        call("CELL-");      // link address
        call("CURRENT");
        code(AT);
        code(AT);
        code(OVER);
        code(STORE);
        call("CELL-");
        code(DUPP);
        call("NP");
        code(STORE);// adjust name pointer
        code(STORE);
        code(EXIT);
        compTHEN();
//1 PSHP                            call("$\"|"); strlit(" name");  // null input
//1 PSHP                            call("THROW");
        literal(-1);                      //1 PSHP
        call("abort\"");
        strlit("name");  //1 PSHP
        code(PSEMI);                      //1 PSHP

        //  FORTH compiler
        //  $COMPILE    ( a -- )  // SCOMP
        //  Compile next word to code dictionary as a token or literal.
        header("$COMPILE");
        call("NAME?"); // code(VERBO);
        call("?DUP");   // ?defined
        compIF();
        code(AT);
        literal(IMEDD);
        code(ANDD);  // ?immediate
        compIF();
        code(EXECU);// immediate, execute
        code(EXIT);
        compTHEN();
        call(",");      // not immediate, compile
        code(EXIT);
        compTHEN();
        call("'NUMBER");
        call("@EXECUTE");// try to convert to number
        compIF();
        call("LITERAL");// compile number as integer
        code(EXIT);
        compTHEN();
        call("MISSING");
        code(PSEMI);       // error  //1 PSHP

        //  OVERT   ( -- )
        //  Link a new word into the current vocabulary.
        header("OVERT");
        call("LAST");
        code(AT);
        call("CURRENT");
        code(AT);
        code(STORE);
        code(PSEMI);

        //  COMPILE-(;) ( -- )
        //  Compile code to terminate a colon definition. //1 PSHP
        header("COMPILE-(;)");
        call("COMPILE");
        code(PSEMI);
        code(PSEMI);

        //  ;       ( -- )  // SEMIS
        //  Terminate a colon definition.
        header(";");
        call("COMPILE-(;)"); //1 PSHP
        call("[");
        call("OVERT");
        code(PSEMI);
        immed();
        compo();

        //  ]       ( -- )  // RBRAC
        //  Start compiling the words in the input stream.
//        header("]");        literal(doFind("$COMPILE"));
//                            call("'EVAL"); code(STORE); code(PSEMI);
        header("]");
        literal(doFind("$COMPILE"));
        call("'EVAL");
        code(STORE);
        literal(-1);
        call("STATE");
        code(STORE);
        code(PSEMI);


        //  call,   ( ca -- )  // CALLC
        //  Assemble a call instruction to ca.
        header("call,");
        code(PSEMI);
        immed(); // do nothing for our VM

        //  :       ( -- ; <string> )  // COLON
        //  Start a new colon definition using next word as its name.
        header(":");
        call("TOKEN");
        call("$,n");
        call("]");
        code(PSEMI);

        //  IMMEDIATE   ( -- )  // IMMED
        //  Make the last compiled word an immediate word.
        header("IMMEDIATE");
        literal(IMEDD);
        call("LAST");
        code(AT);
        code(AT);
        code(ORR);
        call("LAST");
        code(AT);
        code(STORE);
        code(PSEMI);

        //  Defining words
        //  USER    ( u -- ; <string> )
        //  Compile a new user variable.
        header("USER");
        call("TOKEN");
        call("$,n");
        call("OVERT");
        call("COMPILE");
        code(DOLIT);
        call(",");
        code(PSEMI);

        //  CREATE  ( -- ; <string> )  // CREAT
        //  Compile a new array entry without allocating code space.
        header("CREATE");
        call("TOKEN");
        call("$,n");
        call("OVERT");
        call("COMPILE");
        call("doVAR");
        code(PSEMI);
        // FIXCFA   ( -- )
        // Fixes the CFA of a CREATEd word to run the code that will
        // follows DOES> in a definition.
        header("FIXCFA");
        code(RAT);
        code(INC);
        call("LAST");
        code(AT);
        code(DEC);
        code(DEC);
        code(AT);
        code(STORE);
        code(PSEMI);
        //  DOES>  ( -- )
        //  Defines the run-time action of a CREATEd word
        header("DOES>");
        call("COMPILE");
        call("FIXCFA"); // -- ca
        call("COMPILE");
        code(EXIT);
        call("COMPILE");
        code(RFROM);
        code(PSEMI);
        immed();

        //  VARIABLE    ( -- ; <string> )  // VARIA
        //  Compile a new variable initialized to 0.
        header("VARIABLE");
        call("CREATE");
        literal(0);
        call(",");
        code(PSEMI);

        // Tools


        //  _TYPE   ( b u -- )  // UTYPE
        //  Display a string. Filter non-printing characters.
        header("_TYPE");
        code(TOR);          // start count down loop
        compAFT();          // skip first pass
        mark();
        code(DUPP);
        code(CAT);
        call(">CHAR");
        call("EMIT");   // display only printable
        code(INC);      // increment address
        swap();
        compTHEN();
        compNEXT();         // loop till done
        code(DROP);
        code(PSEMI);

        //  dm+     ( a u -- a )  // DMP
        //  Dump u bytes from , leaving a+u on the stack.
        header("dm+");      //literal(0); code(SCRNREF);
        code(OVER);
        literal(4);
        call("U.R");        // display address
        call("SPACE");
        code(TOR);          // start count down loop
        compAFT();
        mark();
        code(DUPP);
        code(CAT);
        literal(9);
        call("U.R");    // display numeric data
        code(INC);      // increment address
        swap();
        compTHEN();
        compNEXT();         // loop till done
//                            literal(-1); code(SCRNREF);
        code(PSEMI);

        //  DUMP    ( a u -- )
        //  Dump u bytes from a, in a formatted manner.
        header("DUMP");
        call("BASE");
        code(AT);
        code(TOR);
        call("HEX");     // save radix, set hex
        literal(4);
        call("/");      // change count to lines
        compFOR();                  // start count down loop
        call("CR");
        literal(4);
        call("2DUP");
        call("dm+");            // display numeric
        code(ROT);
        code(ROT);
        call("SPACE");
        call("SPACE");
        call("_TYPE");  // display printable characters
        //  call("NUF?"); call("NOT"); // user control
        // compWHILE();
        compNEXT();                 // loop till done
        // compELSE();
        //  code(RFROM); code(DROP);// cleanup loop stack, early EXIT
        // compTHEN();
        code(DROP);
        code(RFROM);
        call("BASE");
        code(STORE); //restore radix
        code(PSEMI);

        //  .S      ( ... -- ... )  // DOTS
        //  Display the contents of the data stack.
        header(".S");
        call("CR");
        call("DEPTH");  // stack depth
        code(TOR);          // start count down loop
        compAFT();
        mark();
        code(RAT);
        call("PICK");
        call(".");      // index stack, display contents
        swap();
        compTHEN();
        compNEXT();         // loop till done
        call(".\"|");
        strlit(" <sp");
        code(PSEMI);

        //  !CSP    ( -- )  // STCSP
        //  Save stack pointer in CSP for error checking.
        header("!CSP");
        code(SPAT);
        call("CSP");
        code(STORE);
        code(PSEMI);

        //  ?CSP    ( -- )  // QCSP
        //  Abort if stack pointer differs from that saved in CSP.
        header("?CSP");
        code(SPAT);
        call("CSP");
        code(AT);
        code(XORR);   // compare pointers
        call("abort\"");
        strlit("stacks");       // abort if different
        code(PSEMI);

        //  >NAME   ( ca -- na | F )    // TNAME
        //  Convert code address to a name address.
        header(">NAME");
        call("CURRENT"); // vocabulary link
        compBEGIN();
        code(INC);      // <- opt for CELL+
        code(AT);
        call("?DUP");   // check all vocabularies
        compWHILE();
        call("2DUP");
        compBEGIN();
        code(AT);
        code(DUPP); // ?last word in a vocabulary
        compWHILE();
        call("2DUP");
        call("NAME>");
        code(XORR); // compare
        compWHILE();
        code(DEC);  // <- opt   for CELL-
        // continue with next wor
        compREPEAT();
        compTHEN();
        code(SWAP);
        code(DROP);
        call("?DUP");
        compUNTIL();
        code(SWAP);
        code(DROP);
        code(SWAP);
        code(DROP);
        code(EXIT);
        compTHEN();
        code(DROP);
        literal(0); // false flag
        code(PSEMI);

        //  .ID     ( na -- )   // DOTID
        //  Display the name at address.
        header(".ID");
        call("?DUP");       // if zero no name
        compIF();
        call("COUNT");
        literal(0x1F);
        code(ANDD);     // mask lexicon bits
        call("_TYPE");  // display name string
        code(EXIT);
        compTHEN();
        call(".\"|");
        strlit(" {noName}");
        code(PSEMI);

        //  SEE     ( -- ; <string> )
        //  A simple decompiler.
        header("SEE");
        call("'");      // starting address
        code(DUPP);
        code(AT);                     //1 PSHP
        code(DUPP);                               //1 PSHP
        literal(doFind("doCON"));                 //1 PSHP
        call("=");                                //1 PSHP
        compIF();                                 //1 PSHP
        code(DROP);                           //1 PSHP
        call(".\"|");                         //1 PSHP
        strlit("  constant  = ");             //1 PSHP
        code(INC);
        code(AT);
        call(".");       //1 PSHP
        code(EXIT);                           //1 PSHP
        compTHEN();                               //1 PSHP
        code(DUPP);                               //1 PSHP
        literal(doFind("doUSER"));                //1 PSHP
        call("=");                                //1 PSHP
        compIF();                                 //1 PSHP
        code(DROP);                           //1 PSHP
        call(".\"|");                         //1 PSHP
        strlit("  user variable  contains "); //1 PSHP
        code(INC);
        code(AT);
        code(AT);        //1 PSHP
        call(".");                            //1 PSHP
        code(EXIT);                           //1 PSHP
        compTHEN();                               //1 PSHP
        code(DUPP);                               //1 PSHP
        literal(doFind("doVAR"));                 //1 PSHP
        call("=");                                //1 PSHP
        compIF();                                 //1 PSHP
        code(DROP);                           //1 PSHP
        call(".\"|");                         //1 PSHP
        strlit("  data-field address  contains ");   //1 PSHP
        code(INC);
        code(AT);                  //1 PSHP
        call(".");                            //1 PSHP
        code(EXIT);                           //1 PSHP
        compTHEN();                               //1 PSHP
        code(DROP);                               //1 PSHP
        call("CR");
        compBEGIN();
        /*SEE1:*/
        code(DUPP);
        code(AT);
        code(DUPP); //?does it contain a zero
        compIF();   //DW    QBRAN,SEE2
        call(">NAME");  //?is it a name
        /*SEE2:*/
        compTHEN();
        call("?DUP");       //name address or zero
        compIF();//     DW  QBRAN,SEE3
        call("SPACE");
        call(".ID");    //display name
        compELSE();//       DW  BRAN,SEE4
        code(DUPP);
        code(AT);
        call("U.");//display number
        /*SEE4:*/
        compTHEN();
        code(INC);      // <- opt for CELL+
        call("NUF?");       //user control
        code(OVER);                           //1 PSHP
        code(DEC);                            //1 PSHP
        code(AT);                             //1 PSHP
        literal(doFind("(;)"));               //1 PSHP
        call("=");                            //1 PSHP
        code(ORR);                            //1 PSHP
        compUNTIL();//      DW  QBRAN,SEE1
        code(DROP);
        code(PSEMI);

        //  WORDS   ( -- )
        //  Display the names in the context vocabulary.
        header("WORDS");
        call("CR");
        call("CONTEXT");
        code(AT);   // only in context
        compBEGIN();
        code(AT);
        call("?DUP"); // ?at end of list
        compWHILE();
        code(DUPP);
        call("SPACE");
        call(".ID");            // display a name
        code(DEC);              // <- opt for CELL-
        call("NUF?"); // user control
        compUNTIL();
        code(DROP);
        compTHEN();
        code(PSEMI);

        //  FORGET  ( "name" -- )
        //  Forgets all the recent words back to and including
        //  the named word in the CURRENT vocabulary.
        header("FORGET");
        literal(32);
        call("WORD");
        call("CURRENT");
        code(AT);
//1 PSHP-5                                call("find");
        call("NAME-FIND");                   //1 PSHP-5
        code(DUPP);
        compIF();
        code(SWAP);
        call("CP");
        code(STORE);
        code(DEC); // <-- opt for CELL-
        code(AT);
        code(DUPP);
        call("LAST");
        code(STORE);
        // opt below for: 2 CELLS -
        code(DEC);
        code(DEC);
        call("NP");
        code(STORE);
        call("OVERT");
        compELSE();
        code(DROP);
        code(DROP);
        literal(-1);
        call("abort\"");
        strlit("which ");
        compTHEN();
        code(PSEMI);

        //  Tracing tools

        //  trace  ( f ; "name" -- )
        //  Does a traced execution of "name",
        //  possibly doing single-stepping.
        header("trace");
        call("TOKEN");
        call("NAME?");
        call("?DUP"); // ?defined
        compIF();       // f ca na --
        code(AT);
        literal(COMPO);
        code(ANDD); // ?compile only lexicon bits
        call("abort\"");
        strlit(" compile only");
        code(SWAP);
        call("TRACING");
        code(STORE);
        code(EXECTR);// execute defined word
        code(QUIET);// shut off tracing, if on
        code(EXIT);
        compTHEN();
        call("'NUMBER");
        call("@EXECUTE");   // convert a number
        compIF();
        call(".S");
        code(EXIT);
        compTHEN();
        call("MISSING");
        code(PSEMI);      // error  //1 PSHP


        header("TRACE");
        literal(0);
        call("trace");
        code(PSEMI);

        header("STEP");
        call("STEPPING");
        call("trace");
        code(PSEMI);

        /////////////////////////////////////////////////////////////
        //  BLOCK I/O
        /////////////////////////////////////////////////////////////

//1 CMJ        // SEARCH ( addr elements target -- addr' -1 | 0 )
        // LINEAR-SEARCH ( addr elements target -- addr' -1 | 0 ) //1 CMJ Renamed to avoid confusion with ANS SEARCH
        // Searches through memory, up to number of "elements" starting
        // at "addr" looking for the "target" number.  Returns the first
        // address that contains a match to "target" and a true flag.
        // Otherwise, if there was no match, it just returns a false flag.
        // This is a linear search.
        header("LINEAR-SEARCH");
        code(TOR);
        code(OVER);
        call("+");  // -- addr lim-addr
        compBEGIN();
        code(OVER);
        code(OVER);
        call("<");          // are we still at a valid address?
        compWHILE();
        code(OVER);
        code(AT);
        code(RAT);
        call("=");   // does this address hold the target?
        compIF();               // YES!, drop the limit address
        code(RFROM);
        code(DROP);    // remove the target
        code(DROP);
        literal(-1);

        code(EXIT);         // EXIT the loop early
        compTHEN();
        code(SWAP);
        code(INC);  // Look in next address
        code(SWAP);
        compREPEAT();
        code(DROP);
        code(DROP);     // No target found, drop addresses
        code(RFROM);
        code(DROP);    //
        literal(0);
        code(PSEMI);

        // BUF>ADDR ( buf# -- addr )
        // Converts a buf # to an address
        header("BUF>ADDR");
        literal(BLKLEN);
        call("*");
        call("BUFFER0");
        call("+");
        code(PSEMI);

        // ASSIGN-BUF ( block# buf# -- addr)
        // Associates a BLOCK with the specified block buffer
        // Returns the corresponding block buffer address.
        header("ASSIGN-BUF");
        code(DUPP);
        call("LSTBUF");
        code(STORE); // Save new LSTBUF
        code(SWAP);
        code(OVER); // -- buf# block# buf#
        call("BUF0");
        call("+");
        code(STORE);    // set block# for buffer
        code(DUPP);
        call("UPD0");
        call("+");
        literal(0);
        code(SWAP);
        code(STORE);    // clear update flag
        call("BUF>ADDR"); // return buffer address
        literal(0);
        call("CURPOS");
        code(STORE);    // Reset current position
        code(PSEMI);

        header(".BUFS");    // Diagnostic dump of BLOCK variables
        call("CR");
        literal(0);
        literal(7);
        compFOR();
        code(DUPP);
        call("BUF0");
        call("+");
        code(AT);
        call(".");
        code(INC);
        compNEXT();
        code(DROP);

        call("CR");
        literal(0);
        literal(7);
        compFOR();
        code(DUPP);
        call("UPD0");
        call("+");
        code(AT);
        call(".");
        code(INC);
        compNEXT();
        code(DROP);

        call("CR");
        call("LSTBUF");
        code(AT);
        call(".");
        code(PSEMI);

        //  INBUF? ( block# -- buf# -1 | 0)
        header("INBUF?");   // Is block already in buffer?
        call("BUF0");
        literal(BUFS);
        code(ROT);
        call("LINEAR-SEARCH");
        compIF();
        call("BUF0");
        call("-");
        literal(-1);
        compELSE();
        literal(0);
        compTHEN();
        code(PSEMI);

        //  BUFFER ( n -- addr )
        //    Returns the address of a BLOCK's assigned buffer,
        //    finding or making an available one if needed.
        header("BUFFER");   // Is block already in buffer?
        code(DUPP);
        call("INBUF?");
        compIF();
        // Found! Return its address
        call("BUF>ADDR");
        code(SWAP);
        code(DROP);
        code(EXIT);
        compTHEN();

        // Is there an empty buffer?
        call("BUF0");
        literal(BUFS);
        literal(-1);
        call("LINEAR-SEARCH");
        compIF();
        call("BUF0");
        call("-");   // -- block buf#
        // Found! Return its address
        call("ASSIGN-BUF");
        code(EXIT);
        compTHEN();

        // Must free a buffer
        // Use least recently assigned
        call("LSTBUF");
        code(AT);
        // Least recent is (LSTBUF + 1) MOD 7
        code(INC);
        literal(BUFS - 1);
        code(ANDD);
        // -- n buf#
        // Current buffer updated?
        code(DUPP);
        call("UPD0");
        call("+");
        code(AT);
        compIF();
        code(DUPP);
        call("BUF>ADDR");// -- n buf# addr
        code(OVER);
        call("BUF0");
        call("+");
        code(AT);// -- n buf# addr blk#
        // save current buffer to file
        code(SWAP);
        code(BLOCKWR);
        compTHEN();
        // -- n buf#
        call("ASSIGN-BUF");
        code(PSEMI);

        //  BLOCK? ( n -- n )  Checks for bad block numbers.
        header("BLOCK?");   // must not be past valid blocks
        code(DUPP);
        code(DEC);
        call("BLOCKS");
        code(AT);
        call("U<");
        call("NOT");
        call("abort\"");
        strlit("block number");
        // must not be less than 1
        // i.e. (n-1) must not be less than 0
        code(DUPP);
        code(DEC);
        code(ZLESS);
        call("abort\"");
        strlit("block number");
        code(PSEMI);

        //  BLOCK ( n -- addr )  Return an addess of a BLOCK buffer,
        //    reading block from mass storage if needed.
        header("BLOCK");
        call("BLOCK?");
        code(DUPP);
        call("CURBLK");
        code(STORE);

        code(DUPP);
        call("INBUF?");
        compIF();                       // BLOCK already in memory
        code(SWAP);
        code(DROP);     // Drop block number
        call("BUF>ADDR");           // Leave with BLOCK addr on stack
        code(EXIT);
        compELSE();
        code(DUPP);
        call("BUFFER"); // -- n addr
        code(SWAP);
        code(OVER);     // -- addr n addr
        code(BLOCKRD);
        call("NOT");
        call("abort\"");
        strlit("bad block or file"); // abort if not read
        compTHEN();
        code(PSEMI);

        // UPDATE ( -- )  Mark the most recently accessed BLOCK for update
        header("UPDATE");
        call("CURBLK");
        code(AT);
        call("INBUF?");
        compIF();
        call("UPD0");
        call("+");
        literal(-1);
        code(SWAP);
        code(STORE);
        compTHEN();
        code(PSEMI);

        // .BLOCK ( addr --)    Print BLOCK buffer
        header(".BLOCK");
        literal(15);
        compFOR();
//                                literal(0); code(SCRNREF);
        call("CR");
        literal(15);
        code(RAT);
        call("-");
        literal(2);
        call("U.R");
        literal(':');
        call("EMIT");
        literal(' ');
        call("EMIT");
        code(DUPP);
        literal(64);
        call("TYPE");
//                                literal(-1); code(SCRNREF);
        literal(64);
        call("+");
        compNEXT();
        code(DROP);
        code(PSEMI);

        // LIST ( n -- )  Displays the contents of a BLOCK.
        header("LIST");
        call("BLOCK");
        call(".BLOCK");
        code(PSEMI);

        // SAVE-BUFFERS  ( -- )  Saves all UPDATEd BLOCK buffers.
        header("SAVE-BUFFERS");
        literal(BUFS - 1);
        compFOR();
        code(RAT);
        call("UPD0");
        call("+");
        code(AT);
        compIF();
        code(RAT);
        call("BUF0");
        call("+");
        code(AT);
        code(RAT);
        call("BUF>ADDR");
        code(BLOCKWR);
        literal(0);
        code(RAT);
        call("UPD0");
        call("+");
        code(STORE);
        compTHEN();
        compNEXT();
        code(PSEMI);

        // SAVE  ( -- )
        //   Saves the current BLOCK buffer to storage
        header("SAVE");
        call("CURBLK");
        code(AT);
        call("INBUF?");
        compIF();
        code(DUPP);
        call("BUF>ADDR");       // -- buf# addr
        call("CURBLK");
        code(AT);
        code(SWAP);
        code(BLOCKWR);
        literal(0);
        code(SWAP); // -- 0 buf#
        call("UPD0");
        call("+");
        code(STORE);
        compTHEN();
        code(PSEMI);

        // EMPTY-BUFFERS  ( -- )  Un-assigns all buffers.
        header("EMPTY-BUFFERS");
        call("UPD0");
        literal(BUFS);
        literal(0);
        call("FILL");
        call("BUF0");
        literal(BUFS);
        literal(0);
        call("FILL");
        literal(0);
        call("CURPOS");
        code(STORE);
        code(PSEMI);

        // FLUSH  ( -- )  Saves all UPDATEd buffers and empties them.
        header("FLUSH");
        call("SAVE-BUFFERS");
        call("EMPTY-BUFFERS");
        code(PSEMI);

        // CURBUF  ( -- addr )
        //   Gets address of current BLOCK in its buffer, if any
        header("CURBUF");
        call("CURBLK");
        code(AT);
        call("INBUF?");
        compIF();
        call("BUF>ADDR");
        compELSE();
        call("abort\"");
        strlit("no block"); // abort if no current block
        compTHEN();
        code(PSEMI);

        // WIPE  ( -- )  Blanks out current block
        header("WIPE");
        call("CURBUF");
        literal(BLKLEN);
        literal(' ');
        call("FILL");
        literal(0);
        call("CURPOS");
        code(STORE);
        code(PSEMI);

        // L  ( -- )  Lists current block
        header("L");
        call("CURBLK");
        code(AT);
        call("LIST");
        code(PSEMI);

        // LINE?  ( n -- n )
        //   Checks for bad line numbers.
        header("LINE?");
        code(DUPP);
        literal(0);
        literal(16);
        call("WITHIN");
        call("NOT");
        compIF();
        call("abort\"");
        strlit("line number"); // abort if not lines 0 to 15
        compTHEN();
        code(PSEMI);

        // CURSOR  ( -- addr )
        //   Gets address of cursor position in current BLOCK.
        header("CURSOR");
        call("CURBUF");
        call("CURPOS");
        code(AT);
        call("+");
        code(PSEMI);

        // home  ( -- )
        //   Moves the editor cursor to the beginning of the current row.
        header("home");
        call("CURPOS");
        code(AT);
        literal(0xFFFFFFC0);
        code(ANDD);
        call("CURPOS");
        code(STORE);
        code(PSEMI);

        // +SLIDE  ( n -- )
        //   Slides line starting at CURSOR right by n characters.
        //   Characters at end of line are lost.
        header("+SLIDE");
        call("CURSOR");
        code(OVER);
        code(OVER);
        call("+");
        code(ROT);       // -- cursor cursor+n n
        literal(64);
        code(SWAP);
        call("-");                  // -- . .  64-n
        call("CURPOS");
        code(AT);
        literal(63);
        code(ANDD);
        call("-");                  // -- cursor cursor+n 64-n-(CURPOS@ AND 0x3F)
        call("CMOVE>");
        call("UPDATE");
        code(PSEMI);

        // -SLIDE  ( n -- )
        //   Slides line starting at CURSOR left by n characters.
        //   Characters at end of line are padded with blanks.
        header("-SLIDE");
        code(TOR);
        call("CURSOR");
        code(DUPP);
        code(RAT);
        call("-");                  // -- cur cur-n
        literal(64);
        call("CURPOS");
        code(AT);
        literal(63);
        code(ANDD);
        call("-");                  // -- cur cur-n 64-(CURPOS@ AND 0x3F)
        call("CMOVE");

        call("CURSOR");
        literal(63);
        code(ORR);
        code(RAT);
        code(DEC);
        call("-");
        code(RAT);                // -- endline-(n-1) n
        literal(' ');
        call("FILL");

        code(RFROM);
        call("NEGATE");
        call("CURPOS");
        call("+!");

        call("UPDATE");
        code(PSEMI);

        // SHOVE  ( -- )
        //   Shoves the current line and the following lines down by one.
        //   The last line is lost.
        header("SHOVE");
        call("home");
        literal(64);
        call("CURSOR");
        code(OVER);
        code(OVER);
        call("+");
        code(ROT);       // -- cursor cursor+n n
        literal(BLKLEN);
        code(SWAP);
        call("-");                  // -- . .  1024-n
        call("CURPOS");
        code(AT);
        call("-");                  // -- cursor cursor+n 1024-n-(CURPOS@)
        call("CMOVE>");
        call("UPDATE");
        code(PSEMI);

        // C  ( -- )
        //   Clears the current line
        header("C");
        call("home");
        call("CURSOR");
        literal(64);
        literal(' ');
        call("FILL");
        call("UPDATE");
        code(PSEMI);

        // .LINE  ( -- )
        //   Types line containing insertion point in current BLOCK.
        header(".LINE");    //literal(0); code(SCRNREF);
        call("CURSOR");
        literal(0xFFFFFFC0);
        code(ANDD);  // get home
        literal(64);
        call("CR");
        call("TYPE");   // type line
        call("CR");
        call("CURPOS");
        code(AT);
        literal(0x3F);
        code(ANDD);
        call("SPACES");
        literal('^');
        call("EMIT"); // show cursor
        //literal(-1); code(SCRNREF);
        code(PSEMI);

        // T  ( n -- )
        //   Types line n of current BLOCK.
        header("T");
        call("LINE?");
        literal(64);
        call("*");
        call("CURPOS");
        code(STORE);
        call(".LINE");
        code(PSEMI);

        // get-insert-text  ( -- )
        //   Stores input text (if any) at PAD, which is our
        //   insert buffer.
        header("get-insert-text");
        literal(94);
        call("WORD");
        code(DUPP);
        code(AT);    // WORD found something?
        compIF();
        call("PAD");              // Copy to PAD , including
        code(OVER);
        code(AT);     // everything, including
        code(INC);
        call("CMOVE"); // count
        compTHEN();
        code(PSEMI);

        // P  ( <text> -- )
        //   Allows user to enter text in current line.
        header("P");
        call("C");
        call("get-insert-text");
        call("PAD");
        call("COUNT");
        call("CURSOR");
        code(SWAP);
        call("CMOVE");
        call("UPDATE");
        code(PSEMI);

        // U  ( <text> -- )
        //   Allows user to enter text under the current line.
        header("U");
        literal(64);
        call("CURPOS");
        call("+!");
        call("SHOVE");
        call("P");
        code(PSEMI);

        // X  ( -- )
        //   Extracts the current line to the insert buffer.
        header("X");
        call("home");
        call("CURSOR");
        call("PAD");
        code(INC);
        literal(64);
        call("CMOVE");
        literal(64);
        call("PAD");
        code(STORE);

        call("CURSOR");
        literal(64);
        call("+");
        call("CURSOR");  // -- cursor+n cursor
        literal(BLKLEN - 64);
        call("CURPOS");
        code(AT);
        call("-");                  // -- cursor cursor+n 1024-64-(CURPOS@)
        call("CMOVE");

        call("CURBUF");
        literal(BLKLEN - 64);
        call("+");
        literal(64);
        literal(' ');
        call("FILL"); // Blank out last line

        call("UPDATE");
        code(PSEMI);

        // insert  ( <text> -- )
        //   Inserts text from PAD at the current cursor
        //   position, moving other text to the right.
        header("insert");
        call("get-insert-text");
        call("PAD");
        call("COUNT");
        code(DUPP);
        call("+SLIDE");
        call("CURSOR");
        code(SWAP);
        call("CMOVE");
        call("UPDATE");
        code(PSEMI);

        // I  ( <text> -- )
        //   Compile mode: pushes current loop index on stack
        //   Interpret mode: Inserts text then redisplays the line
        //   Yes, this a kludge, but I did not want to make a
        //   separate EDITOR vocabulary.
        header("I");
        literal(doFind("$COMPILE"));
        call("'EVAL");
        code(AT);
        call("=");
        compIF();
        // compile a DO-LOOP "I"
        call("COMPILE");
        code(RAT);
        compELSE();
        // Do editor insert comand, redisplay
        call("insert");
        call(".LINE");
        compTHEN();
        code(PSEMI);
        immed();

        // FINDBUF  ( -- a)
        //   Returns the address of the find buffer,
        //   which holds a counted string.
        header("FINDBUF");
        call("HERE");
        literal(160);
        call("+");
        code(PSEMI);

        // in-line?  ( a -- T | F )
        //   Returns TRUE if address a is within the searchable
        //   part of the current line.
        header("in-line?");
        call("CURSOR");
        literal(0xFFFFFFC0);
        code(ANDD);
        call("FINDBUF");
        code(AT);
        call("-");
        literal(65);
        call("+");
        call("<");
        code(PSEMI);

        // found?  ( a -- T | F )
        //   Returns TRUE if strings at a and FINDBUF match
        header("found?");
        call("FINDBUF");
        call("COUNT");
        call("SAME?");
        code(TOR);
        code(DROP);
        code(DROP);
        code(RFROM);
        call("NOT");
        code(PSEMI);

        // get-find-text  ( -- )
        //   Stores input text (if any) at FINDBUF.
        header("get-find-text");
        literal(94);
        call("WORD");
        code(DUPP);
        code(AT);    // WORD found something?
        compIF();
        call("FINDBUF");          // Copy to FINDBUF, including
        code(OVER);
        code(AT);     // everything, including
        code(INC);
        call("CMOVE"); // count
        compTHEN();
        code(PSEMI);

        // locate  ( <text> -- )
        //   Searches for <text> on the current line, adjusting
        //   CURSOR if found.
        header("locate");
        call("get-find-text");
        call("CURSOR");
        compBEGIN();
        code(DUPP);          // -- cur cur
        call("in-line?");   // -- cur flag1
        code(OVER);         // -- cur flag1 cur
        call("found?");     // -- cur flag1 flag2
        call("NOT");        // -- cur flag1 ~flag2
        code(ANDD);         // -- cur flag
        compWHILE();
        code(INC);          // -- cur = cur + 1
        compREPEAT();

        code(DUPP);          // -- cur cur
        call("in-line?");   // -- cur flag1
        code(OVER);         // -- cur flag1 cur
        call("found?");     // -- cur flag1 flag2
        code(ANDD);         // -- cur flag
        compIF();
        literal(BLKLEN);
        call("MOD");
        call("FINDBUF");
        code(AT);
        call("+");      // move cur past found string
        call("CURPOS");
        code(STORE);
        compELSE();
        code(DROP);
        call(".\"|");
        strlit(" none ");
        compTHEN();
        code(PSEMI);

        // F  ( <text> -- )
        //   Locates text then redisplays the line.
        header("F");
        call("locate");
        call(".LINE");
        code(PSEMI);

        // erase  ( -- )
        //   Erases the same number of character as found in
        //   the find buffer from the current CURSOR to the
        //   left, sliding the rest of the current line down.
        header("erase");
        call("FINDBUF");
        code(AT);
        code(DUPP);
        compIF();
        call("-SLIDE");
        compELSE();
        code(DROP);
        compTHEN();
        code(PSEMI);

        // E  ( -- )
        //   Erases the found text then redisplays the line.
        header("E");
        call("erase");
        call(".LINE");
        code(PSEMI);

        // D  ( <text> -- )
        //   Finds and erases the given text (or whatever
        //   is in the find buffer).
        header("D");
        call("locate");
        call("E");
        code(PSEMI);

        // R  ( <text> -- )
        //   Erases the previously found text, and replaces it
        //   with the given <text>.  Finally the line is redisplayed.
        header("R");
        call("erase");
        call("I");
        code(PSEMI);

//4 PSHP-2 Tagged block comment:
//4 PSHP-2/*        // EVALUATE    ( a -- )
//4 PSHP-2        // Evaluates text at address.  Useful for compiling
//4 PSHP-2        // from a file or disk block.
//4 PSHP-2
//4 PSHP-2        header("EVALUATE"); call("#TIB"); code(AT);
//4 PSHP-2                            code(TOR);              // save #TIB
//4 PSHP-2                            literal(BLKLEN); call("#TIB");
//4 PSHP-2                            code(STORE);            // set #TIB to file length
//4 PSHP-2                            call("TIB"); code(TOR); // save TIB address
//4 PSHP-2                            call("#TIB"); code(INC);
//4 PSHP-2                            code(STORE);            // point TIB to IO buffer
//4 PSHP-2                            call(">IN"); code(DUPP);
//4 PSHP-2                            code(AT); code(TOR);    // save >IN
//4 PSHP-2                            literal(0); code(SWAP);
//4 PSHP-2                            code(STORE);            // clear >IN
//4 PSHP-2                            call("EVAL");           // do evaluation
//4 PSHP-2                            code(RFROM); call(">IN");
//4 PSHP-2                            code(STORE);            // restore >IN
//4 PSHP-2                            code(RFROM); call("#TIB");
//4 PSHP-2                            code(INC); code(STORE); // restore TIB
//4 PSHP-2                            code(RFROM); call("#TIB");
//4 PSHP-2                            code(STORE);            // restore #TIB
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2        // LOAD  ( blk -- )
//4 PSHP-2        //   Brings in a block containing Forth commands to be
//4 PSHP-2        //   interpreted or compiled.  Will update SCR variable.
//4 PSHP-2        header("LOAD");     call("BLK"); code(AT);
//4 PSHP-2                            code(TOR);              // save current interpreted BLOCK
//4 PSHP-2                            code(DUPP); call("BLK");
//4 PSHP-2                            code(STORE);            // point BLK to a specified BLOCK
//4 PSHP-2                            call("BLOCK");
//4 PSHP-2                            call(".\"|"); strlit("  loading... ");
//4 PSHP-2                            call("EVALUATE");       // interpret BLOCK
//4 PSHP-2                            code(RFROM); call("BLK");
//4 PSHP-2                            code(STORE);            // restore previous BLK value
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2*/

//4 PSHP-2 Moved forward to integrate with file input words:
//4 PSHP-2        // NESTINPUT    ( a len blk -- )
//4 PSHP-2        // Nests the current input source and interprets/compiles
//4 PSHP-2        // the new input source specified by a len and blk
//4 PSHP-2
//4 PSHP-2        header("NESTINPUT");
//4 PSHP-2                            call("BLK"); code(AT); code(TOR);
//4 PSHP-2                            call("BLK"); code(STORE);
//4 PSHP-2                            call("#TIB"); code(AT);
//4 PSHP-2                            code(TOR);              // save #TIB
//4 PSHP-2                            call("#TIB");
//4 PSHP-2                            code(STORE);            // set #TIB
//4 PSHP-2                            call("TIB"); code(TOR); // save TIB address
//4 PSHP-2                            call("#TIB"); code(INC);
//4 PSHP-2                            code(STORE);            // point TIB to IObuffer
//4 PSHP-2                            call(">IN"); code(DUPP);
//4 PSHP-2                            code(AT); code(TOR);    // save >IN
//4 PSHP-2                            literal(0); code(SWAP);
//4 PSHP-2                            code(STORE);            // clear >IN
//4 PSHP-2//1 PSHP                            call("EVAL");           // do evaluation
//4 PSHP-2                            literal(doFind("EVAL")); call("CATCH");  //1 PSHP
//4 PSHP-2                            code(RFROM); call(">IN");
//4 PSHP-2                            code(STORE);            // restore >IN
//4 PSHP-2                            code(RFROM); call("#TIB");
//4 PSHP-2                            code(INC); code(STORE); // restore TIB
//4 PSHP-2                            code(RFROM); call("#TIB");
//4 PSHP-2                            code(STORE);            // restore #TIB
//4 PSHP-2// Preston's ANS Fixes:
//4 PSHP-2//                            code(RFROM); call("BLK");
//4 PSHP-2//                            code(STORE);
//4 PSHP-2                            code(RFROM);
//4 PSHP-2                            code(DUPP); compIF();
//4 PSHP-2                              code(DUPP); call("BLOCK");
//4 PSHP-2                              call("#TIB"); code(INC);
//4 PSHP-2                              code(STORE);
//4 PSHP-2                            compTHEN();
//4 PSHP-2                            call("BLK"); code(STORE);
//4 PSHP-2                            call("THROW");  //1 PSHP
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2        // LOAD  ( blk -- )
//4 PSHP-2        //   Brings in a block containing Forth commands to be
//4 PSHP-2        //   interpreted or compiled.  Will update SCR variable.
//4 PSHP-2        header("LOAD");     code(DUPP);
//4 PSHP-2                            call("BLOCK");
//4 PSHP-2                            literal(BLKLEN);
//4 PSHP-2                            code(ROT);
//4 PSHP-2//4 CMJ                            call(".\"|"); strlit("  loading... ");
//4 PSHP-2// CMJ Added screen-refresh
//4 PSHP-2                            literal(-1); code(SCRNREF);   // force a repaint of screen
//4 PSHP-2                            call("NESTINPUT");       // interpret BLOCK
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2        // EVALUATE ( a len -- )
//4 PSHP-2        // Nest current input source and interpret/compile
//4 PSHP-2        // from string specified by a len.
//4 PSHP-2        header("EVALUATE"); literal(0); call("NESTINPUT");
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2
//4 PSHP-2// Preston's ANS fixes:
//4 PSHP-2
//4 PSHP-2//        // THRU  ( n1 n2 -- )  LOADS blocks from "n1" to "n2", inclusive.
//4 PSHP-2//        header("THRU");     code(OVER); call("-");
//4 PSHP-2//                            compFOR();
//4 PSHP-2//                                code(DUPP); call("LOAD");
//4 PSHP-2//                                code(INC);
//4 PSHP-2//                            compNEXT(); code(DROP);
//4 PSHP-2//                            code(PSEMI);
//4 PSHP-2
//4 PSHP-2        // THRU  ( n1 n2 -- )  LOADS blocks from "n1" to "n2", inclusive.
//4 PSHP-2        header("THRU");     compBEGIN();
//4 PSHP-2                                code(TOR); code(TOR);
//4 PSHP-2                                code(RAT); call("LOAD");
//4 PSHP-2                                code(RFROM); code(INC); code(RFROM);
//4 PSHP-2                                call("2DUP"); code(SWAP); call("<");
//4 PSHP-2                            compUNTIL();
//4 PSHP-2                            call("2DROP");
//4 PSHP-2                            code(PSEMI);
//4 PSHP-2

        // INDEX ( n1 n2 -- )  displays first line of all
        //   blocks from "n1" to "n2", inclusive.
        header("INDEX");
        code(OVER);
        call("-");
        compFOR();
//                                literal(0); code(SCRNREF);
        call("CR");
        code(DUPP);
        literal(2);
        call("U.R");
        call(".\"|");
        strlit(" : ");
        code(DUPP);
        call("BLOCK");
        literal(64);
        call("TYPE");
//                                literal(-1); code(SCRNREF);
        code(INC);
        compNEXT();
        code(DROP);
        code(PSEMI);

//4 PSHP-2 Moved forward with LOAD
//4 PSHP-2        // OK  ( -- )  Reloads the most recently read block.
//4 PSHP-2        header("OK");       call("CURBLK"); code(AT);
//4 PSHP-2                            call("LOAD"); code(PSEMI);

        // N  ( -- )  Makes the next block current.
        header("N");
        literal(1);
        call("CURBLK");
        call("+!");
        code(PSEMI);

        // B  ( -- )  Makes the previous block (one back) current.
        header("B");
        literal(-1);
        call("CURBLK");
        call("+!");
        code(PSEMI);

        // COPY  ( n1 n2 -- )  copies block n1 to n2.
        header("COPY");
        code(SWAP);
        call("BLOCK");
        code(SWAP);
        call("BLOCK");
        literal(BLKLEN);
        call("CMOVE");
        call("UPDATE");
        code(PSEMI);


        /////////////////////////////////////////////////////////////
        // Additional words from ANS Core word set
        /////////////////////////////////////////////////////////////

//1 PSHP-5        // 0=  ( x -- flag ) flag is true if x = zero.
//1 PSHP-5        header("0=");       literal(0); call("=");
//1 PSHP-5                            code(PSEMI);

        // 2*  ( x1 -- x2 )
        // x2 is the result of shifting x1 one bit toward the
        // most-significant bit, filling the vacated least-significant
        // bit with zero.
        header("2*");
        literal(2);
        call("*");
        code(PSEMI);

        // 2/  ( x1 -- x2 )
        // x2 is the result of shifting x1 one bit toward the
        // least-significant bit, leaving the most-significant
        // bit unchanged.
        header("2/");
        literal(2);
        call("/");
        code(PSEMI);

        // 2SWAP  ( x1 x2 x3 x4 -- x3 x4 x1 x2 )
        // exchange the top two cell pairs.
        header("2SWAP");
        code(ROT);
        code(TOR);
        code(ROT);
        code(RFROM);
        code(PSEMI);

        // 2OVER  ( x1 x2 x3 x4 -- x1 x2 x3 x4 x1 x2 )
        // copy cell pair x1 x2 to the top of the stack.
        header("2OVER");
        code(TOR);
        code(TOR);
        call("2DUP");
        code(RFROM);
        code(RFROM);
        call("2SWAP");
        code(PSEMI);

        // >  ( n1 n2 -- flag )
        // flag is true if and only if n1 is greater than n2.
        header(">");
        code(SWAP);
        call("<");
        code(PSEMI);

        // >BODY  ( xt -- a-addr )
        // a-addr is the data field address corresponding to xt.
        header(">BODY");
        code(INC);
        code(PSEMI);

        // >NUMBER  ( ud1 c-addr1 u1 -- ud2 c-addr2 u2 )
        // ud2 is the unsigned result of converting the characters within
        // the string specified by c-addr1 u1 into digits, using the number
        // in BASE, and adding each into ud1 after multiplying ud1 by the
        // number in BASE. Conversion continues left-to-right until a
        // character that is not convertible, including any "+" or "-", is
        // encountered or the string is entirely converted. c-addr2 is the
        // location of the first unconverted character or the first
        // character past the end of the string if the string was entirely
        // converted. u2 is the number of unconverted characters in
        // the string.
        header(">NUMBER");
        code(DUPP);
        compIF();
        code(DUPP);
        code(DEC);
        compFOR();
        code(TOR);
        code(DUPP);
        code(TOR);
        code(CAT);
        call("BASE");
        code(AT);
        call("DIGIT?");
        compIF();
        code(SWAP);
        call("BASE");
        code(AT);
        code(UMSTA);
        code(DROP);
        code(ROT);
        call("BASE");
        code(AT);
        code(UMSTA);
        call("D+");
        code(RFROM);
        code(INC);
        code(RFROM);
        code(DEC);
        compELSE();
        code(DROP);
        code(RFROM);
        code(RFROM);
        code(RFROM);
        code(DROP);
        code(EXIT);
        compTHEN();
        compNEXT();
        compTHEN();
        code(PSEMI);

        // ACCEPT  ( c-addr +n1 -- +n2 )
        // receive a string of at most +n1 characters. Input terminates when
        // a termination character is received. +n2 is the length of the
        // string stored at c-addr.
        header("ACCEPT");
        call("accept");
        code(SWAP);
        code(DROP);
        code(PSEMI);

        // ALIGN  (  --  )
        // if the data-space pointer is not aligned, reserve
        // enough space to align it.
        header("ALIGN");
        code(PSEMI);
        immed();

        // C,  ( char --  )
        // reserve space for one character in the data space
        // and store char in the space.
        header("C,");
        call(",");
        code(PSEMI);

        // CHAR  ( "<spaces>name" -- char)
        // skip leading space delimiters. Parse name delimited by a space.
        // Put the value of its first character on to the stack.
        header("CHAR");
        call("BL");
        call("WORD");
        code(INC);
        code(CAT);
        code(PSEMI);

        // CHAR+  ( c-addr1 -- c-addr2 )
        // add the size in address units of a character to c-addr1 giving
        // c-addr2.
        header("CHAR+");
        code(INC);
        code(PSEMI);

        // CHARS  ( n1 -- n2 )
        // n2 is the size in address units of n1 characters.
        header("CHARS");
        code(PSEMI);
        immed();

//1 PSHP        //  doCON   ( -- )
//1 PSHP        //  Run time action of CONSTANT
//1 PSHP        header("doCON");    code(RFROM); code(AT);
//1 PSHP                            code(PSEMI); compo();

        // CONSTANT  ( x "<spaces>name" --  )
        // skip leading space delimiters. Parse name delimited by a space.
        // Create a new definition for name. When name is executed x is
        // placed on the stack.
        header("CONSTANT");
        call("TOKEN");
        call("$,n");
        call("OVERT");
        call("COMPILE");
        code(doCON);
        call(",");  //1 PSHP
        code(PSEMI);

        // FIND  ( c-addr -- c-addr 0 | xt 1 | xt -1 )
        // find the definition named in the counted string at c-addr.
        // If the definition is not found , return c-addr and zero.
        // If the definition is found, return its execution token xt.
        // If the definition is immediate, also return one, otherwise
        // return minus-one.
//1 PSHP-5        header("FIND");     call("NAME?"); code(DUPP);
//1 PSHP-5                            compIF();
//1 PSHP-5                              code(AT); literal(0x80); code(ANDD);
//1 PSHP-5                              compIF();
//1 PSHP-5                                literal(1);
//1 PSHP-5                              compELSE();
//1 PSHP-5                                literal(-1);
//1 PSHP-5                              compTHEN();
//1 PSHP-5                            compTHEN();
        header("FIND");
        call("NAME?");
        call("IMMEDIATE?");        //1 PSHP-5
        code(PSEMI);

        // FM/MOD  ( d1 n1 -- n2 n3)
        // divide d1 by n1, giving the floored quotient n3 and the
        // remainder n2.
        header("FM/MOD");
        call("M/MOD");
        code(PSEMI);

        // INVERT  ( x1 -- x2 )
        // invert all the bits of x1, giving its logical inverse x2.
        header("INVERT");
        call("NOT");
        code(PSEMI);

        // J  (  -- n|u )
        // n|u is a copy of the next-outer loop index.
        header("J");
        code(RFROM);
        code(RFROM);
        code(RFROM);
        code(RAT);
        code(SWAP);
        code(TOR);
        code(SWAP);
        code(TOR);
        code(SWAP);
        code(TOR);
        code(PSEMI);

        // LSHIFT  ( x1 u -- x2 )
        // perform a logical left shift of u bit places on x1 giving x2.
        header("LSHIFT");
        literal(31);
        code(ANDD);
        call("?DUP");
        compIF();
        code(DEC);
        compFOR();
        call("2*");
        compNEXT();
        compTHEN();
        code(PSEMI);

        // MOVE  ( addr1 addr2 u --  )
        // if u is greater than zero, copy the contents of u consecutive
        // units at addr1 to the u consecutive address units at addr2.
        header("MOVE");
        call("?DUP");
        compIF();
        code(TOR);
        call("2DUP");
        call("U<");
        compIF();
        code(RFROM);
        call("CMOVE>");
        compELSE();
        code(RFROM);
        call("CMOVE");
        compTHEN();
        compELSE();
        call("2DROP");
        compTHEN();
        code(PSEMI);

        // POSTPONE  ( "<spaces>name" --  )
        // skip leading space delimiters. Parse name delimited by a space.
        // Find name. Append the compilation semantics of name to the
        // current definition.
        header("POSTPONE");
        call("BL");
        call("WORD");
        call("FIND");
        call("?DUP");
        call("0=");
        compIF();
        call("MISSING");  //1 PSHP
        compTHEN();
        code(DEC);
        compIF();
        call("COMPILE");
        call("COMPILE");
        compTHEN();
        call(",");
        code(PSEMI);
        immed();

        // RSHIFT  ( x1 u -- x2 )
        // perform a logical right shift of u bit-places on x1 giving x2.
        header("RSHIFT");
        literal(31);
        code(ANDD);
        call("?DUP");
        compIF();
        code(SWAP);
        call("2/");
        literal(0x7FFFFFFF);
        code(ANDD);
        code(SWAP);
        code(DEC);
        call("?DUP");
        compIF();
        code(DEC);
        compFOR();
        call("2/");
        compNEXT();
        compTHEN();
        compTHEN();
        code(PSEMI);

        // S"
        // compilation: ( "ccc<quote>" --  ) parse ccc delimited by "
        // run-time: (  -- c-addr u ) c-addr u describe a string consisting
        // of the characters ccc.
        header("S\"");
        call("$\"");
        call("COMPILE");
        call("COUNT");
        code(PSEMI);
        immed();

        // S>D  ( n -- d )
        // convert the number n to the double-cell number d with the
        // same value.
        header("S>D");
        code(DUPP);
        call("0<");
        code(PSEMI);

        // SM/REM  ( d1 n1 -- n2 n3 )
        // divide d1 by n1 giving the symmetric quotient n3 and the
        // remainder n2.
        header("SM/REM");
        code(OVER);
        code(TOR);
        call("2DUP");
        code(XORR);
        code(TOR);
        code(TOR);
        code(DUPP);
        call("0<");
        compIF();
        call("DNEGATE");
        compTHEN();
        code(RFROM);
        call("ABS");
// Correction from Preston:
//                          call("M/MOD");
        call("UM/MOD");
        code(RFROM);
        call("0<");
        compIF();
        call("NEGATE");
        compTHEN();
        code(SWAP);
        code(RFROM);
        call("0<");
        compIF();
        call("NEGATE");
        compTHEN();
        code(SWAP);
        code(PSEMI);

        // SOURCE  (  -- c-addr u )
        // c-addr is the address of, and u is the number of characters in,
        // the input buffer.
        header("SOURCE");
        call("TIB");
        call("#TIB");
        code(AT);
        code(PSEMI);

        // [']
        // compilation:  ( "<spaces>name" --  )
        //   skip leading space delimiters. Parse name delimited by a
        //   space. Find name.
        // execution:    (  -- xt )
        //   place name's execution token xt on the stack.
        header("[\']");
        call("\'");
        call("LITERAL");
        code(PSEMI);
        immed();

        // [CHAR]
        // compilation:  ( "<spaces>name" --  )
        //   skip leading space delimiters. Parse name delimited by a
        //   space.
        // execution:    (  -- char )
        //   place char, the value of the first character of name, on the
        //   stack.
        header("[CHAR]");
        call("CHAR");
        call("LITERAL");
        code(PSEMI);
        immed();

        // Environmental Query Keywords                              //1 PSHP-5

        // The following three Java variables are used to manipulate the
        // dictionary link fields to give the effect of the environmental
        // query keyword (EQK) definitions being compiled into a separate
        // word list:

        int lastForth; // saves the name address of the last Forth header
        // before the start of the EQK headers.
        int firstEQK;  // saves the name address of the first EQK header.
        int lastEQK;   // saves the name address of the last EQK header.

        lastForth = _LINK;
        header("/COUNTED-STRING");
        code(doCON);
        comma(0xFFFFFFFF);
        firstEQK = _LINK;
        header("/HOLD");
        code(doCON);
        comma(PAD_OFFSET);
        header("/PAD");
        code(doCON);
        comma(PAD_SIZE);
        header("ADDRESS-UNIT-BITS");
        code(doCON);
        comma(32);
        header("CORE");
        code(doCON);
        comma(TRUE);
        header("CORE-EXT");
        code(doCON);
        comma(FALSE);
        header("FLOORED");
        code(doCON);
        comma(TRUE);
//4 CMJ        header("MAX-CHAR");           code(doCON); comma(MAX_CHAR);
        header("MAX-CHAR");
        code(doCON);
        comma(127); //4 CMJ
        header("MAX-D");
        literal(0xFFFFFFFF);
        literal(0x7FFFFFFF);
        code(PSEMI);
        header("MAX-N");
        code(doCON);
        comma(0x7FFFFFFF);
        header("MAX-U");
        code(doCON);
        comma(0xFFFFFFFF);
        header("MAX-UD");
        literal(0xFFFFFFFF);
        literal(0xFFFFFFFF);
        code(PSEMI);
        header("RETURN-STACK-CELLS");
        code(doCON);
        comma(RTS - TIB_SIZE);
        header("STACK-CELLS");
        code(doCON);
        comma(SPP - BSPP);
        header("BLOCK");
        code(doCON);
        comma(TRUE);
        header("BLOCK-EXT");
        code(doCON);
        comma(FALSE);
        header("EXCEPTION");
        code(doCON);
        comma(TRUE);
        header("EXCEPTION-EXT");
        code(doCON);
        comma(TRUE);
        header("TOOLS");
        code(doCON);
        comma(TRUE);
        header("TOOLS-EXT");
        code(doCON);
        comma(FALSE);

        lastEQK = _LINK;       // Becomes the top pointer in the EQK word list - see below.
        forthStack[firstEQK - 1] = 0;   // Disconnect the EQK word list from the Forth word list.
        _LINK = lastForth;     // Connect future headers to the Forth word list.

        //  EQK-WORDLIST (  -- wid )                                 //1 PSHP-5
        //  Word list containing the environmental query keywords.
        header("EQK-WORDLIST");
        call("doVAR");
        comma(lastEQK);
        comma(0);

        //  ENVIRONMENT? ( c-addr u -- false | i*x true )            //1 PSHP-5
        //  c-addr u is the address and length of a string containing
        //  an environmental query keyword to be checked for
        //  correspondence with an attribute of the present environment.
        //  If the attribute is unknown the returned flag is false,
        //  otherwise the flag is true and i*x is of the type specified
        //  for the attribute queried.
        header("ENVIRONMENT?");
        call("EQK-WORDLIST");
        call("SEARCH-WORDLIST");
        code(DUPP);
        compIF();
        code(DROP);
        code(EXECU);
        literal(-1);
        compTHEN();
        code(PSEMI);

        /////////////////////////////////////////////////////////////
        //  Useful Non-ANS Words
        /////////////////////////////////////////////////////////////
        // ON  ( a --  )
        //   Write a TRUE (integer -1) to the cell with address a.
        header("ON");
        literal(-1);
        code(SWAP);
        code(STORE);
        code(PSEMI);

        // OFF ( a --  )
        //   Write a FALSE (integer 0) to the cell with address a.
        header("OFF");
        literal(0);
        code(SWAP);
        code(STORE);
        code(PSEMI);

//4 PSHP-2        /////////////////////////////////////////////////////////////              //4 PSHP
//4 PSHP-2        //  FILE INPUT                                                             //4 PSHP
//4 PSHP-2        /////////////////////////////////////////////////////////////              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // TOOLONG (  -- n )                                                       //4 PSHP
//4 PSHP-2        // One greater than the maximum line length readable from a source file    //4 PSHP
//4 PSHP-2        header("TOOLONG");  code(doCON);  comma(TOOLONG);                          //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // FIB (  -- a-addr)                                                       //4 PSHP
//4 PSHP-2        // File input buffer                                                       //4 PSHP
//4 PSHP-2        header("FIB");      call("doVAR"); allot(TOOLONG + 2);                     //4 PSHP
//4 PSHP-2
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // R/O     (  -- fam )                                                     //4 PSHP
//4 PSHP-2        // fam is the value for selecting "read only" file access method           //4 PSHP
//4 PSHP-2        header("R/O");      code(doCON);  comma(RSLASHO);                          //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // INTERPRET-FILE  ( i*x fileid -- j*x )                                   //4 PSHP
//4 PSHP-2        // Interpret source code in file identified by fileid.                     //4 PSHP
//4 PSHP-2        header("INTERPRET-FILE");                                                  //4 PSHP
//4 PSHP-2                            code(TOR);                                             //4 PSHP
//4 PSHP-2                            compBEGIN();                                           //4 PSHP
//4 PSHP-2                                call("FIB"); call("TOOLONG");                      //4 PSHP
//4 PSHP-2                                code(RAT); call("READ-LINE");                      //4 PSHP
//4 PSHP-2                                call("abort\""); strlit(" non-existent file");     //4 PSHP
//4 PSHP-2                                compWHILE();                                       //4 PSHP
//4 PSHP-2                                    code(DUPP); call("TOOLONG"); call("=");        //4 PSHP
//4 PSHP-2                                    call("abort\""); strlit(" line too long");     //4 PSHP
//4 PSHP-2                                    call("FIB"); code(SWAP);                       //4 PSHP
//4 PSHP-2                                    call("EVALUATE");                              //4 PSHP
//4 PSHP-2                            compREPEAT();                                          //4 PSHP
//4 PSHP-2                            code(RFROM); code(DROP);                               //4 PSHP
//4 PSHP-2                            code(PSEMI);                                           //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // INCLUDE-FILE  ( i*x fileid -- j*x )                                     //4 PSHP
//4 PSHP-2        // Save input source specification, interpret source code in file          //4 PSHP
//4 PSHP-2        // identified by fileid, close file and restore saved input source         //4 PSHP
//4 PSHP-2        // specification.                                                          //4 PSHP
//4 PSHP-2        // NOTE: saving and restoring of input source specification is not         //4 PSHP
//4 PSHP-2        // yet implemented.                                                        //4 PSHP
//4 PSHP-2        header("INCLUDE-FILE");                                                    //4 PSHP
//4 PSHP-2                            // save input source specification here                //4 PSHP
//4 PSHP-2                            code(DUPP); code(TOR);                                 //4 PSHP
//4 PSHP-2                            literal(doFind("INTERPRET-FILE")); call("CATCH");      //4 PSHP
//4 PSHP-2                            code(RFROM); call("CLOSE-FILE");                       //4 PSHP
//4 PSHP-2                            // restore input source specification here             //4 PSHP
//4 PSHP-2                            code(SWAP); call("THROW");                             //4 PSHP
//4 PSHP-2                            call("abort\""); strlit(" non-existent file");         //4 PSHP
//4 PSHP-2                            code(PSEMI);                                           //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // INCLUDED  ( i*x c-addr u -- j*x )                                       //4 PSHP
//4 PSHP-2        // Save input source specification, open file named in string              //4 PSHP
//4 PSHP-2        // c-addr u, interpret source code in file, close file and                 //4 PSHP
//4 PSHP-2        // restore saved input source specification.                               //4 PSHP
//4 PSHP-2        // NOTE: saving and restoring of input source specification is not         //4 PSHP
//4 PSHP-2        // yet implemented - see INCLUDE-FILE.                                     //4 PSHP
//4 PSHP-2        header("INCLUDED");                                                        //4 PSHP
//4 PSHP-2                            call("R/O"); call("OPEN-FILE");                        //4 PSHP
//4 PSHP-2                            code(DUPP); literal(-38); call("=");                   //4 PSHP
//4 PSHP-2                            call("abort\""); strlit(" non-existent file");         //4 PSHP
//4 PSHP-2                            call("abort\""); strlit(" file I/O exception");        //4 PSHP
//4 PSHP-2                            call("INCLUDE-FILE");                                  //4 PSHP
//4 PSHP-2                            code(PSEMI);                                           //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2        // INCLUDE   ( i*x "<spaces>name" -- j*x )                                 //4 PSHP
//4 PSHP-2        // Save input source specification, open file identified by                //4 PSHP
//4 PSHP-2        // name, interpret source code in file, close file and                     //4 PSHP
//4 PSHP-2        // restore saved input source specification.                               //4 PSHP
//4 PSHP-2        // NOTE: saving and restoring of input source specification is not         //4 PSHP
//4 PSHP-2        // yet implemented - see INCLUDE-FILE.                                     //4 PSHP
//4 PSHP-2        header("INCLUDE");                                                         //4 PSHP
//4 PSHP-2                            call("BL"); call("WORD"); call("COUNT");               //4 PSHP
//4 PSHP-2                            call("INCLUDED");                                      //4 PSHP
//4 PSHP-2                            code(PSEMI);                                           //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2

        /////////////////////////////////////////////////////////////              //4 PSHP
        //  INPUT SOURCES                                                          //4 PSHP-2
        /////////////////////////////////////////////////////////////              //4 PSHP
        //4 PSHP
        // SOURCE-ID (  -- n )                                                     //4 PSHP-2
        // Identifies input source:                                                //4 PSHP-2
        //    -1   = EVALUATEd string                                              //4 PSHP-2
        //     0   = terminal or BLOCK                                             //4 PSHP-2
        //   other = fileid of text file                                           //4 PSHP-2
        header("SOURCE-ID");
        call("SID");
        code(AT);
        code(PSEMI);                  //4 PSHP-2
        //4 PSHP-2
        // FIBLENGTH (  -- n )                                                     //4 PSHP-2
        // One greater than the maximum line length readable from a source file    //4 PSHP
        header("FIBLENGTH");
        code(doCON);
        comma(FIBLENGTH);                      //4 PSHP-2
        //4 PSHP
        // FIB (  -- c-addr)                                                       //4 PSHP-2
        // File input buffer                                                       //4 PSHP
        header("FIB");
        call("doVAR");
        allot(FIBLENGTH);                       //4 PSHP-2

        // SAVE-SOURCE ( --  ) ( R:  -- c-addr u n1 n2 n3 )                        //4 PSHP-2
        // Save parameters specifying current input source on return stack:        //4 PSHP-2
        //    c-addr = address of buffer                                           //4 PSHP-2
        //       u   = length of data                                              //4 PSHP-2
        //       n1  = block number                                                //4 PSHP-2
        //       n2  = SOURCE-ID                                                   //4 PSHP-2
        //       n3  = value of >IN                                                //4 PSHP-2
        header("SAVE-SOURCE");                                                     //4 PSHP-2
        code(RFROM);                                           //4 PSHP-2
        call("TIB");                                           //4 PSHP-2
        call("#TIB");
        code(AT);                                //4 PSHP-2
        call("BLK");
        code(AT);                                 //4 PSHP-2
        call("SOURCE-ID");                                     //4 PSHP-2
        call(">IN");
        code(AT);                                 //4 PSHP-2
        code(TOR);
        code(TOR);
        code(TOR);                       //4 PSHP-2
        code(TOR);
        code(TOR);                                  //4 PSHP-2
        code(TOR);                                             //4 PSHP-2
        code(PSEMI);                                           //4 PSHP-2
        //4 PSHP-2
        // SET-SOURCE ( c-addr u n1 n2 n3 --  )                                    //4 PSHP-2
        // Set parameters to specify input source:                                 //4 PSHP-2
        //    c-addr = address of buffer                                           //4 PSHP-2
        //       u   = length of data                                              //4 PSHP-2
        //       n1  = block number                                                //4 PSHP-2
        //       n2  = SOURCE-ID                                                   //4 PSHP-2
        //       n3  = value of >IN                                                //4 PSHP-2
        header("SET-SOURCE");                                                      //4 PSHP-2
        call(">IN");
        code(STORE);                              //4 PSHP-2
        call("SID");
        code(STORE);                              //4 PSHP-2
        call("BLK");
        code(STORE);                              //4 PSHP-2
        call("#TIB");
        code(STORE);                             //4 PSHP-2
        call("#TIB");
        code(INC);
        code(STORE);                  //4 PSHP-2
        code(PSEMI);                                           //4 PSHP-2
        //4 PSHP-2
        // RESTORE-SOURCE ( -- ) ( R: c-addr u n1 n2 n3 --  )                      //4 PSHP-2
        // Restore input source from parameters on return stack:                   //4 PSHP-2
        //    c-addr = address of buffer                                           //4 PSHP-2
        //       u   = length of data                                              //4 PSHP-2
        //       n1  = block number                                                //4 PSHP-2
        //       n2  = SOURCE-ID                                                   //4 PSHP-2
        //       n3  = value of >IN                                                //4 PSHP-2
        header("RESTORE-SOURCE");                                                  //4 PSHP-2
        code(RFROM);                                           //4 PSHP-2
        code(RFROM);
        code(RFROM);                              //4 PSHP-2
        code(RFROM);
        code(RFROM);
        code(RFROM);                 //4 PSHP-2
        call("SET-SOURCE");                                    //4 PSHP-2
        call("SOURCE-ID");
        literal(0);
        call(">");              //4 PSHP-2
        compIF();                                              //4 PSHP-2
        call("FIB");
        call("SOURCE-ID");
        code(REPEATLINE);  //4 PSHP-2
        call("THROW");
        code(DROP);                         //4 PSHP-2
        compELSE();                                            //4 PSHP-2
        call("BLK");
        code(AT);
        call("?DUP");               //4 PSHP-2
        compIF();                                          //4 PSHP-2
        call("BLOCK");                                 //4 PSHP-2
        call("#TIB");
        code(INC);
        code(STORE);          //4 PSHP-2
        compTHEN();                                        //4 PSHP-2
        compTHEN();                                            //4 PSHP-2
        code(TOR);                                             //4 PSHP-2
        code(PSEMI);                                           //4 PSHP-2
        //4 PSHP-2
        // NEST-INPUT    ( c-addr u n1 n2 -- )                                     //4 PSHP-2
        // Nests the current input source and interprets/compiles                  //4 PSHP-2
        // the new input source specified by the input parameters:                 //4 PSHP-2
        //    c-addr = address of buffer                                           //4 PSHP-2
        //       u   = length of data                                              //4 PSHP-2
        //       n1  = block number                                                //4 PSHP-2
        //       n2  = SOURCE-ID                                                   //4 PSHP-2
        header("NEST-INPUT");                                                      //4 PSHP-2
        call("SAVE-SOURCE");                                   //4 PSHP-2
        literal(0);
        call("SET-SOURCE");                        //4 PSHP-2
        literal(doFind("EVAL"));
        call("CATCH");                //4 PSHP-2
        call("RESTORE-SOURCE");                                //4 PSHP-2
        call("THROW");                                         //4 PSHP-2
        code(PSEMI);                                           //4 PSHP-2

//4 PSHP-2 Moved from further back:
        // LOAD  ( blk -- )
        //   Brings in a block containing Forth commands to be
        //   interpreted or compiled.  Will update SCR variable.
        header("LOAD");
        code(DUPP);
        call("BLOCK");
        literal(BLKLEN);
        code(ROT);
//4 CMJ                            call(".\"|"); strlit("  loading... ");
// CMJ Added screen-refresh
        literal(-1);
        code(SCRNREF);   // force a repaint of screen
//4 PSHP-2                            call("NESTINPUT");       // interpret BLOCK
        literal(0);
        call("NEST-INPUT");                        //4 PSHP-2
        code(PSEMI);

        // EVALUATE ( a len -- )
        // Nest current input source and interpret/compile
        // from string specified by a len.
//4 PSHP-2        header("EVALUATE"); literal(0); call("NESTINPUT");
        header("EVALUATE");
        literal(0);
        literal(-1);
        call("NEST-INPUT");           //4 PSHP-2
        code(PSEMI);

        // THRU  ( n1 n2 -- )  LOAD blocks from "n1" to "n2", inclusive.
        header("THRU");
        compBEGIN();
        code(TOR);
        code(TOR);
        code(RAT);
        call("LOAD");
        code(RFROM);
        code(INC);
        code(RFROM);
        call("2DUP");
        code(SWAP);
        call("<");
        compUNTIL();
        call("2DROP");
        code(PSEMI);

        // OK  ( -- )  Reloads the most recently read block.
        header("OK");
        call("CURBLK");
        code(AT);
        call("LOAD");
        code(PSEMI);

        //4 PSHP
        // R/O     (  -- fam )                                                     //4 PSHP
        // fam is the value for selecting "read only" file access method           //4 PSHP
        header("R/O");
        code(doCON);
        comma(RSLASHO);                          //4 PSHP
        //4 PSHP
//4 PSHP-F        // INTERPRET-FILE  ( i*x -- j*x )                                          //4 PSHP-2
//4 PSHP-F        // Interpret source code in file identified by SOURCE-ID.                  //4 PSHP-2
//4 PSHP-F        header("INTERPRET-FILE");                                                  //4 PSHP
//4 PSHP-F                            compBEGIN();                                           //4 PSHP
//4 PSHP-F                                call("TIB"); call("FIBLENGTH");                    //4 PSHP-2
//4 PSHP-F                                call("SOURCE-ID"); call("READ-LINE");              //4 PSHP-2
//4 PSHP-F                                call("abort\""); strlit(" non-existent file");     //4 PSHP
//4 PSHP-F                                compWHILE();                                       //4 PSHP
//4 PSHP-F                                    code(DUPP); call("FIBLENGTH"); call("=");      //4 PSHP
//4 PSHP-F                                    call("abort\""); strlit(" line too long");     //4 PSHP
//4 PSHP-F                                    call("#TIB"); code(STORE);                     //4 PSHP-2
//4 PSHP-F                                    call(">IN"); call("OFF"); call("EVAL");        //4 PSHP-2
//4 PSHP-F                            compREPEAT();                                          //4 PSHP
//4 PSHP-F                            code(DROP);                                            //4 PSHP-2
//4 PSHP-F                            code(PSEMI);                                           //4 PSHP
//4 PSHP-F                                                                                   //4 PSHP
        // INTERPRET-FILE  ( i*x -- j*x )                                          //4 PSHP-2
        // Interpret source code in file identified by SOURCE-ID.                  //4 PSHP-2
        header("INTERPRET-FILE");                                                  //4 PSHP
        compBEGIN();                                           //4 PSHP
        call(">IN");
        call("OFF");                          //4 PSHP-F
        call("SOURCE-ID");
        call("LINE-NUMBER");            //4 PSHP-F
        code(DROP);
        call("DEEPLINE");
        code(STORE);         //4 PSHP-F
        call("TIB");
        call("FIBLENGTH");                    //4 PSHP-2
        call("SOURCE-ID");
        call("READ-LINE");              //4 PSHP-2
        call("abort\"");
        strlit(" non-existent file");     //4 PSHP
        compWHILE();                                       //4 PSHP
        code(DUPP);
        call("#TIB");
        code(STORE);         //4 PSHP-F
        call("FIBLENGTH");
        call("=");                  //4 PSHP-F
        call("abort\"");
        strlit(" line too long");     //4 PSHP
        call("EVAL");                                  //4 PSHP-F
        compREPEAT();                                          //4 PSHP
        code(DROP);                                            //4 PSHP-2
        code(PSEMI);                                           //4 PSHP
        //4 PSHP
        // INCLUDE-FILE  ( i*x fileid -- j*x )                                     //4 PSHP
        // Save input source specification, interpret source code in file          //4 PSHP
        // identified by fileid, close file (unless the file has generated an      //4 PSHP-2
        // exception) and restore saved input source specification.                //4 PSHP-2
        header("INCLUDE-FILE");                                                    //4 PSHP
        call("SAVE-SOURCE");                                   //4 PSHP-2
        code(TOR);                                             //4 PSHP-2
        call("FIB");
        call("FIBLENGTH");
        literal(0);            //4 PSHP-2
        code(RFROM);
        literal(0);
        call("SET-SOURCE");           //4 PSHP-2
        literal(doFind("INTERPRET-FILE"));
        call("CATCH");      //4 PSHP
        call("SOURCE-ID");
        call("DEEPSID");
        code(AT);          //4 PSHP-2
        code(XORR);                                            //4 PSHP-2
        compIF();                                              //4 PSHP-2
        call("SOURCE-ID");
        call("CLOSE-FILE");             //4 PSHP-2
        compELSE();                                            //4 PSHP-2
        literal(0);                                        //4 PSHP-2
        compTHEN();                                            //4 PSHP-2
        call("RESTORE-SOURCE");                                //4 PSHP-2
        code(SWAP);
        call("THROW");                             //4 PSHP
        call("abort\"");
        strlit(" non-existent file");         //4 PSHP
        code(PSEMI);                                           //4 PSHP
        //4 PSHP
        // INCLUDED  ( i*x c-addr u -- j*x )                                       //4 PSHP
        // Save input source specification, open file named in string              //4 PSHP
        // c-addr u, interpret source code in file, close file (unless             //4 PSHP-2
        // the file has generated an exception) and restore saved input            //4 PSHP-2
        // source specification.                                                   //4 PSHP-2
        header("INCLUDED");                                                        //4 PSHP
        call("R/O");
        call("OPEN-FILE");                        //4 PSHP
        code(DUPP);
        literal(-38);
        call("=");                   //4 PSHP
        call("abort\"");
        strlit(" non-existent file");         //4 PSHP
        call("abort\"");
        strlit(" file I/O exception");        //4 PSHP
        call("INCLUDE-FILE");                                  //4 PSHP
        code(PSEMI);                                           //4 PSHP
        //4 PSHP
        // INCLUDE   ( i*x "<spaces>name" -- j*x )                                 //4 PSHP
        // Save input source specification, open file identified by                //4 PSHP
        // name, interpret source code in file, close file (unless                 //4 PSHP-2
        // the file has generated an exception) and restore saved input            //4 PSHP-2
        // source specification.                                                   //4 PSHP-2
        header("INCLUDE");                                                         //4 PSHP
        call("BL");
        call("WORD");
        call("COUNT");               //4 PSHP
        call("INCLUDED");                                      //4 PSHP
        code(PSEMI);                                           //4 PSHP
        //4 PSHP
        // OPEN      ( fam "<spaces>name" -- fileid ior )                          //4 PSHP-2
        // Open the file identified by name and return its fileid                  //4 PSHP-2
        // and set the file position to the start of the file. fam (file           //4 PSHP-2
        // access method) must equal R/O as this is the only access                //4 PSHP-2
        // method available.                                                       //4 PSHP-2
        // ior values are:   0 - operation succeeded                               //4 PSHP-2
        //                 -38 - file name not recognised                          //4 PSHP-2
        //                 -37 - any other exception                               //4 PSHP-2
        header("OPEN");                                                            //4 PSHP-2
        call("BL");
        call("WORD");
        call("COUNT");               //4 PSHP-2
        call("ROT");
        code(OPENFILE);                           //4 PSHP-2
        code(PSEMI);                                           //4 PSHP-2

        /////////////////////////////////////////////////////////////
        //  QUIT AND DEFAULT EXCEPTION HANDLING
        /////////////////////////////////////////////////////////////
//1 PSHP moved these definitions here so .LINE can be used when
//1 PSHP an error occurs during a LOAD

//1 PSHP new routine for exception handling
        // EXMESSAGE ( throw-code -- c-addr u true | throw-code false )
        // If an exception message corresponding to throw-code for the
        // current local language is available return address and length
        // with true flag otherwise return throw-code and false flag.
        // NOTE: This routine is intended to be rewritten to return messages
        // in a number of natural languages for the full range of
        // system throw-codes in use; at present it supports only two
        // throw-codes in one language.
        header("EXMESSAGE");
        literal(-9);
        code(OVER);
        call("=");
        compIF();
        code(DROP);
        call("$\"|");
        strlit("invalid memory address");
        call("COUNT");
        literal(-1);
        compELSE();
        literal(-13);
        code(OVER);
        call("=");
        compIF();
        code(DROP);
        call("$\"|");
        strlit("?");
        call("COUNT");
        literal(-1);
        compELSE();
        literal(0);
        compTHEN();
        compTHEN();
        code(PSEMI);

//1 PSHP new routine for exception handling
        // EXDEFAULT  (  -- c-addr u )
        // Return address and length of default exception message
        // in the current local language.
        // NOTE: only one language currently supported.
        header("EXDEFAULT");
        call("$\"|");
        strlit("exception:");
        call("COUNT");
        code(PSEMI);

//1 PSHP new routine for exception handling
        //  EXDISPLAY ( throw-code -- )
        //  Displays exception message corresponding to throw-code
        header("EXDISPLAY");
        call("DEEPBLK");
        code(AT);
        call("?DUP");
        compIF();
        call("CURBLK");
        code(STORE);
        call("DEEPPOS");
        code(AT);
        call("CURPOS");
        code(STORE);
        compTHEN();
        literal(-1);
        code(OVER);
        code(XORR); // ?display error message
        compIF();
        call("ERROR-CLR");
        code(AT);   // show error text color
        call("FG-CLR");
        code(STORE);
        literal(-1);
        code(SCRNREF);
        code(DUPP);
        literal(-13);
        call("=");
        compIF();
        call("SPACE");
        call("NAME$");
        code(AT);
        call("COUNT");
        literal(63);
        code(ANDD);
        call("TYPE");
        compTHEN();
        call("SPACE");
        literal(-2);
        code(OVER);
        call("=");
        compIF();
        code(DROP);
        call("ABORT$");
        code(AT);
        call("COUNT");
        call("TYPE");
        compELSE();
        call("EXMESSAGE");
        compIF();
        call("TYPE");
        compELSE();
        call("EXDEFAULT");
        call("TYPE");
        call(".");
        compTHEN();
        compTHEN();
        call("FORTH-CLR");
        code(AT);      // show Forth text color
        call("FG-CLR");
        code(STORE);
        call("DEEPBLK");
        code(AT);
        call("?DUP");
        compIF();
        call(".\"|");
        strlit("   block");
        call(".");
//4 PSHP-2                                    call(".LINE");
        call(".\"|");
        strlit("  line");               //4 PSHP-2
        call("DEEPPOS");
        code(AT);                    //4 PSHP-2
        literal(64);
        call("/");
        call(".");            //4 PSHP-2
        call(".LINE");                                //4 PSHP-2
        compELSE();                                       //4 PSHP-2
        call("DEEPSID");
        code(AT);                    //4 PSHP-2
        literal(0);
        call(">");                        //4 PSHP-2
        compIF();                                     //4 PSHP-2
        call(".\"|");
        strlit("   ");              //4 PSHP-2
        call("FIB");
        code(DUPP);                  //4 PSHP-2
        call("FIBLENGTH");                        //4 PSHP-2
        call("DEEPSID");
        code(AT);                //4 PSHP-2
        code(FILENAME);
        code(DROP);
        call("TYPE"); //4 PSHP-2
//4 PSHP-F                                        call(".\"|"); strlit("  line");           //4 PSHP-2
//4 PSHP-F                                        call("DEEPSID"); code(AT);                //4 PSHP-2
//4 PSHP-F                                        code(LINENUMBER); code(DROP);             //4 PSHP-2
//4 PSHP-F                                        code(DEC); call(".");                     //4 PSHP-2
        call("DEEPLINE");
        code(AT);               //4 PSHP-F
        code(DUPP);
        literal(0);
        call(">");        //4 PSHP-F
        compIF();                                 //4 PSHP-F
        call(".\"|");
        strlit("  line");       //4 PSHP-F
        code(DUPP);
        call(".");                //4 PSHP-F
        compTHEN();                               //4 PSHP-F
        code(DROP);                               //4 PSHP-F
        call("CR");                               //4 PSHP-2
        call("FIB");
        code(DUPP);                  //4 PSHP-2
        call("DEEPSID");
        code(AT);                //4 PSHP-2
        code(REPEATLINE);
        code(DROP);             //4 PSHP-2
        call("TYPE");                             //4 PSHP-2
        call("CR");                               //4 PSHP-2
        call("DEEPPOS");
        code(AT);                //4 PSHP-2
        call("SPACES");                           //4 PSHP-2
        literal(94);
        call("EMIT");                //4 PSHP-2
        compTHEN();                                   //4 PSHP-2
        call(".LINE");
        compTHEN();
        call("SPACE");
        compELSE();
        code(DROP);
        compTHEN();
//4 PSHP-2                            call("DEEPBLK"); call("OFF");
//4 PSHP-2                            call("DEEPPOS"); call("OFF");
        code(PSEMI);

        //  EXRESET   ( -- )                                                        //4 PSHP-2
        //  Reset the user variables DEEPBLK DEEPPOS and DEEPSID and,               //4 PSHP-2
        //  where appropriate, close the file that caused an exception.             //4 PSHP-2
        //  Programs which intercept an exception thrown during the                 //4 PSHP-2
        //  loading of source code should ensure this word is executed              //4 PSHP-2
        //  before loading continues.                                               //4 PSHP-2
        header("EXRESET");                                                          //4 PSHP-2
        call("DEEPBLK");
        call("OFF");                           //4 PSHP-2
        call("DEEPPOS");
        call("OFF");                           //4 PSHP-2
        call("DEEPSID");
        code(AT);
        code(CLOSEFILE);
        code(DROP); //4 PSHP-2
        call("DEEPSID");
        call("OFF");                           //4 PSHP-2
        code(PSEMI);                                            //4 PSHP-2

// MAL: added code for configurable color text
        //  QUIT    ( -- )
        //  Reset return stack pointer and start text interpreter.
        header("QUIT");
        literal(RZERO);
        code(AT);
        code(RPSTO);    // reset return stack pointer
// CMJ Removed redundant screen-refresh
//                          literal(-1); code(SCRNREF); // force a repaint of screen
        compBEGIN();
        call("DEF-BG-CLR");
        code(AT);      // reset background color
        call("BG-CLR");
        code(STORE);
        call("[");  // start interpretation
        compBEGIN();
        call("USER-CLR");
        code(AT);      // show user text color
        call("FG-CLR");
        code(STORE);
        call("QUERY");                // get input
// CMJ Added screen-refresh
        literal(-1);
        code(SCRNREF);   // force a repaint of screen
        call("FORTH-CLR");
        code(AT);      // show Forth text color
        call("FG-CLR");
        code(STORE);
        literal(doFind("EVAL"));
        call("CATCH");
        code(DUPP);
        code(ZEQUAL);              //5 CMJ
        compIF();                              //5 CMJ
        call("'PROMPT");
        call("@EXECUTE"); //5 CMJ prompt
        compTHEN();                            //5 CMJ
        call("?DUP");   // evaluate input
        compUNTIL();        // continue till error
        call("'PROMPT");
        code(AT);
        code(SWAP);         // save input device
//1 PSHP                                call("CONSOLE"); call("NULL$");
//1 PSHP                                code(OVER);
//1 PSHP                                code(XORR); // ?display error message
//1 PSHP                                compIF();
//1 PSHP									call("ERROR-CLR"); code(AT);	  // show error text color
//1 PSHP                                    call("FG-CLR"); code(STORE);
//1 PSHP                                    literal(-1); code(SCRNREF);
//1 PSHP                                    call("SPACE"); call("COUNT");
//1 PSHP                                    call("TYPE");       // error message
//1 PSHP                                    call(".\"|");
//1 PSHP                                    strlit(" ? ");      // error prompt
//1 PSHP									call("FORTH-CLR"); code(AT);	  // show Forth text color
//1 PSHP                                    call("FG-CLR"); code(STORE);
//1 PSHP                                compTHEN();
        call("CONSOLE");              //1 PSHP
        call("EXDISPLAY");            //1 PSHP
        call("EXRESET");                                    //4 PSHP-2
        literal(doFind(".OK"));
        code(XORR);             // ?file input
        compIF();
        literal(ERR);
        call("EMIT");   // file error, tell host
        compTHEN();
        call("PRESET");     // some cleanup
        compAGAIN();
        code(PSEMI);                          //1 PSHP for benefit of SEE


        /////////////////////////////////////////////////////////////
        //  BOOT
        /////////////////////////////////////////////////////////////

        //  VER     ( -- n )  // VERSN
        //  Return the version number of this implementation.
        header("VER");
        literal(VER * 256 + EXT);
        code(PSEMI);

        //  hi      ( -- )  // HI
        //  Display the sign-on message of eForth.
        header("hi");
        call("!IO");                // initialize I/O
        call("PAGE"); //4 CMJ To overcome timing problem that omits some
        // some characters at startup.
        call(".\"|");
        strlit("NoWebForth");
//        call("BASE");
//        code(AT);    // save radix
//        call("HEX");
//        call("VER");
//        literal(0); // Preston's ANS Fix
//        call("<#");
//        call("#");
//        call("#");
//        literal('.');
//        call("HOLD");
//        call("#S");
//        call("#>");
//        call("TYPE");       // format version number
//        call("BASE");
//        code(STORE);
//        call("CR");
//        call(".\"|");
//        strlit("Copyright 1999 by Losh, Preston and Jakeman, under the GNU GPL.");
//        call("CR");
//        call(".\"|");
//        strlit("PROVIDED AS IS WITHOUT WARRANTY");
        call("CR");
        code(PSEMI);     // restore radix

        //  'BOOT   ( -- a )  // TBOOT
        //  The application startup vector.
        header("'BOOT");
        call("doVAR");
        comma(doFind("hi"));    // application to boot

        //  COLD    ( -- )
        //  The hilevel cold start sequence.
        header("COLD");
        compBEGIN();
        code(UINIT);    // initialize user variables
        call("PRESET"); // initialize stack and TIB
        call("'BOOT");
        call("@EXECUTE");   // application boot
        call("FORTH");
        call("CONTEXT");
        code(AT);
        code(DUPP);     // initialize search order
        call("CURRENT");
        call("2!");
        call("OVERT");
        call("QUIT");   // start interpretation
        compAGAIN();        // just in case

    }

    /////////////////////////////////////////////////////////////
    // These load and run the VM
    /////////////////////////////////////////////////////////////
    public void prepare ()
    {
        loadDictionary();

        ip = doFind("COLD");
        bGone = false;
    }

    public void run ()
    {
        // Inner interpreter loop
        while (!bGone)
        {
            try
            {
                int inst = forthStack[ip++];     // Read current instruction,
                // advance instr. pointer
                if ((forthStack[TRACING] & SHOWING) != 0)
                {
                    inform(inst);       // Report state if tracing
                }
                if (inst > PRIMITIVE)   // Check for primitive bit
                {
                    // Strip off primitive instruction bit,
                    // execute primitive
                    //System.err.print(" "+(inst-PRIMITIVE));
                    doPrim(inst - PRIMITIVE);
                }
                else                    // Nest into colon word
                {
                    rp--;
                    forthStack[rp] = ip;         // save ip for return
                    ip = inst;          // "inst" is word's address
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
//1 PSHP                    app.print(" address");
//1 PSHP                    // Duplicate Forth THROW
//1 PSHP                    rp = forthStack[HANDL];
//1 PSHP                    forthStack[HANDL] = forthStack[rp];
//1 PSHP                    rp++;
//1 PSHP                    sp = forthStack[rp];
//1 PSHP                    rp++;
//1 PSHP                    pop();
//1 PSHP                    push(NULLSTR);  // blank error string
//1 PSHP                    doPrim(EXIT);
                push(-9);              //1 PSHP
                forthStack[--rp] = ip;          //1 PSHP
                ip = doFind("THROW");  //1 PSHP
            }
        } // end while
        System.exit(1); // bGone == TRUE
    }
}

