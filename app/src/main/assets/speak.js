function Speak(text)
{
    var input = text;
    while (input.length < 256) input += " ";
    var ptr = allocate(intArrayFromString(input), 'i8', ALLOC_STACK);
    _TextToPhonemes(ptr);
    _SetInput(ptr);
    _Code39771();
    var bufferlength = Math.floor(_GetBufferLength()/50);
    var bufferptr = _GetBuffer();
    audiobuffer = new Float32Array(bufferlength);
    for(var i=0; i<bufferlength; i++)
        audiobuffer[i] = ((getValue(bufferptr+i, 'i8')&0xFF)-128)/256;
    //PlayBuffer(audiobuffer);
    //PlayWebkit(new AudioContext(), audiobuffer);
    var context = new AudioContext();
    var source = context.createBufferSource();
    var soundBuffer = context.createBuffer(1, audiobuffer.length, 22050);
    var buffer = soundBuffer.getChannelData(0);
    for(var i=0; i<audiobuffer.length; i++) buffer[i] = audiobuffer[i];
    source.buffer = soundBuffer;
    source.connect(context.destination);
    source.start(0);
}
