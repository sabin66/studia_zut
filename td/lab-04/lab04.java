// Dorian Sobieranski 55617
// implementacja PlotSignal,PlotNineSignalInOneWindow, FFT oraz PlotTwoSignals stworzona z chatGPT
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        XYSeries msig = new XYSeries("MSIG");
        XYSeries z1sig = new XYSeries("ZSIG");
        XYSeries z2sig = new XYSeries("ZSIG");
        XYSeries z3sig = new XYSeries("ZSIG");
        XYSeries p1sig = new XYSeries("ZSIG");
        XYSeries p2sig = new XYSeries("ZSIG");
        XYSeries p3sig = new XYSeries("ZSIG");
        XYSeries f1sig = new XYSeries("ZSIG");
        XYSeries f2sig = new XYSeries("ZSIG");
        XYSeries f3sig = new XYSeries("ZSIG");
        double samplePeriod = 1.024;
        double sampleFreq = 2000;
        double mFreq = 10;
        double nFreq = sampleFreq/4;
        int N = (int) Math.round(samplePeriod * sampleFreq);

        double[] mt = new double[N];
        double[] za1 = new double[N];
        double[] za2 = new double[N];
        double[] za3 = new double[N];
        double[] pa1 = new double[N];
        double[] pa2 = new double[N];
        double[] pa3 = new double[N];
        double[] fa1 = new double[N];
        double[] fa2 = new double[N];
        double[] fa3 = new double[N];

        double[] kval = {0.5,5.0,25.0};
        double[] phaseVals = {0.5,Math.PI/2,Math.PI*3};
        double[] freqVals = {0.5,Math.PI/2,Math.PI*3};

        for(int i = 0; i < N; i++){
            double t = i / sampleFreq;
            mt[i] = Math.sin(Math.PI * 2 * mFreq * t);
            za1[i] = (kval[0] * mt[i] + 1) * Math.cos(Math.PI * 2 * nFreq * t);
            za2[i] = (kval[1] * mt[i] + 1) * Math.cos(Math.PI * 2 * nFreq * t);
            za3[i] = (kval[2] * mt[i] + 1) * Math.cos(Math.PI * 2 * nFreq * t);

            pa1[i] = Math.cos(Math.PI * 2 * nFreq * t + phaseVals[0] * mt[i]);
            pa2[i] = Math.cos(Math.PI * 2 * nFreq * t + phaseVals[1] * mt[i]);
            pa3[i] = Math.cos(Math.PI * 2 * nFreq * t + phaseVals[2] * mt[i]);

            fa1[i] = Math.cos(Math.PI*2*nFreq * t + (freqVals[0]/mFreq) * mt[i]);
            fa2[i] = Math.cos(Math.PI*2*nFreq * t + (freqVals[1]/mFreq) * mt[i]);
            fa3[i] = Math.cos(Math.PI*2*nFreq * t + (freqVals[2]/mFreq) * mt[i]);

            msig.add(t,mt[i]);
            z1sig.add(t,za1[i]);
            z2sig.add(t,za2[i]);
            z3sig.add(t,za3[i]);

            p1sig.add(t, pa1[i]);
            p2sig.add(t, pa2[i]);
            p3sig.add(t, pa3[i]);

            f1sig.add(t,fa1[i]);
            f2sig.add(t,fa2[i]);
            f3sig.add(t,fa3[i]);

        }

        PlotTwoSignals(msig, z1sig,"sygnal i modyfikacja amplitudy k =" +kval[0] ,"czas","amp");
        PlotTwoSignals(msig, z2sig,"sygnal i modyfikacja amplitudy k =" +kval[1] ,"czas","amp");
        PlotTwoSignals(msig, z3sig,"sygnal i modyfikacja amplitudy k =" +kval[2] ,"czas","amp");

        PlotTwoSignals(msig, p1sig,"sygnal i modyfikacja fazy k =" + phaseVals[0] ,"czas","amp");
        PlotTwoSignals(msig, p2sig,"sygnal i modyfikacja fazy k =" + phaseVals[1] ,"czas","amp");
        PlotTwoSignals(msig, p3sig,"sygnal i modyfikacja fazy k =" + phaseVals[2] ,"czas","amp");

        PlotTwoSignals(msig, f1sig,"sygnal i modyfikacja czestotliwosci k =" + freqVals[0] ,"czas","amp");
        PlotTwoSignals(msig, f2sig,"sygnal i modyfikacja czestotliwosci k =" + freqVals[1] ,"czas","amp");
        PlotTwoSignals(msig, f3sig,"sygnal i modyfikacja czestotliwosci k =" + freqVals[2] ,"czas","amp");
        // czesc 2
        XYSeries m1 = new XYSeries("M1");
        XYSeries m2 = new XYSeries("M2");
        XYSeries m3 = new XYSeries("M3");
        XYSeries m4 = new XYSeries("M4");
        XYSeries m5 = new XYSeries("M5");
        XYSeries m6 = new XYSeries("M6");
        XYSeries m7 = new XYSeries("M7");
        XYSeries m8 = new XYSeries("M8");
        XYSeries m9 = new XYSeries("M9");
        double[] real1 = Arrays.copyOf(za1,N);
        double[] imag1 = new double[N];
        double[] real2 = Arrays.copyOf(za2,N);
        double[] imag2 = new double[N];
        double[] real3 = Arrays.copyOf(za3,N);
        double[] imag3 = new double[N];
        double[] real4 = Arrays.copyOf(pa1,N);
        double[] imag4 = new double[N];
        double[] real5 = Arrays.copyOf(pa2,N);
        double[] imag5 = new double[N];
        double[] real6 = Arrays.copyOf(pa3,N);
        double[] imag6 = new double[N];
        double[] real7 = Arrays.copyOf(fa1,N);
        double[] imag7 = new double[N];
        double[] real8 = Arrays.copyOf(fa2,N);
        double[] imag8 = new double[N];
        double[] real9 = Arrays.copyOf(fa3,N);
        double[] imag9 = new double[N];
        fastFourierTransform(real1,imag1,N);
        fastFourierTransform(real2,imag2,N);
        fastFourierTransform(real3,imag3,N);
        fastFourierTransform(real4,imag4,N);
        fastFourierTransform(real5,imag5,N);
        fastFourierTransform(real6,imag6,N);
        fastFourierTransform(real7,imag7,N);
        fastFourierTransform(real8,imag8,N);
        fastFourierTransform(real9,imag9,N);
        for(int j = 0;j < (N/2);j++){
            double fk = j * (sampleFreq/N);
            double msig1 = Math.sqrt(real1[j] * real1[j] + imag1[j] * imag1[j]);
            double msig2 = Math.sqrt(real2[j] * real2[j] + imag2[j] * imag2[j]);
            double msig3 = Math.sqrt(real3[j] * real3[j] + imag3[j] * imag3[j]);
            double msig4 = Math.sqrt(real4[j] * real4[j] + imag4[j] * imag4[j]);
            double msig5 = Math.sqrt(real5[j] * real5[j] + imag5[j] * imag5[j]);
            double msig6 = Math.sqrt(real6[j] * real6[j] + imag6[j] * imag6[j]);
            double msig7 = Math.sqrt(real7[j] * real7[j] + imag7[j] * imag7[j]);
            double msig8 = Math.sqrt(real8[j] * real8[j] + imag8[j] * imag8[j]);
            double msig9 = Math.sqrt(real9[j] * real9[j] + imag9[j] * imag9[j]);
            m1.add(fk,msig1);
            m2.add(fk,msig2);
            m3.add(fk,msig3);
            m4.add(fk,msig4);
            m5.add(fk,msig5);
            m6.add(fk,msig6);
            m7.add(fk,msig7);
            m8.add(fk,msig8);
            m9.add(fk,msig9);
        }
        PlotNineSignalsInOneWindow(m1,m2,m3,m4,m5,m6,m7,m8,m9,"widma amplitudowe","freq","amp");


    }
    public static void fastFourierTransform(double[] real, double[] imag, int n) {
        if((n & (n-1)) != 0){
            throw new IllegalArgumentException("n must be a power of 2");
        }
        if(n <= 1){
            return;
        }
        int half = n/2;
        double[] realEven = new double[half];
        double[] imagEven = new double[half];
        double[] realOdd = new double[half];
        double[] imagOdd = new double[half];

        for(int i = 0; i < half; i++){
            realEven[i] = real[2*i];
            imagEven[i] = imag[2*i];
            realOdd[i] = real[2*i+1];
            imagOdd[i] = imag[2*i+1];
        }

        fastFourierTransform(realEven, imagEven, half);
        fastFourierTransform(realOdd, imagOdd, half);

        for(int i = 0; i < half; i++){
            double angle = -2 * Math.PI * i / n;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double realOddPart = realOdd[i] * cos - imagOdd[i] * sin;
            double imagOddPart = realOdd[i] * sin + imagOdd[i] * cos;

            real[i] = realEven[i] + realOddPart;
            imag[i] = imagEven[i] + imagOddPart;

            real[i + half] = realEven[i] - realOddPart;
            imag[i + half] = imagEven[i] - imagOddPart;
        }


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
    public static void PlotTwoSignals(XYSeries series1, XYSeries series2, String title, String xLabel, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel,
                dataset, PlotOrientation.VERTICAL,
                true, true, false 
        );

        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);
    }
    public static void PlotNineSignalsInOneWindow(
            XYSeries s1, XYSeries s2, XYSeries s3,
            XYSeries s4, XYSeries s5, XYSeries s6,
            XYSeries s7, XYSeries s8, XYSeries s9,
            String title, String xLabel, String yLabel
    ) {
        XYSeries[] seriesArray = {s1, s2, s3, s4, s5, s6, s7, s8, s9};

        JPanel mainPanel = new JPanel(new GridLayout(3, 3));

        for (int i = 0; i < 9; i++) {
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(seriesArray[i]);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    title + " " + (i + 1), xLabel, yLabel,
                    dataset, PlotOrientation.VERTICAL,
                    false, true, false
            );

            ChartPanel chartPanel = new ChartPanel(chart);
            mainPanel.add(chartPanel);
        }

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

}
