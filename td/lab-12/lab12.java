// sd55617
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
public class Main {
    private static final int n = 15;
    private static final int k = 11;
    private static final int r = n - k;

    private static final int[] PARITY_POS_1 = {1, 2, 4, 8};

    private static final int[] DATA_POS_1 = new int[k];
    static {
        int idx = 0;
        outer:
        for (int i = 1; i <= n; i++) {
            for (int p : PARITY_POS_1) {
                if (p == i) continue outer;
            }
            DATA_POS_1[idx++] = i;
        }
    }

    private static final int[][] G = new int[k][n];
    static {
        for (int row = 0; row < k; row++) {
            int dataPos0 = DATA_POS_1[row] - 1;
            G[row][dataPos0] = 1;
            for (int i = 0; i < r; i++) {
                if (((DATA_POS_1[row] >> i) & 1) == 1) {
                    int parityCol0 = PARITY_POS_1[i] - 1;
                    G[row][parityCol0] = 1;
                }
            }
        }
    }

    private static final int[][] H = new int[r][n];
    static {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < n; j++) {
                H[i][j] = ((j+1) >> i) & 1;
            }
        }
    }

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
        List<Double> berASK1511_alpha = new ArrayList<>();
        List<Double> berPSK1511_alpha = new ArrayList<>();
        List<Double> berFSK1511_alpha = new ArrayList<>();
        List<Double> berASK74_alpha  = new ArrayList<>();
        List<Double> berPSK74_alpha  = new ArrayList<>();
        List<Double> berFSK74_alpha  = new ArrayList<>();
        List<Double> berASK1511_beta = new ArrayList<>();
        List<Double> berPSK1511_beta = new ArrayList<>();
        List<Double> berFSK1511_beta = new ArrayList<>();
        List<Double> berASK74_beta  = new ArrayList<>();
        List<Double> berPSK74_beta  = new ArrayList<>();
        List<Double> berFSK74_beta  = new ArrayList<>();
        double[] t = generateTime(bits.length,Tb,fs);
        double[] alphas = {0.3,0.6,0.9,1.2,1.5,1.8,2.1,2.4,2.7,3.0};
        double[] betas = {0.0,0.2,0.4,0.6,0.8,1.0,1.2,1.4,1.6,1.8,2.0};
        int counter = 0;

        for(double alpha : alphas){
            berASK1511_alpha.add(askHamming1511_alpha(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK1511_alpha.add(pskHamming1511_alpha(bits, fn, fs, Tb, A, alpha));
            berFSK1511_alpha.add(fskHamming1511_alpha(bits, fn1, fn2, fs, Tb, alpha));
            berASK74_alpha.add(askHamming74_alpha(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK74_alpha.add(pskHamming74_alpha(bits, fn, fs, Tb, A, alpha));
            berFSK74_alpha.add(fskHamming74_alpha(bits, fn1, fn2, fs, Tb, alpha));
        }
        for(double beta : betas){
            berASK1511_beta.add(askHamming1511_beta(bits, A1, A2, fn, fs, Tb, A, beta, t, Tc));
            berPSK1511_beta.add(pskHamming1511_beta(bits, fn, fs, Tb, A, beta, t, Tc));
            berFSK1511_beta.add(fskHamming1511_beta(bits, fn1, fn2, fs, Tb, beta, t, Tc));
            berASK74_beta.add(askHamming74_beta(bits, A1, A2, fn, fs, Tb, A, beta, t, Tc));
            berPSK74_beta.add(pskHamming74_beta(bits, fn, fs, Tb, A, beta, t, Tc));
            berFSK74_beta.add(fskHamming74_beta(bits, fn1, fn2, fs, Tb, beta, t, Tc));
            counter++;
            System.out.println("-------------------------------------" + counter + "--------------------------------------");
        }
        showPlotsAlfa(berASK1511_alpha, berPSK1511_alpha,berFSK1511_alpha, berASK74_alpha, berPSK74_alpha, berFSK74_alpha, alphas);
        showPlotsBeta(berASK1511_beta,berPSK1511_beta,berFSK1511_beta,berASK74_beta, berPSK74_beta, berFSK74_beta, betas);
    }
    private static void showPlotsBeta(List<Double> berASK1511, List<Double> berPSK1511, List<Double> berFSK1511,
                                      List<Double> berASK74, List<Double> berPSK74,List<Double> berFSK74, double[] betas) {
        double[] dASK15 = toArray(berASK1511);
        double[] dPSK15 = toArray(berPSK1511);
        double[] dFSK15 = toArray(berFSK1511);
        double[] dASK74 = toArray(berASK74);
        double[] dPSK74 = toArray(berPSK74);
        double[] dFSK74 = toArray(berFSK74);
        double[][] allBers = { dASK15, dPSK15, dFSK15, dASK74, dPSK74, dFSK74 };
        String[] titles = {
                "ASK 15/11", "PSK 15/11",
                "FSK 15/11",  "ASK 7/4",
                "PSK 7/4 ", "FSK 7/4"
        };
        showSingleSeriesChart("BER vs β", betas, allBers, titles);

    }
    private static void showPlotsAlfa(List<Double> berASK1511, List<Double> berPSK1511, List<Double> berFSK1511,
                                  List<Double> berASK74, List<Double> berPSK74,List<Double> berFSK74, double[] alphas) {
        double[] dASK15 = toArray(berASK1511);
        double[] dPSK15 = toArray(berPSK1511);
        double[] dFSK15 = toArray(berFSK1511);
        double[] dASK74 = toArray(berASK74);
        double[] dPSK74 = toArray(berPSK74);
        double[] dFSK74 = toArray(berFSK74);
        double[][] allBers = { dASK15, dPSK15, dFSK15, dASK74, dPSK74, dFSK74 };
        String[] titles = {
                "ASK 15/11", "PSK 15/11",
                "FSK 15/11",  "ASK 7/4",
                "PSK 7/4 ", "FSK 7/4"
        };
        showSingleSeriesChart("BER vs α", alphas, allBers, titles);

    }

    private static double askHamming74_alpha(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {
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

    private static double pskHamming74_alpha(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
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
        System.out.println("Po Hamming(7,4): " + coded);
        System.out.println("Po demodulacji : " + demodListPSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming74_alpha(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise(carrierFSK1, alpha);
        double[] noisyFSK2 = addWhiteNoise(carrierFSK2, alpha);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_cfsk(pFSK);
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

    private static double askHamming1511_alpha(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {
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

    private static double pskHamming1511_alpha(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
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

    private static double fskHamming1511_alpha(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise(carrierFSK1, alpha);
        double[] noisyFSK2 = addWhiteNoise(carrierFSK2, alpha);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_cfsk(pFSK);
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

    private static double askHamming74_beta(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double beta, double[] t,double Tc) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogASK = ASK(coded, A1, A2, fn, fs, Tb);
        double[] carrierASK = modulate_x(analogASK, fn, fs, A);

        double[] noisyASK = addWhiteNoise2(carrierASK, beta,Tc,t);

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

    private static double pskHamming74_beta(int[] bits, double fn, double fs, double Tb, double A, double beta, double[] t,double Tc) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogPSK = PSK(coded,A,fn, fs, Tb);
        double[] carrierPSK = modulate_x(analogPSK, fn, fs, A);

        double[] noisyPSK = addWhiteNoise2(carrierPSK, beta,Tc,t);

        double[] pPSK = modulate_p(noisyPSK, fs, Tb);
        double[] cPSK = modulate_c(pPSK);
        int[] demodBitsPSK = toBits(cPSK, Tb, fs);

        List<Integer> demodListPSK = new ArrayList<>();
        for (int b : demodBitsPSK) demodListPSK.add(b);

        List<Integer> decoded = HammingDecode74(demodListPSK);
        System.out.println("--------------------PSK--------------------");
        System.out.println("Wejściowe      : " + Arrays.toString(bits));
        System.out.println("Po Hamming(7,4): " + coded);
        System.out.println("Po demodulacji : " + demodListPSK);
        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming74_beta(int[] bits,double fn1,double fn2, double fs, double Tb, double beta, double[] t,double Tc) {
        List<Integer> coded = HammingCode74(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise2(carrierFSK1, beta, Tc, t);
        double[] noisyFSK2 = addWhiteNoise2(carrierFSK2, beta, Tc, t);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_cfsk(pFSK);
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

    private static double askHamming1511_beta(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double beta, double[] t,double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogASK = ASK(coded, A1, A2, fn, fs, Tb);
        double[] carrierASK = modulate_x(analogASK, fn, fs, A);

        double[] noisyASK = addWhiteNoise2(carrierASK, beta, Tc, t);

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

    private static double pskHamming1511_beta(int[] bits, double fn, double fs, double Tb, double A, double beta, double[] t,double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogPSK = PSK(coded,A,fn, fs, Tb);
        double[] carrierPSK = modulate_x(analogPSK, fn, fs, A);

        double[] noisyPSK = addWhiteNoise2(carrierPSK, beta, Tc, t);

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

    private static double fskHamming1511_beta(int[] bits,double fn1,double fn2, double fs, double Tb, double beta, double[] t, double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] analogFSK = FSK(coded,fn1,fn2,fs,Tb);
        double[] carrierFSK1 = modulate_x1(analogFSK, fn1, fs, Tb);
        double[] carrierFSK2 = modulate_x2(analogFSK, fn1, fs, Tb);

        double[] noisyFSK1 = addWhiteNoise2(carrierFSK1, beta, Tc, t);
        double[] noisyFSK2 = addWhiteNoise2(carrierFSK2, beta, Tc, t);

        double[] pFSK1 = modulate_p(noisyFSK1, fs, Tb);
        double[] pFSK2 = modulate_p(noisyFSK2, fs, Tb);
        double[] pFSK = addTwoArrays(pFSK1, pFSK2);
        double[] cFSK = modulate_cfsk(pFSK);
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

    private static final int[][] P = new int[k][r];
    static {
        for(int j=0;j<k;j++){
            for(int i=0;i<r;i++){
                P[j][i] = ((j+1) >> i) & 1;
            }
        }
    }

    public static int[] HammingCodeInternal1511(int[] m) {
        int[] c = new int[n];
        for(int i=0;i<r;i++){
            int sum = 0;
            for(int j=0;j<k;j++){
                sum += m[j]*P[j][i];
            }
            c[i] = sum & 1;
        }
        System.arraycopy(m, 0, c, r, k);
        return c;
    }

    public static List<Integer> HammingDecode1511(List<Integer> coded) {
        List<Integer> msg = new ArrayList<>();
        for(int i=0; i<coded.size(); i+=n) {
            int[] block = new int[n];
            for(int j=0;j<n;j++) block[j] = coded.get(i+j);
            int[] corr = HammingDecodeInternal(block);
            for(int j=0;j<k;j++){
                msg.add(corr[r + j]);
            }
        }
        return msg;
    }

    public static int[] HammingDecodeInternal1511(int[] c) {
        int syndrome = 0;
        for(int i=0;i<r;i++){
            int sum = c[i];
            for(int j=0;j<k;j++){
                sum += c[r+j]*P[j][i];
            }
            if((sum & 1) == 1) syndrome |= 1<<i;
        }
        if(syndrome>0 && syndrome<=n) {
            c[syndrome-1] ^= 1;
        }
        return c;
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

    static double[] addWhiteNoise2(double[] x,double beta,double Tc, double[] t){
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < t.length; j++) {
                y[i] = x[i] * Math.exp(-beta * t[j]) * Math.max(0,1-(t[j]/Tc*0.95));
            }
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

    static double[] generateTime(int bitsLength, double Tb, double fs) {
        int totalSamples = (int)(bitsLength * Tb * fs);
        double[] t = new double[totalSamples];
        for (int i = 0; i < t.length; i++) {
            t[i] = i / fs;
        }
        return t;
    }

    private static void showSingleSeriesChart(String windowTitle,
                                              double[] params,
                                              double[][] bers,
                                              String[] seriesTitles) {

        JPanel panel = new JPanel(new GridLayout(2, 3));

        for (int i = 0; i < 6; i++) {
            XYSeries series = new XYSeries(seriesTitles[i]);
            for (int j = 0; j < params.length; j++) {
                series.add(params[j], bers[i][j]);
            }
            XYSeriesCollection dataset = new XYSeriesCollection(series);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    seriesTitles[i],
                    "param",
                    "BER (%)",
                    dataset
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(300, 250));
            panel.add(chartPanel);
        }

        JFrame frame = new JFrame(windowTitle);
        frame.setContentPane(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
