package noweb;

/**
 * Created by Administrator on 3/13/2017.
 */ //4 PSHP-2/////////////////////////////////////////////////////////////                      //4 PSHP
//4 PSHP-2// Internally held file.                                                           //4 PSHP
//4 PSHP-2/////////////////////////////////////////////////////////////                      //4 PSHP
//4 PSHP-2class WebForthFile {                                                               //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    private byte data[];     // file data                                          //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    private String name;     // file name                                          //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    private int position;    // file position updated by readfile and readLine;    //4 PSHP
//4 PSHP-2                             // invariant: 0 <= position <= data.length            //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    private int lineNumber;  // line number of line identified by position;        //4 PSHP
//4 PSHP-2                             // updated by readLine when valid; invalidated by     //4 PSHP
//4 PSHP-2                             // readFile and setPosition; invalid value is -1      //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    public WebForthFile(byte fileData[], String fileName) {                        //4 PSHP
//4 PSHP-2        data = fileData;                                                           //4 PSHP
//4 PSHP-2        name = fileName;                                                           //4 PSHP
//4 PSHP-2        position = 0;                                                              //4 PSHP
//4 PSHP-2        lineNumber = 1;                                                            //4 PSHP
//4 PSHP-2     }                                                                             //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    public int getSize() { return data.length; }                                   //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    // Read from position to end of data up to n bytes into array m                //4 PSHP
//4 PSHP-2    // starting at offset. Update position, invalidate lineNumber.                 //4 PSHP
//4 PSHP-2    // Return number of bytes read.                                                //4 PSHP
//4 PSHP-2    public int readFile(int m[], int offset, int n) {                              //4 PSHP
//4 PSHP-2        int limit = Math.min(n, data.length - position);                           //4 PSHP
//4 PSHP-2        for (int i = 0; i < limit; i++) {                                          //4 PSHP
//4 PSHP-2            m[offset + i] = data[position++] & 0xff;                               //4 PSHP
//4 PSHP-2        }                                                                          //4 PSHP
//4 PSHP-2        lineNumber = -1;                                                           //4 PSHP
//4 PSHP-2        return limit;                                                              //4 PSHP
//4 PSHP-2    }                                                                              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    // Read from position to end of line up to n bytes into array m                //4 PSHP
//4 PSHP-2    // starting at offset. Up to 2 line-terminating characters may also be         //4 PSHP
//4 PSHP-2    // read; recognised line terminators are CR (0x0D) or LF (0x0A)                //4 PSHP
//4 PSHP-2    // singly or together in either order; Update position and lineNumber.         //4 PSHP
//4 PSHP-2    // Return number of bytes read excluding line terminator(s).                   //4 PSHP
//4 PSHP-2    public int readLine(int m[], int offset, int n) {                              //4 PSHP
//4 PSHP-2        int startPosition = position;                                              //4 PSHP
//4 PSHP-2        int limit = Math.min(n, data.length - position);                           //4 PSHP
//4 PSHP-2        int count = 0;                                                             //4 PSHP
//4 PSHP-2        int c;                                                                     //4 PSHP
//4 PSHP-2        for (int i = 0; i < limit; i++) {                                          //4 PSHP
//4 PSHP-2            c = data[position++] & 0xff;                                           //4 PSHP
//4 PSHP-2            if (c == 0x0a || c == 0x0d) {                                          //4 PSHP
//4 PSHP-2                position--;                                                        //4 PSHP
//4 PSHP-2                break;                                                             //4 PSHP
//4 PSHP-2            } else {                                                               //4 PSHP
//4 PSHP-2                m[offset + count++] = c;                                           //4 PSHP
//4 PSHP-2            }                                                                      //4 PSHP
//4 PSHP-2        }                                                                          //4 PSHP
//4 PSHP-2        if (position < data.length) {                                              //4 PSHP
//4 PSHP-2            if ((data[position] & 0xff) == 0x0a) {                                 //4 PSHP
//4 PSHP-2                m[offset + count] = data[position++] & 0xff;                       //4 PSHP
//4 PSHP-2                if (lineNumber != -1) lineNumber++;                                //4 PSHP
//4 PSHP-2                if (position < data.length && (data[position] & 0xff) == 0x0d) {   //4 PSHP
//4 PSHP-2                    m[offset + count + 1] = data[position++] & 0xff;               //4 PSHP
//4 PSHP-2                }                                                                  //4 PSHP
//4 PSHP-2            } else if ((data[position] & 0xff) == 0x0d) {                          //4 PSHP
//4 PSHP-2                m[offset + count] = data[position++] & 0xff;                       //4 PSHP
//4 PSHP-2                if (lineNumber != -1) lineNumber++;                                //4 PSHP
//4 PSHP-2                if (position < data.length && (data[position] & 0xff) == 0x0a) {   //4 PSHP
//4 PSHP-2                    m[offset + count + 1] = data[position++] & 0xff;               //4 PSHP
//4 PSHP-2                }                                                                  //4 PSHP
//4 PSHP-2            }                                                                      //4 PSHP
//4 PSHP-2        } else if (position > startPosition) {                                     //4 PSHP
//4 PSHP-2            if (lineNumber != -1) lineNumber++;                                    //4 PSHP
//4 PSHP-2        }                                                                          //4 PSHP
//4 PSHP-2        return count;                                                              //4 PSHP
//4 PSHP-2    }                                                                              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    // Read up to n characters of name into array m starting at offset.            //4 PSHP
//4 PSHP-2    // If name is longer than n characters then leading characters are truncated.  //4 PSHP
//4 PSHP-2    // Return number of characters read.                                           //4 PSHP
//4 PSHP-2    public int readName(int m[], int offset, int n) {                              //4 PSHP
//4 PSHP-2        char fileName[] = name.toCharArray();                                      //4 PSHP
//4 PSHP-2        int limit = Math.min(n, fileName.length);                                  //4 PSHP
//4 PSHP-2        int r = Math.max(0, fileName.length - limit);                              //4 PSHP
//4 PSHP-2        for (int i = 0; i < limit; i++) {                                          //4 PSHP
//4 PSHP-2            m[offset + i] = fileName[r++];                                         //4 PSHP
//4 PSHP-2        }                                                                          //4 PSHP
//4 PSHP-2        return limit;                                                              //4 PSHP
//4 PSHP-2    }                                                                              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    public int getPosition() { return position; }                                  //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    // Set position to n if it is valid. Invalidate lineNumber.                    //4 PSHP
//4 PSHP-2    // Return new position.                                                        //4 PSHP
//4 PSHP-2    public int setPosition(int n) {                                                //4 PSHP
//4 PSHP-2        if (n >= 0 && n <= data.length) {                                          //4 PSHP
//4 PSHP-2            position = n;                                                          //4 PSHP
//4 PSHP-2            lineNumber = -1;                                                       //4 PSHP
//4 PSHP-2        }                                                                          //4 PSHP
//4 PSHP-2        return position;                                                           //4 PSHP
//4 PSHP-2    }                                                                              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    public int getLineNumber() { return lineNumber; }                              //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2    public void setLineNumber(int n) { lineNumber = n; }                           //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2                                                                                   //4 PSHP
//4 PSHP-2}                                                                                  //4 PSHP
/////////////////////////////////////////////////////////////                      //4 PSHP
// Internally held file.                                                           //4 PSHP
/////////////////////////////////////////////////////////////                      //4 PSHP
class WebForthFile {                                                               //4 PSHP
    //4 PSHP
    private final byte[] data;     // file data                                          //4 PSHP
    //4 PSHP
    private final String name;     // file name                                          //4 PSHP
    //4 PSHP
    private int position;    // file position updated by readFile and readLine;    //4 PSHP-2
    // invariant: 0 <= position <= data.length            //4 PSHP
    //4 PSHP
    private int lineNumber;  // line number of line identified by position;        //4 PSHP
    // updated by readLine when valid; invalidated by     //4 PSHP
    // readFile and setPosition; invalid value is -1      //4 PSHP
    //4 PSHP-2
    private int line[];      // most recent data transferred by readLine           //4 PSHP-2
    //4 PSHP
    public WebForthFile(byte fileData[], String fileName) {                        //4 PSHP
        data = fileData;                                                           //4 PSHP
        name = fileName;                                                           //4 PSHP
        position = 0;                                                              //4 PSHP
        lineNumber = 1;                                                            //4 PSHP
        line = new int[0];                                                         //4 PSHP-2
    }                                                                             //4 PSHP
    //4 PSHP
    public int getSize() { return data.length; }                                   //4 PSHP
    //4 PSHP
    // From position copy the lesser of n bytes or until end of file               //4 PSHP-2
    // into array m starting at offset. Update position, invalidate                //4 PSHP-2
    // lineNumber. Return number of bytes copied.                                  //4 PSHP-2
    public int readFile(int m[], int offset, int n) {                              //4 PSHP
        int limit = Math.min(n, data.length - position);                           //4 PSHP
        for (int i = 0; i < limit; i++) {                                          //4 PSHP
            m[offset + i] = data[position++] & 0xff;                               //4 PSHP
        }                                                                          //4 PSHP
        lineNumber = -1;                                                           //4 PSHP
        return limit;                                                              //4 PSHP
    }                                                                              //4 PSHP
    //4 PSHP
    // From position copy the lesser of n characters or until a line terminator    //4 PSHP-2
    // or end of file is encountered into array m starting at offset. Update       //4 PSHP-2
    // position. If a line terminator is encountered it is not copied but          //4 PSHP-2
    // position is set to the first character of the following line and            //4 PSHP-2
    // lineNumber is incremented if valid. Recognised line terminators are         //4 PSHP-2
    // CR (0x0D) or LF (0x0A) singly or together in either order. Return number    //4 PSHP-2
    // of characters copied.                                                       //4 PSHP-2
    public int readLine(int m[], int offset, int n) {                              //4 PSHP-2
        int count = 0;                                                             //4 PSHP-2
        int c;                                                                     //4 PSHP-2
        if (position < data.length) {                                              //4 PSHP-2
            for (int i = 0; i < n; i++) {                                          //4 PSHP-2
                if (position == data.length) {                                     //4 PSHP-2
                    if (lineNumber != -1) lineNumber++;                            //4 PSHP-2
                    break;                                                         //4 PSHP-2
                }                                                                  //4 PSHP-2
                c = data[position++] & 0xff;                                       //4 PSHP-2
                if (c == 0x0A || c == 0x0D) {                                      //4 PSHP-2
                    if (c == 0x0A) {                                               //4 PSHP-2
                        if (position < data.length && data[position] == 0x0D) {    //4 PSHP-2
                            position++;                                            //4 PSHP-2
                        }                                                          //4 PSHP-2
                    } else {                                                       //4 PSHP-2
                        if (position < data.length && data[position] == 0x0A) {    //4 PSHP-2
                            position++;                                            //4 PSHP-2
                        }                                                          //4 PSHP-2
                    }                                                              //4 PSHP-2
                    if (lineNumber != -1) lineNumber++;                            //4 PSHP-2
                    break;                                                         //4 PSHP-2
                } else {                                                           //4 PSHP-2
                    m[offset + count++] = c;                                       //4 PSHP-2
                }                                                                  //4 PSHP-2
            }                                                                      //4 PSHP-2
        }                                                                          //4 PSHP-2
        line = new int[count];                                                     //4 PSHP-2
        System.arraycopy(m, offset, line, 0, count);                               //4 PSHP-2
        return count;                                                              //4 PSHP-2
    }                                                                              //4 PSHP-2
    //4 PSHP-2
    // Copy up to n characters of name into array m starting at offset.            //4 PSHP-2
    // If name is longer than n characters then leading characters are truncated.  //4 PSHP-2
    // Return number of characters copied.                                         //4 PSHP-2
    public int readName(int m[], int offset, int n) {                              //4 PSHP
        char fileName[] = name.toCharArray();                                      //4 PSHP
        int limit = Math.min(n, fileName.length);                                  //4 PSHP
        int r = Math.max(0, fileName.length - limit);                              //4 PSHP
        for (int i = 0; i < limit; i++) {                                          //4 PSHP
            m[offset + i] = fileName[r++];                                         //4 PSHP
        }                                                                          //4 PSHP
        return limit;                                                              //4 PSHP
    }                                                                              //4 PSHP
    //4 PSHP
    public int getPosition() { return position; }                                  //4 PSHP
    //4 PSHP
    // Set position to n if it is valid. Invalidate lineNumber.                    //4 PSHP
    // Return new position.                                                        //4 PSHP
    public int setPosition(int n) {                                                //4 PSHP
        if (n >= 0 && n <= data.length) {                                          //4 PSHP
            position = n;                                                          //4 PSHP
            lineNumber = -1;                                                       //4 PSHP
        }                                                                          //4 PSHP
        return position;                                                           //4 PSHP
    }                                                                              //4 PSHP
    //4 PSHP
    public int getLineNumber() { return lineNumber; }                              //4 PSHP
    //4 PSHP
    public void setLineNumber(int n) { lineNumber = n; }                           //4 PSHP
    //4 PSHP
    // Copy contents of line into array m starting at offset.                      //4 PSHP-2
    // Return number of characters copied.                                         //4 PSHP-2
    public int repeatLine(int m[], int offset) {                                   //4 PSHP-2
        System.arraycopy(line, 0, m, offset, line.length);                         //4 PSHP-2
        return line.length;                                                        //4 PSHP-2
    }                                                                              //4 PSHP-2
    //4 PSHP-2
    //4 PSHP
}                                                                                  //4 PSHP
