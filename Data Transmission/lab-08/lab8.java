// sd55617
// pomoca z robieniem zadania byl chatGPT oraz Mikolaj Odzeniak, z ktorym wykonywalem funkcje z powodu powolnej utraty poczytalnosci
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        int[] bits = {0, 1, 0, 1, 1, 0, 1, 0, 1, 1};
        double A1 = 1;
        double A2 = 2;
        double A = 3;
        double Tc = 2;
        double Tb = Tc / bits.length;
        double W = 2;
        double fn = W / Tb;
        double fs = 8000;

        double[] t = generateTime(bits.length, Tb, fs);
        double[] h_values = {1, 2, 3};
        double[] ASK_signal = null;
        double[] xASK = null;
        double[] pASK = null;
        double[] cASK = null;
        double[] PSK_signal = null;
        double[] xPSK = null;
        double[] pPSK = null;
        double[] cPSK = null;
        for (double h : h_values) {

            // ASK
            ASK_signal = ASK(bits, A1, A2, fn, fs, Tb);
            xASK = modulate_x(ASK_signal, fn, fs, A);
            pASK = modulate_p(xASK, fs, Tb);
            cASK = modulate_c(pASK);
            int[] ASKBits = toBits(cASK, Tb, fs);

            System.out.println(Arrays.toString(ASKBits));
            double berASK = calculateBER(bits, ASKBits);
            System.out.printf("BER ASK dla h=%.2f: %.2f%%\n", h, berASK);

            // PSK
            PSK_signal = PSK(bits, A, fn, fs, Tb);
            xPSK = modulate_x(PSK_signal, fn, fs, A);
            pPSK = modulate_p(xPSK, fs, Tb);
            cPSK = modulate_c(pPSK);
            int[] PSKBits = toBits(cPSK, Tb, fs);

            System.out.println(Arrays.toString(PSKBits));
            double berPSK = calculateBER(bits, PSKBits);
            System.out.printf("BER PSK dla h=%.2f: %.2f%%\n", h, berPSK);

        }

        plot(t, ASK_signal, "ASK_(t)");
        plot(t, xASK, "ASK_x(t)");
        plot(t, pASK, "ASK_p(t)");
        plot(t, cASK, "ASK_c(t)");

        plot(t, PSK_signal, "PSK_z(t)");
        plot(t, xPSK, "PSK_x(t)");
        plot(t, pPSK, "PSK_p(t)");
        plot(t, cPSK, "PSK_c(t)");
    }
    static double[] ASK(int[] bits, double A1, double A2, double fn, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[bits.length * samplesPerBit];

        for (int i = 0; i < bits.length; i++) {
            double amplitude = bits[i] == 1 ? A2 : A1;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = amplitude * Math.cos(2 * Math.PI * fn * t);
            }
        }

        return result;
    }

    public static double[] PSK(int[] bits, double A, double fn, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[bits.length * samplesPerBit];
        for (int i = 0; i < bits.length; i++) {
            double phase = bits[i] == 0 ? 0 : Math.PI;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = A * Math.sin(2 * Math.PI * fn * t + phase);
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


    static double[] modulate_x(double[] signal, double fn, double fs, double A) {
        double[] result = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            double t = i / fs;
            result[i] = signal[i] * A * Math.cos(2 * Math.PI * fn * t);
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


    static double[] modulate_c(double[] signal) {
        double[] c = new double[signal.length];
        double sum = 0;
        for (double v : signal) {
            sum += v;
        }
        double h = sum / signal.length;

        for (int n = 0; n < signal.length; n++) {
            if (signal[n] > h) {
                c[n] = 1;
            } else {
                c[n] = 0;
            }
        }
        return c;
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

    public static double calculateBER(int[] sentBits, int[] receivedBits) {
        if (sentBits.length != receivedBits.length) {
            throw new IllegalArgumentException("Długości ciągów bitów muszą być takie same");
        }
        int errors = 0;
        int N = sentBits.length;
        for (int i = 0; i < N; i++) {
            if (sentBits[i] != receivedBits[i]) {
                errors++;
            }
        }
        return (errors / (double) N) * 100.0;
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
