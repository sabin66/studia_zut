// sd55617
// zadanie bylo robione razem z Andriim Zhupanovem
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.awt.Dimension;
import javax.swing.JFrame;
import java.util.LinkedList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Main {
    private static final int n = 15;
    private static final int k = 11;
    private static final int r = n - k;
    public static final int h = 540;

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
        double Tc = 20;
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

        int counter = 0;
        double[] alphas = new double[40];

        System.out.println("BER ASK(7/4), alpha=0: " + askHamming74(bits, A1, A2, fn, fs, Tb, A, 0.0));
        System.out.println("BER PSK(7/4), alpha=0: " + pskHamming74(bits, fn, fs, Tb, A, 0.0));
        System.out.println("BER FSK(7/4), alpha=0: " + fskHamming74(bits, fn1, fn2, fs, Tb, 0.0));

        System.out.println("BER ASK(15/11), alpha=0: " + askHamming1511(bits, A1, A2, fn, fs, Tb, A, 0.0));
        System.out.println("BER PSK(15/11), alpha=0: " + pskHamming1511(bits, fn, fs, Tb, A, 0.0));
        System.out.println("BER FSK(15/11), alpha=0: " + fskHamming1511(bits, fn1, fn2, fs, Tb, 0.0));

        for(double alpha = 0; alpha < 12; alpha+=0.3) {
            alphas[counter] = alpha;
            berASK74.add(askHamming74(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK74.add(pskHamming74(bits, fn, fs, Tb, A, alpha));
            berFSK74.add(fskHamming74(bits, fn1, fn2, fs, Tb, alpha));
            berASK1511.add(askHamming1511(bits, A1, A2, fn, fs, Tb, A, alpha));
            berPSK1511.add(pskHamming1511(bits, fn, fs, Tb, A, alpha));
            berFSK1511.add(fskHamming1511(bits, fn1, fn2, fs, Tb, alpha));
            System.out.println(counter);
            counter++;
        }
        showCombinedPlot(berASK1511, berPSK1511, berFSK1511, berASK74, berPSK74, berFSK74, alphas);
    }

    private static void showCombinedPlot(
            List<Double> berASK1511, List<Double> berPSK1511, List<Double> berFSK1511,
            List<Double> berASK74, List<Double> berPSK74, List<Double> berFSK74, double[] alphas) {

        XYSeriesCollection dataset = new XYSeriesCollection();

        String[] labels = {"ASK 15/11", "PSK 15/11", "FSK 15/11", "ASK 7/4", "PSK 7/4", "FSK 7/4"};
        List<List<Double>> all = List.of(berASK1511, berPSK1511, berFSK1511, berASK74, berPSK74, berFSK74);

        for (int i = 0; i < all.size(); i++) {
            XYSeries series = new XYSeries(labels[i]);
            for (int j = 0; j < alphas.length; j++) {
                series.add(alphas[j], all.get(i).get(j));
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                "BER vs α (dla wszystkich modulacji i kodów Hamminga)",
                "α (poziom szumu)",
                "BER (%)",
                dataset
        );

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 500));

        JFrame frame = new JFrame("Zbiorczy wykres BER");
        frame.setContentPane(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


    private static double askHamming74(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {

        List<Integer> coded = HammingCode74(bits);

        double[] ASK = za(A1,A2,coded,fn,fs,Tb);

        double[] xASK = xt(ASK,fn,fs,A,0);

        double[] noiseASK = addWhiteNoise(xASK,alpha);

        double[] pASK = pt(noiseASK,fs,Tb);

        int[] cASK = ct(pASK,h);

        int[] result = changeToBits(cASK,Tb,fs);

        List<Integer> demodListASK = new ArrayList<>();

        for (int b : result) demodListASK.add(b);

        List<Integer> decoded = HammingDecode74(demodListASK);

//        System.out.println("--------------------ASK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(7,4): " + coded);
//        System.out.println("Po demodulacji : " + demodListASK);
//        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double pskHamming74(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] PSK = zp(coded,fn,fs,Tb);

        double[] xPSK = xt(PSK,fn,fs,A,Math.PI);

        double[] noisePSK = addWhiteNoise(xPSK,alpha);

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

    private static double fskHamming74(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode74(bits);

        double[] FSK = zf(coded,fn1,fn2,fs,Tb);

        double[] xFSK1 = xt1(FSK,fn1,fs);

        double[] xFSK2 = xt1(FSK,fn2,fs);

        double[] noiseFSK1 = addWhiteNoise(xFSK1,alpha);

        double[] noiseFSK2 = addWhiteNoise(xFSK2,alpha);

        double[] pFSK1 = pt(noiseFSK1,fs,Tb);

        double[] pFSK2 = pt(noiseFSK2,fs,Tb);

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

    private static double askHamming1511(int[] bits, double A1, double A2, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] ASK = za(A1,A2,coded,fn,fs,Tb);

        double[] xASK = xt(ASK,fn,fs,A,0);

        double[] noiseASK = addWhiteNoise(xASK,alpha);

        double[] pASK = pt(noiseASK,fs,Tb);

        int[] cASK = ct(pASK,h);

        int[] result = changeToBits(cASK,Tb,fs);

        List<Integer> demodListASK = new ArrayList<>();

        for (int b : result) demodListASK.add(b);

        List<Integer> decoded = HammingDecode1511(demodListASK);
//        System.out.println("--------------------ASK--------------------");
//        System.out.println("Wejściowe      : " + Arrays.toString(bits));
//        System.out.println("Po Hamming(15,11): " + coded);
//        System.out.println("Po demodulacji : " + demodListASK);
//        System.out.println("Ostatecznie    : " + decoded);
        return calculateBER(bits,decoded);
    }

    private static double pskHamming1511(int[] bits, double fn, double fs, double Tb, double A, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] PSK = zp(coded,fn,fs,Tb);

        double[] xPSK = xt(PSK,fn,fs,A,Math.PI);

        double[] noisePSK = addWhiteNoise(xPSK,alpha);

        double[] pPSK = pt(noisePSK,fs,Tb);

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

    private static double fskHamming1511(int[] bits,double fn1,double fn2, double fs, double Tb, double alpha) {
        List<Integer> coded = HammingCode1511(bits);

        double[] FSK = zf(coded,fn1,fn2,fs,Tb);

        double[] xFSK1 = xt1(FSK,fn1,fs);

        double[] xFSK2 = xt1(FSK,fn2,fs);

        double[] noiseFSK1 = addWhiteNoise(xFSK1,alpha);

        double[] noiseFSK2 = addWhiteNoise(xFSK2,alpha);

        double[] pFSK1 = pt(noiseFSK1,fs,Tb);

        double[] pFSK2 = pt(noiseFSK2,fs,Tb);

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
        for(int i = 0; i < pt1.length; i++){
            pt1[i] = pt2[i] - pt1[i];
        }

        return pt1;
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


}
