package jforth.waves;

public class Wave16
{

    public WaveFormType waveType = WaveFormType.OFF;

    /**
     * Data array that holds sampling data
     */
    public double[] data;
    /**
     * Sampling rate
     */
    private final int samplingRate;

    /**
     * Upper level constant
     */
    public static final double MAX_VALUE = Short.MAX_VALUE;

    /**
     * Lower level constant
     */
    public static final double MIN_VALUE = Short.MIN_VALUE;

    /**
     * Math constants
     */
    public static final double PI = Math.PI;
    public static final double PI2 = 2.0 * PI;
    public static final double ASIN1 = Math.asin(1.0);

    /**
     * Builds a new com.soundgen.pittbull.soundgen.Wave16 object
     *
     * @param size Size of array
     * @param rate Sampling rate
     */
    public Wave16(int size, int rate)
    {
        data = new double[size];
        samplingRate = rate;
    }

    public Wave16(int size, int rate, WaveFormType t)
    {
        this(size, rate);
        waveType = t;
    }

//    public static Wave16 extractSamples(Wave16 source, int from, int to)
//    {
//        int len = to - from;
//        Wave16 res = new Wave16(len, source.samplingRate);
//        System.arraycopy(source.data, from, res.data, 0, len);
//        return res;
//    }


    /**
     * Local factory function that builds a new SamplingData16 object from this one
     * All samples are empty
     *
     * @return The new object
     */
    public Wave16 createEmptyCopy()
    {
        return new Wave16(data.length, samplingRate);
        //out.setName(Thread.currentThread().getStackTrace()[2].getMethodName());
    }

//    /**
//     * Returns whole array as 'short' values
//     *
//     * @return The new 'short' array
//     */
//    public short[] toShortArray()
//    {
//        short[] res = new short[data.length];
//        for (int s = 0; s < data.length; s++)
//        {
//            res[s] = (short) data[s];
//        }
//        return res;
//    }

    /**
     * Implements the standard toString
     *
     * @return A string describing this object
     */
    @Override
    public String toString()
    {
        char[] res = new char[data.length];
        for (int s = 0; s < data.length; s++)
        {
            res[s] = (char) data[s];
        }
        return new String(res);
    }

    public static double[] fitValues(double[] in)
    {
        double[] out = new double[in.length];
        Wave16.Wave16AmplitudeInfo am = new Wave16.Wave16AmplitudeInfo();
        am.calc(in);
        double div = am.span / (Wave16.MAX_VALUE - Wave16.MIN_VALUE);
        am.min = am.min / div;
        for (int s = 0; s < in.length; s++)
        {
            out[s] = in[s] / div + Wave16.MIN_VALUE - am.min;
            if (Double.isInfinite(out[s]) || Double.isNaN(out[s]))
            {
                out[s] = 0.0;
            }
        }
        return out;
    }

    public Wave16 deriveAndFitValues()
    {
        double f1;
        double f2;
        Wave16 out = createEmptyCopy();

        for (int s = 0; s < (data.length - 1); s++)
        {
            f1 = data[s];
            f2 = data[s + 1];
            out.data[s] = f2 - f1;
        }
        // Last sample
        out.data[data.length - 1] = out.data[data.length - 2];
        out.data = fitValues(out.data);
        return out;
    }

    //////////////////////////////////////////////////////////////////


    static class Wave16AmplitudeInfo
    {
        /**
         * Minimum amplitude
         */
        public double min;
        /**
         * Maximum amplitude
         */
        public double max;
        /**
         * Total amplitude span
         */
        public double span;

        Wave16AmplitudeInfo()
        {
        }

        /**
         * Does calculation so that members are valid
         *
         * @param arr Array to be used as base object
         */
        public void calc(double arr[])
        {
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;

            // Find min and max
            for (double anIn : arr)
            {
                // Force forbidden values to zero
                if (Double.isInfinite(anIn) || Double.isNaN(anIn))
                    anIn = 0.0;

                if (anIn < min)
                {
                    min = anIn;
                }
                if (anIn > max)
                {
                    max = anIn;
                }
            }
            span = max - min;
        }

        public String toString()
        {
            return "Min:" + min + " Max:" + max + " Span:" + span;
        }
    }
}

