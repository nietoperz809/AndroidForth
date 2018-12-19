function PlayWebkit(context, audiobuffer)
{
    var source = context.createBufferSource();
    var soundBuffer = context.createBuffer(1, audiobuffer.length, 22050);
    var buffer = soundBuffer.getChannelData(0);
    for(var i=0; i<audiobuffer.length; i++) buffer[i] = audiobuffer[i];
    source.buffer = soundBuffer;
    source.connect(context.destination);
    source.start(0);
}

function PlayMozilla(context, audiobuffer)
{
    var written = context.mozWriteAudio(audiobuffer);
    var diff = audiobuffer.length - written;
    if (diff <= 0) return;
    var buffer = new Float32Array(diff);
    for(var i = 0; i<diff; i++) buffer[i] = audiobuffer[i+written];
    window.setTimeout(function(){PlayMozilla(context, buffer)}, 500);
}


function PlayBuffer(audiobuffer)
{
    if (typeof AudioContext !== "undefined")
    {
           PlayWebkit(new AudioContext(), audiobuffer);
    } else
    if (typeof webkitAudioContext !== "undefined")
    {
        PlayWebkit(new webkitAudioContext(), audiobuffer);
    } else if (typeof Audio !== "undefined")
    {
        var context = new Audio();
        context.mozSetup(1, 22050);
        PlayMozilla(context, audiobuffer);
    }
}

function Speak(text)
{
    //alert(text);

    var input = text;
    while (input.length < 256) input += " ";
    var ptr = allocate(intArrayFromString(input), 'i8', ALLOC_STACK);
    _TextToPhonemes(ptr);
    //alert(Pointer_stringify(ptr));
    _SetInput(ptr);
    _Code39771();

    var bufferlength = Math.floor(_GetBufferLength()/50);
    var bufferptr = _GetBuffer();

    audiobuffer = new Float32Array(bufferlength);

    for(var i=0; i<bufferlength; i++)
        audiobuffer[i] = ((getValue(bufferptr+i, 'i8')&0xFF)-128)/256;
    PlayBuffer(audiobuffer);
}

Speak ("hello world");
