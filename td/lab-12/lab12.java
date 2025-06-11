// sd55617
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.awt.Dimension;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
public class Main {

    public static void main(String[] args) {
        // sekcja parametrow
        int[] bits = {
                1,0,1,0,0,1,1,0,1,1,1,
                0,1,1,1,1,0,0,1,0,1,0,
                1,1,0,0,0,1,1,1,0,1,0,
                0,0,1,1,0,1,1,0,1,0,0,
                1,1,0,1,1,1,0,0,1,0,1,
                0,1,0,0,1,1,1,1,0,1,0
        };
        double A1 = 1, A2 = 2, A = 3;
        double Tc = 2;
        double Tb = Tc / bits.length;
        double W  = 2;
        double fn = W / Tb;
        double fn1 = (W+1) / Tb;
        double fn2 = (W+2) / Tb;
        double fs = 8000;
        List<Double> berASK1511 = new ArrayList<>();
        List<Double> berPSK1511 = new ArrayList<>();
        List<Double> berFSK1511 = new ArrayList<>();
        List<Double> berASK74  = new ArrayList<>();
        List<Double> berPSK74  = new ArrayList<>();
        List<Double> berFSK74  = new ArrayList<>();
        double[] alphas = {0.3,0.6,0.9,1.2,1.5,1.8,2.1,2.4,2.7,3.0};

        for(double alpha : alphas){
            berASK1511.add(askHamming1511(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK1511.add(pskHamming1511(bits, fn, fs, Tb, A, alpha));
            berFSK1511.add(fskHamming1511(bits, fn1, fn2, fs, Tb, alpha));
            berASK74.add(askHamming74(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK74.add(pskHamming74(bits, fn, fs, Tb, A, alpha));
            berFSK74.add(fskHamming74(bits, fn1, fn2, fs, Tb, alpha));
        }
        showPlots(berASK1511, berPSK1511,berFSK1511, berASK74, berPSK74, berFSK74, alphas);
    }

    private static void showPlots(List<Double> berASK1511, List<Double> berPSK1511, List<Double> berFSK1511,
                                  List<Double> berASK74, List<Double> berPSK74,List<Double> berFSK74, double[] alphas) {
        double[] dASK15 = toArray(berASK1511);
        double[] dPSK15 = toArray(berPSK1511);
        double[] dFSK15 = toArray(berFSK1511);
        double[] dASK74 = toArray(berASK74);
        double[] dPSK74 = toArray(berPSK74);
        double[] dFSK74 = toArray(berFSK74);
        showSingleSeriesChart("BER vs α — ASK 15/11", alphas, dASK15);
        showSingleSeriesChart("BER vs α — PSK 15/11", alphas, dPSK15);
        showSingleSeriesChart("BER vs α — FSK 15/11", alphas, dFSK15);
        showSingleSeriesChart("BER vs α — ASK 7/4", alphas, dASK74);
        showSingleSeriesChart("BER vs α — PSK 7/4", alphas, dPSK74);
        showSingleSeriesChart("BER vs α — FSK 7/4", alphas, dFSK74);
    }

    private static double askHamming74(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogASK = ASK(coded, A1, A2, fn, fs, Tb);
        double[] carrierASK = modulate_x(analogASK, fn, fs, A);

        double[] noisyASK = addWhiteNoise(carrierASK, alpha);

        double[] pASK = modulate_p(noisyASK, fs, Tb);
        double[] cASK = modulate_c(pASK);
        int[] demodBitsASK = toBits(cASK, Tb, fs);

        List<Integer> demodListASK = new ArrayList<>();
        for (int b : demodBitsASK) demodListASK.add(b);

        List<Integer> decoded = HammingDecode74(demodListASK);
        System.out.println("--------------------ASK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(7,4): " + coded);
        System.out.println("Po demodulacji : " + demodListASK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double pskHamming74(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogPSK = PSK(coded,A,fn, fs, Tb);
        double[] carrierPSK = modulate_x(analogPSK, fn, fs, A);

        double[] noisyPSK = addWhiteNoise(carrierPSK, alpha);

        double[] pPSK = modulate_p(noisyPSK, fs, Tb);
        double[] cPSK = modulate_c(pPSK);
        int[] demodBitsPSK = toBits(cPSK, Tb, fs);

        List<Integer> demodListPSK = new ArrayList<>();
        for (int b : demodBitsPSK) demodListPSK.add(b);

        List<Integer> decoded = HammingDecode74(demodListPSK);
        System.out.println("--------------------PSK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(15,11): " + coded);
        System.out.println("Po demodulacji : " + demodListPSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming74(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise(carrierFSK1, alpha);
        double[] noisyFSK2 = addWhiteNoise(carrierFSK2, alpha);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_c(pFSK);
        int[] demodBitsFSK = toBits(cFSK, Tb, fs);

        List<Integer> demodListFSK = new ArrayList<>();
        for (int b : demodBitsFSK) demodListFSK.add(b);

        List<Integer> decoded = HammingDecode74(demodListFSK);
        System.out.println("--------------------FSK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(7,4): " + coded);
        System.out.println("Po demodulacji : " + demodListFSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double askHamming1511(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogASK = ASK(coded, A1, A2, fn, fs, Tb);
        double[] carrierASK = modulate_x(analogASK, fn, fs, A);

        double[] noisyASK = addWhiteNoise(carrierASK, alpha);

        double[] pASK = modulate_p(noisyASK, fs, Tb);
        double[] cASK = modulate_c(pASK);
        int[] demodBitsASK = toBits(cASK, Tb, fs);

        List<Integer> demodListASK = new ArrayList<>();
        for (int b : demodBitsASK) demodListASK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListASK);
        System.out.println("--------------------ASK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(15,11): " + coded);
        System.out.println("Po demodulacji : " + demodListASK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double pskHamming1511(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogPSK = PSK(coded,A,fn, fs, Tb);
        double[] carrierPSK = modulate_x(analogPSK, fn, fs, A);

        double[] noisyPSK = addWhiteNoise(carrierPSK, alpha);

        double[] pPSK = modulate_p(noisyPSK, fs, Tb);
        double[] cPSK = modulate_c(pPSK);
        int[] demodBitsPSK = toBits(cPSK, Tb, fs);

        List<Integer> demodListPSK = new ArrayList<>();
        for (int b : demodBitsPSK) demodListPSK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListPSK);
        System.out.println("--------------------PSK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(15,11): " + coded);
        System.out.println("Po demodulacji : " + demodListPSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming1511(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise(carrierFSK1, alpha);
        double[] noisyFSK2 = addWhiteNoise(carrierFSK2, alpha);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_c(pFSK);
        int[] demodBitsFSK = toBits(cFSK, Tb, fs);

        List<Integer> demodListFSK = new ArrayList<>();
        for (int b : demodBitsFSK) demodListFSK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListFSK);
        System.out.println("--------------------FSK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(15,11): " + coded);
        System.out.println("Po demodulacji : " + demodListFSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    public static List<Integer> HammingCode1511(int[] bits) {
        List<Integer> encoded = new ArrayList<>();

        for (int i = 0; i < bits.length; i += 11) {
            int[] newBlock = new int[11];
            for (int j = 0; j < 11; j++) {
                if (i + j < bits.length) {
                    newBlock[j] = bits[i + j];
                } else {
                    newBlock[j] = 0;
                }
            }
            int[] encodedBlock = HammingCodeInternal1511(newBlock);
            for (int bit : encodedBlock) {
                encoded.add(bit);
            }
        }

        return encoded;
    }

    public static int[] HammingCodeInternal1511(int[] bits) {
        int[] codeword = new int[15];

        codeword[2] = bits[0];
        codeword[4] = bits[1];
        codeword[5] = bits[2];
        codeword[6] = bits[3];
        codeword[8] = bits[4];
        codeword[9] = bits[5];
        codeword[10] = bits[6];
        codeword[11] = bits[7];
        codeword[12] = bits[8];
        codeword[13] = bits[9];
        codeword[14] = bits[10];

        codeword[0] = codeword[2] ^ codeword[4] ^ codeword[6] ^ codeword[8] ^ codeword[10] ^ codeword[12] ^ codeword[14];
        codeword[1] = codeword[2] ^ codeword[5] ^ codeword[6] ^ codeword[9] ^ codeword[10] ^ codeword[13] ^ codeword[14];
        codeword[3] = codeword[4] ^ codeword[5] ^ codeword[6] ^ codeword[11] ^ codeword[12] ^ codeword[13] ^ codeword[14];
        codeword[7] = codeword[8] ^ codeword[9] ^ codeword[10] ^ codeword[11] ^ codeword[12] ^ codeword[13] ^ codeword[14];

        return codeword;
    }

    public static List<Integer> HammingDecode1511(List<Integer> coded){
        List<Integer> decoded = new ArrayList<>();
        for (int i = 0; i < coded.size(); i+=15) {
            int[] newBlock = new int[15];
            for (int j = 0; j < 15; j++) {
                newBlock[j] = coded.get(i + j);
            }
            int[] corrected = HammingDecodeInternal1511(newBlock);
            int[] dataPosition = {2,4,5,6,8,9,10,11,12,13,14};
            for(int pos :dataPosition){
                decoded.add(corrected[pos]);
            }
        }
        return decoded;
    }

    public static int[] HammingDecodeInternal1511(int[] coded){
        int s1 = coded[0]^coded[2]^coded[4]^coded[6]^coded[8]^coded[10]^coded[12]^coded[14];
        int s2 = coded[1]^coded[2]^coded[5]^coded[6]^coded[9]^coded[10]^coded[13]^coded[14];
        int s4 = coded[3]^coded[4]^coded[5]^coded[6]^coded[11]^coded[12]^coded[13]^coded[14];
        int s8 = coded[7]^coded[8]^coded[9]^coded[10]^coded[11]^coded[12]^coded[13]^coded[14];
        int syndrome = s1 + s2*2 + s4*4 + s8*8;
        if (syndrome > 0) {
            coded[syndrome-1] ^= 1;
        }
        return coded;
    }

    static List<Integer> HammingCode74(int[] bits) {
        List<Integer> encoded = new ArrayList<>();

        for (int i = 0; i < bits.length; i += 4) {
            int[] newBlock = new int[4];
            for (int j = 0; j < 4; j++) {
                if (i + j < bits.length) {
                    newBlock[j] = bits[i + j];
                } else {
                    newBlock[j] = 0;
                }
            }
            int[] encodedBlock = HammingCodeInternal74(newBlock);
            for (int bit : encodedBlock) {
                encoded.add(bit);
            }
        }

        return encoded;
    }

    static int[] HammingCodeInternal74(int[] bits){
        int x3 = bits[0];
        int x5 = bits[1];
        int x6 = bits[2];
        int x7 = bits[3];

        int x1 = x3 ^ x5 ^ x7;
        int x2 = x3 ^ x6 ^ x7;
        int x4 = x5 ^ x6 ^ x7;

        return new int[]{x1, x2, x3, x4, x5, x6, x7};
    }

    static List<Integer> HammingDecode74(List<Integer> encodedBits) {
        List<Integer> decoded = new ArrayList<>();

        for (int i = 0; i < encodedBits.size(); i += 7) {
            int[] block = new int[7];

            for (int j = 0; j < 7; j++) {
                if (i + j < encodedBits.size()) {
                    block[j] = encodedBits.get(i + j);
                } else {
                    block[j] = 0;
                }
            }

            int[] correctedBlock = HammingDecodeInternal74(block);

            decoded.add(correctedBlock[2]);
            decoded.add(correctedBlock[4]);
            decoded.add(correctedBlock[5]);
            decoded.add(correctedBlock[6]);
        }

        return decoded;
    }

    static int[] HammingDecodeInternal74(int[] bits){
        int[] corrected = Arrays.copyOf(bits, bits.length);

        int x3 = corrected[2];
        int x5 = corrected[4];
        int x6 = corrected[5];
        int x7 = corrected[6];

        int x1 = x3 ^ x5 ^ x7;
        int x2 = x3 ^ x6 ^ x7;
        int x4 = x5 ^ x6 ^ x7;

        int x1_roof = corrected[0] ^ x1;
        int x2_roof = corrected[1] ^ x2;
        int x4_roof = corrected[3] ^ x4;

        int target = x1_roof + x2_roof * 2 + x4_roof * 4;
        if(target > 0 ) {
            corrected[target - 1] ^= 1;
        }
        return corrected;
    }

    static double[] ASK(List<Integer> bits, double A1, double A2, double fn, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[bits.size() * samplesPerBit];

        for (int i = 0; i < bits.size(); i++) {
            double amplitude = bits.get(i) == 1 ? A2 : A1;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = amplitude * Math.cos(2 * Math.PI * fn * t);
            }
        }

        return result;
    }

    public static double[] PSK(List<Integer> bits, double A, double fn, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[bits.size() * samplesPerBit];
        for (int i = 0; i < bits.size(); i++) {
            double phase = bits.get(i) == 0 ? 0 : Math.PI;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = A * Math.sin(2 * Math.PI * fn * t + phase);
            }
        }

        return result;
    }

    static double[] FSK(List<Integer> bits, double fn1, double fn2, double fs, double Tb) {
        int samplesPerBit = (int)(Tb * fs);
        double[] result = new double[bits.size() * samplesPerBit];

        for (int i = 0; i < bits.size(); i++) {
            double fn = bits.get(i) == 0 ? fn1 : fn2;
            for (int j = 0; j < samplesPerBit; j++) {
                double t = (i * samplesPerBit + j) / fs;
                result[i * samplesPerBit + j] = Math.sin(2 * Math.PI * fn * t);
            }
        }

        return result;
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

    static double[] addTwoArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    static double[] modulate_cfsk(double[] signal) {
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

    static double[] addWhiteNoise(double[] x, double alpha) {
        Random rng = new Random();
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            double g = 2.0 * rng.nextDouble() - 1.0;
            y[i] = x[i] + alpha * g;
        }
        return y;
    }

    public static double calculateBER(int[] sentBits, List<Integer> receivedBits) {
        int errors = 0;
        int N = sentBits.length;
        for (int i = 0; i < N; i++) {
            if (sentBits[i] != receivedBits.get(i)) {
                errors++;
            }
        }
        return (errors / (double) N) * 100.0;
    }

    private static double[] toArray(List<Double> list) {
        double[] out = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    private static void showSingleSeriesChart(String title, double[] alphas, double[] ber) {
        XYSeries series = new XYSeries(title);
        for (int i = 0; i < alphas.length; i++) {
            series.add(alphas[i], ber[i]);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "α",
                "BER (%)",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 400));

        JFrame frame = new JFrame(title);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

}