// sd55617
// funkcje do modulacji/demodulacji byby robione razem z Andriim Zhupanovem
// chat wykonal funkcje do wyswietlania wykresow
import java.awt.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart3d.Chart3D;
import org.jfree.chart3d.Chart3DFactory;
import org.jfree.chart3d.Chart3DPanel;
import org.jfree.data.Range;
import org.jfree.chart3d.data.function.Function3D;
import org.jfree.chart3d.graphics3d.swing.Panel3D;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;


public class Main {
    private static final int n = 15;
    private static final int k = 11;
    private static final int r = n - k;
    public static final int h = 240;

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
                0,0,1,1,0,1,1,0,1,0,0
        };
        System.out.println(bits.length);
        double A1 = 1, A2 = 2, A = 1;
        double Tc = 15;
        double Tb = Tc / bits.length;
        double W  = 2;
        double fn = W / Tb;
        double fn1 = (W+1) / Tb;
        double fn2 = (W+2) / Tb;
        double fs = 2000;

        List<Double> berASK1511 = new ArrayList<>();
        List<Double> berPSK1511 = new ArrayList<>();
        List<Double> berFSK1511 = new ArrayList<>();
        List<Double> berASK74  = new ArrayList<>();
        List<Double> berPSK74  = new ArrayList<>();
        List<Double> berFSK74  = new ArrayList<>();


        double step = 0.2;
        int na = (int)(3.0 / step);
        int total = na * na;

        double[] alphas = new double[total];
        double[] betas  = new double[total];
        int counter = 0;

        for (int i = 0; i < na; i++) {
            double alpha = i * step;
            for (int j = 0; j < na; j++) {
                double beta = j * step;
                alphas[counter] = alpha;
                betas[counter] = beta;
                berASK74.add(askHamming74(bits, A1, A2, fn, fs, Tb, A, alpha, beta, Tc));
                berPSK74.add(pskHamming74(bits, fn, fs, Tb, A, alpha, beta, Tc));
//                berFSK74.add(fskHamming74(bits, fn1, fn2, fs, Tb, alpha, beta, Tc));
//                berASK1511.add(askHamming1511(bits, A1, A2, fn, fs, Tb, A, alpha, beta, Tc));
//                berPSK1511.add(pskHamming1511(bits, fn, fs, Tb, A, alpha, beta, Tc));
//                berFSK1511.add(fskHamming1511(bits, fn1, fn2, fs, Tb, alpha, beta, Tc));
                System.out.println(counter);
                counter++;
            }
        }
        BerPlots.showHeatmap(
                alphas,
                betas,
                berASK74,
                "BER dla ASK Hamming(7,4) Tlumienie -> szum"
        );

        BerPlots.showHeatmap(
                alphas,
                betas,
                berPSK74,
                "BER dla PSK Hamming(7,4) Tlumienie -> szum"
        );

    }

    private static double askHamming74(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha, double beta, double Tc) {

        List<Integer> coded = HammingCode74(bits);

        double[] ASK = za(A1,A2,coded,fn,fs,Tb);

        double[] xASK = xt(ASK,fn,fs,A,0);

        double[] tonedASK = addWhiteNoiseBeta(xASK,Tc,beta);

        double[] noiseASK = addWhiteNoise(tonedASK,alpha);


        double[] pASK = ptASK(noiseASK,fs,Tb);

        int[] cASK = ctASK(pASK,h);

        int[] result = changeToBits(cASK,Tb,fs);

        List<Integer> demodListASK = new ArrayList<>();

        for (int b : result) demodListASK.add(b);

        List<Integer> decoded = HammingDecode74(demodListASK);

//        System.out.println("--------------------ASK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(7,4): " + coded);
//        System.out.println("Po demodulacji : " + demodListASK);
//        System.out.println("Ostatecznie    : " + decoded);
        return findBestBERForASK(pASK,Tb,fs,bits,false);
    }

    private static double pskHamming74(int[] bits, double fn, double fs, double Tb, double A, double alpha, double beta, double Tc) {
        List<Integer> coded = HammingCode74(bits);

        double[] PSK = zp(coded,fn,fs,Tb);

        double[] xPSK = xt(PSK,fn,fs,A,Math.PI);

        double[] tonedPSK = addWhiteNoiseBeta(xPSK,Tc,beta);

        double[] noisePSK = addWhiteNoise(tonedPSK,alpha);

        double[] pPSK = pt(noisePSK,fs,Tb);

        int[] cPSK = ct(pPSK,0);

        int[] result = changeToBits(cPSK,Tb,fs);

        List<Integer> demodListPSK = new ArrayList<>();

        for (int b : result) demodListPSK.add(b);

        List<Integer> decoded = HammingDecode74(demodListPSK);
//        System.out.println("--------------------PSK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(15,11): " + coded);
//        System.out.println("Po demodulacji : " + demodListPSK);
//        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming74(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha, double beta, double Tc) {
        List<Integer> coded = HammingCode74(bits);

        double[] FSK = zf(coded,fn1,fn2,fs,Tb);

        double[] xFSK1 = xt1(FSK,fn1,fs);

        double[] xFSK2 = xt1(FSK,fn2,fs);

        double[] noiseFSK1 = addWhiteNoise(xFSK1,alpha);

        double[] noiseFSK2 = addWhiteNoise(xFSK2,alpha);

        double[] tonedFSK1 = addWhiteNoiseBeta(noiseFSK1,Tc,beta);

        double[] tonedFSK2 = addWhiteNoiseBeta(noiseFSK2,Tc,beta);

        double[] pFSK1 = pt(tonedFSK1,fs,Tb);

        double[] pFSK2 = pt(tonedFSK2,fs,Tb);

        double[] pFSK = SubtractArrays(pFSK1,pFSK2);

        int[] cFSK = ct(pFSK,0);

        int[] result = changeToBits(cFSK,Tb,fs);

        List<Integer> demodListFSK = new ArrayList<>();

        for (int b : result) demodListFSK.add(b);

        List<Integer> decoded = HammingDecode74(demodListFSK);
//        System.out.println("--------------------FSK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(7,4): " + coded);
//        System.out.println("Po demodulacji : " + demodListFSK);
//        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double askHamming1511(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha, double beta, double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] ASK = za(A1,A2,coded,fn,fs,Tb);

        double[] xASK = xt(ASK,fn,fs,A,0);

        double[] noiseASK = addWhiteNoise(xASK,alpha);

        double[] tonedASK = addWhiteNoiseBeta(noiseASK,Tc,beta);

        double[] pASK = ptASK(tonedASK,fs,Tb);

        int[] cASK = ctASK(pASK,h);

        int[] result = changeToBits(cASK,Tb,fs);

        List<Integer> demodListASK = new ArrayList<>();

        for (int b : result) demodListASK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListASK);
//        System.out.println("--------------------ASK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(15,11): " + coded);
//        System.out.println("Po demodulacji : " + demodListASK);
//        System.out.println("Ostatecznie    : " + decoded);
        return findBestBERForASK(pASK,Tb,fs,bits,true);
    }

    private static double pskHamming1511(int[] bits, double fn, double fs, double Tb, double A, double alpha, double beta, double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] PSK = zp(coded,fn,fs,Tb);

        double[] xPSK = xt(PSK,fn,fs,A,Math.PI);

        double[] noisePSK = addWhiteNoise(xPSK,alpha);

        double[] tonedPSK = addWhiteNoiseBeta(noisePSK,Tc,beta);

        double[] pPSK = pt(tonedPSK,fs,Tb);

        int[] cPSK = ct(pPSK,0);

        int[] result = changeToBits(cPSK,Tb,fs);

        List<Integer> demodListPSK = new ArrayList<>();

        for (int b : result) demodListPSK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListPSK);
//        System.out.println("--------------------PSK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(15,11): " + coded);
//        System.out.println("Po demodulacji : " + demodListPSK);
//        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double fskHamming1511(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha, double beta, double Tc) {
        List<Integer> coded = HammingCode1511(bits);

        double[] FSK = zf(coded,fn1,fn2,fs,Tb);

        double[] xFSK1 = xt1(FSK,fn1,fs);

        double[] xFSK2 = xt1(FSK,fn2,fs);

        double[] noiseFSK1 = addWhiteNoise(xFSK1,alpha);

        double[] noiseFSK2 = addWhiteNoise(xFSK2,alpha);

        double[] tonedFSK1 = addWhiteNoiseBeta(noiseFSK1,Tc,beta);

        double[] tonedFSK2 = addWhiteNoiseBeta(noiseFSK2,Tc,beta);

        double[] pFSK1 = pt(tonedFSK1,fs,Tb);

        double[] pFSK2 = pt(tonedFSK2,fs,Tb);

        double[] pFSK = SubtractArrays(pFSK1,pFSK2);

        int[] cFSK = ct(pFSK,0);

        int[] result = changeToBits(cFSK,Tb,fs);

        List<Integer> demodListFSK = new ArrayList<>();

        for (int b : result) demodListFSK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListFSK);
//        System.out.println("--------------------ASK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(15,11): " + coded);
//        System.out.println("Po demodulacji : " + demodListFSK);
//        System.out.println("Ostatecznie    : " + decoded);
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

    public static int[] HammingCodeInternal1511(int[] bits) {
        int[] c = new int[n];
        for(int i=0;i<r;i++){
            int sum = 0;
            for(int j=0;j<k;j++){
                sum += bits[j]*P[j][i];
            }
            c[i] = sum & 1;
        }
        System.arraycopy(bits, 0, c, r, k);
        return c;
    }

    public static List<Integer> HammingDecode1511(List<Integer> coded){
        List<Integer> msg = new ArrayList<>();
        for(int i=0; i<coded.size(); i+=n) {
            int[] block = new int[n];
            for(int j=0;j<n;j++) block[j] = coded.get(i+j);
            int[] corr = HammingDecodeInternal1511(block);
            for(int j=0;j<k;j++){
                msg.add(corr[r + j]);
            }
        }
        return msg;
    }

    public static int[] HammingDecodeInternal1511(int[] coded){
        int syndrome = 0;
        for(int i=0;i<r;i++){
            int sum = coded[i];
            for(int j=0;j<k;j++){
                sum += coded[r+j]*P[j][i];
            }
            if((sum & 1) == 1) syndrome |= 1<<i;
        }
        if(syndrome>0 && syndrome<=n) {
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

    public static double[] za(double A1, double A2, List<Integer> bits, double fn, double fs, double Tb){
        int bitTime = (int)(fs * Tb);
        double[] signal = new double[bits.size() * bitTime];

        for(int n = 0; n < signal.length; n++) {
            double t = (double) n / fs;
            double f = 0;
            if(bits.get(n / bitTime) == 0){
                f = A1 * Math.sin(2 * Math.PI * t * fn);
            }
            else {
                f = A2 * Math.sin(2 * Math.PI * t * fn);
            }
            signal[n] = f;
        }
        return signal;
    }

    public static double[] zf(List<Integer> bits, double fn1, double fn2, double fs, double Tb){
        int bitTime = (int)(fs * Tb);
        double[] signal = new double[bits.size() * bitTime];

        for(int n = 0; n < signal.length; n++) {
            double t = (double) n / fs;
            double s = 0;
            if(bits.get(n / bitTime) == 0){
                s = Math.sin(2 * Math.PI * t * fn1);
            }
            else {
                s =Math.sin(2 * Math.PI * t * fn2);
            }
            signal[n] = s;
        }
        return signal;
    }

    public static double[] zp(List<Integer> bits, double fn, double fs, double Tb){
        int bitTime = (int)(fs * Tb);
        double[] signal = new double[bits.size() * bitTime];

        for(int n = 0; n < signal.length; n++) {
            double t = (double) n / fs;
            double f = 0;
            if(bits.get(n / bitTime) == 0){
                f = Math.sin(2 * Math.PI * t * fn);
            }
            else {
                f = Math.sin(2 * Math.PI * t * fn + Math.PI);
            }
            signal[n] = f;
        }
        return signal;
    }

    public static double[] xt(double[] signal, double fn, double fs, double A, double fi){
        double[] res = new double[signal.length];
        for(int n = 0; n < signal.length; n++) {
            double t = (double) n / fs;
            res[n] = signal[n] * A * Math.sin(2 * Math.PI * t * fn + fi);
        }

        return res;
    }

    public static double[] xt1(double[] signal, double fn, double fs){
        double[] res = new double[signal.length];
        for(int n = 0; n < signal.length; n++) {
            double t = (double) n / fs;
            res[n] = signal[n] * Math.sin(2 * Math.PI * t * fn);
        }

        return res;
    }

    public static double[] pt(double[] xt, double fs, double Tb){
        int bitTime = (int)(fs * Tb);
        int numBits = xt.length / bitTime;
        LinkedList<Double> res = new LinkedList<>();
        for(int i = 0; i < numBits; i++) {
            double sum = 0;
            for(int y = i * bitTime; y < (i + 1) * bitTime; y++ ) {
                sum += xt[y];
                res.add(sum);
            }
        }

        double[] pt = new double[res.size()];
        for(int i = 0; i < pt.length; i++) {
            pt[i] = res.get(i);
        }
        return pt;
    };

    public static int[] ct(double[] pt, double h) {
        int[] ct = new int[pt.length];
        for (int i = 0; i < pt.length; i++) {
            if (pt[i] > h) {
                ct[i] = 1;
            } else {
                ct[i] = 0;
            }
        }
        return ct;
    }

    public static int[] changeToBits(int[] ct, double Tb, double fs){
        int bitTime = (int)(fs * Tb);
        int numBits = ct.length / bitTime;
        LinkedList<Integer> res = new LinkedList<>();
        for(int i = 0; i < numBits; i++) {
            int w = 0;
            int n = 0;
            for(int y = i * bitTime; y < (i + 1) * bitTime; y++ ) {
                if(ct[y] != 0){
                    w++;
                }
                else{
                    n++;
                }
            }
            if(w > n){
                res.add(1);
            }
            else{
                res.add(0);
            }
        }

        int[] bits = new int[res.size()];
        for(int i = 0; i < bits.length; i++) {
            bits[i] = res.get(i);
        }
        return bits;
    };

    public static double[] SubtractArrays(double[] pt1, double[] pt2){
        double[] result = new double[pt1.length];
        for(int i = 0; i < pt1.length; i++){
            result[i] = pt2[i] - pt1[i];
        }

        return result;
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

    static double[] addWhiteNoiseBeta(double[] x, double Tc, double beta) {
        double[] result = new double[x.length];
        double t0 = Tc*0.95;
        for (int i = 0; i < x.length; i++) {
            double t = (i*Tc)/x.length;
            result[i] = x[i] * Math.exp(-beta * t) * Math.max(0,1-(t/t0));
        }
        return result;
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

    private static double findBestBERForASK(double[] pt, double Tb, double fs, int[] bits, boolean is1511) {
        double min = Arrays.stream(pt).min().orElse(0);
        double max = Arrays.stream(pt).max().orElse(1);
        double bestBER = 100.0;

        for (int j = 0; j <= 50; j++) {
            double h = min + (max - min) * j / 50.0;
            int[] bitDecisions = new int[pt.length];
            for (int i = 0; i < pt.length; i++) {
                bitDecisions[i] = (pt[i] > h) ? 1 : 0;
            }
            List<Integer> demodList = new ArrayList<>();
            for (int b : bitDecisions) demodList.add(b);

            List<Integer> decoded;
            if (is1511)
                decoded = HammingDecode1511(demodList);
            else
                decoded = HammingDecode74(demodList);

            double ber = calculateBER(bits, decoded);
            if (ber < bestBER) bestBER = ber;
        }

        return bestBER;
    }

    public static int[] ctASK(double[] pt, double h) {
        int[] ct = new int[pt.length];
        for (int i = 0; i < pt.length; i++) {
            if (pt[i] > h) {
                ct[i] = 0;
            } else {
                ct[i] = 1;
            }
        }
        return ct;
    }

    public static double[] ptASK(double[] xt, double fs, double Tb){
        int bitTime = (int)(fs * Tb);
        int numBits = xt.length / bitTime;
        LinkedList<Double> res = new LinkedList<>();
        for(int i = 0; i < numBits; i++) {
            double sum = 0;
            for(int y = i * bitTime; y < (i + 1) * bitTime; y++ ) {
                sum += Math.abs(xt[y]);
            }
            res.add(sum);
        }

        double[] pt = new double[res.size()];
        for(int i = 0; i < pt.length; i++) {
            pt[i] = res.get(i);
        }
        return pt;
    }


}

