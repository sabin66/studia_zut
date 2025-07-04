// sd55617
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        int[] bits = {0, 0,0,1,1,0,1,1,0,0,0,1};
        double Tc = 2;
        double Tb = Tc / bits.length;
        double W1 = 2;
        double fn1 = (W1+1) / Tb;
        double fn2 = (W1+2) / Tb;
        double fs = 8000;

        double[] t = generateTime(bits.length, Tb, fs);

        double[] FSK_signal = FSK(bits,fn1,fn2,fs,Tb);
        double[] x1FSK = modulate_x1(FSK_signal,fn1,fs,Tb);
        double[] x2FSK = modulate_x2(FSK_signal,fn2,fs,Tb);
        double[] p1FSK = modulate_p(x1FSK,fs,Tb);
        double[] p2FSK = modulate_p(x2FSK,fs,Tb);
        double[] pFSK = addTwoArrays(p1FSK,p2FSK);
        double[] cFSK = modulate_c(pFSK);
        int[] bitsFSK = toBits(cFSK,Tb,fs);

        plot(t, FSK_signal,"FSK_z(t)");
        plot(t,x1FSK,"x1FSK");
        plot(t,x2FSK,"x2FSK");
        plot(t,p1FSK,"p1FSK");
        plot(t,p2FSK,"p2FSK");
        plot(t,pFSK,"pFSK");
        plot(t,cFSK,"cFSK");
        System.out.println(Arrays.toString(bitsFSK));


    }
    static double[] FSK(int[] signal, double fn1, double fn2, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[signal.length * samplesPerBit];

        for (int i = 0; i < signal.length; i++) {
            double fn = signal[i] == 0 ? fn1 : fn2;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = Math.sin(2 * Math.PI * fn * t);
            }
        }

        return result;
    }


    static double[] generateTime(int numBits, double Tb, double fs) {
        int totalSamples = (int)(numBits * Tb * fs);
        double[] t = new double[totalSamples];
        for (int i = 0; i < t.length; i++) {
            t[i] = i / fs;
        }
        return t;
    }


    static double[] modulate_x1(double[] signal, double fn1, double fs, double Tb) {
        double[] result = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            double t = i / fs;
            result[i] = signal[i] * Math.sin(2 * Math.PI * fn1 * t);
        }
        return result;
    }


    static double[] modulate_x2(double[] signal, double fn2, double fs, double Tb) {
        double[] result = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            double t = i / fs;
            result[i] = signal[i] * Math.sin(2 * Math.PI * fn2 * t);
        }
        return result;
    }


    static double[] modulate_p(double[] signal, double fs, double Tb) {
        int Tbp = (int)(Tb * fs);
        int B = signal.length / Tbp;
        double[] p = new double[signal.length];
        int i = 0;
        for (int b = 0; b < B; b++) {
            double s = 0;
            for (int n = 0; n < Tbp; n++) {
                s = s + signal[b * Tbp + n];
                p[i++] = s;
            }
        }
        return p;
    }
    static double[] addTwoArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    static double[] modulate_c(double[] signal) {
        int B = signal.length;
        double[] result = new double[signal.length];
        for (int i = 0; i < B; i++) {
            if(signal[i] > 0.000000001 ){
                result[i] = 1;
            }else{
                result[i] = 0;
            }
        }
        return result;
    }


    public static int[] toBits(double[] signal, double Tb, double fs) {
        int Tbp = (int)(Tb * fs);
        int B = signal.length / Tbp;
        int[] bits = new int[B];

        for (int b = 0; b < B; b++) {
            double m = 0;
            for (int n = 0; n < Tbp; n++) {
                m += signal[b * Tbp + n];
            }
            double avg = m / Tbp;
            if (avg >= 0.5) {
                bits[b] = 1;
            } else {
                bits[b] = 0;
            }
        }
        return bits;
    }

    public static void plot(double[] x, double[] y, String title) {
        XYSeries series = new XYSeries(title);
        int len = Math.min(x.length, y.length);
        for (int i = 0; i < len; i++) {
            series.add(x[i], y[i]);
        }
        PlotSignal(series, title, "Czas [s]", "Amplituda");
    }


    public static void PlotSignal(XYSeries series, String title, String xLabel, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel,
                dataset, PlotOrientation.VERTICAL,
                false, true, false
        );
        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);
    }
}
