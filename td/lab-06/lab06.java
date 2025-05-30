// Dorian Sobieranski 55617
// implementacja PlotNineSignalInOneWindow oraz PlotSignal stworzona z chatGPT
// kod mojego autorstwa, chat zostal poproszony o zmiane/poprawe kodu
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
            XYSeries askSeries = new XYSeries("Ask");
            XYSeries fskSeries = new XYSeries("Fsk");
            XYSeries pskSeries = new XYSeries("Psk");
            XYSeries bpskSeries = new XYSeries("Bpsk");

            double Tb = 0.1;
            double sampleFreq = 8000;
            int W = 2;
            int[] bits = {0,1,0,0,1,1,0,1,0,1};
            double freq = W/Tb;
            double fn1 = (W+1)/Tb;
            double fn2 = (W+2)/Tb;
            double A1 = 1;
            double A2 = 2;
            double t = 0.0;
            double dt = 1.0/sampleFreq;
            double ask,fsk,psk,bpsk;

            for(int i = 0; i < bits.length; i++) {
                for(double time = 0.0;time < Tb; time+=dt){
                    if(bits[i]==0){
                        ask = A1 * Math.sin(2*Math.PI * freq * t);
                        fsk = Math.sin(2*Math.PI * fn1 * t);
                        psk = Math.sin(2*Math.PI * freq * t);
                        bpsk = 1;
                    }else{
                        ask = A2 * Math.sin(2*Math.PI * freq * t);
                        fsk = Math.sin(2*Math.PI * fn2 * t);
                        psk = Math.sin(2*Math.PI * freq * t + Math.PI);
                        bpsk = 0;
                    }
                    askSeries.add(t,ask);
                    fskSeries.add(t,fsk);
                    pskSeries.add(t,psk);
                    bpskSeries.add(t,bpsk);
                    t+=dt;
                }
            }

            PlotThreeSignalsInOneWindow(askSeries,fskSeries,pskSeries,bpskSeries,"signal","time","amp");

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
    public static void PlotThreeSignalsInOneWindow(
            XYSeries s1, XYSeries s2, XYSeries s3,XYSeries s4,
            String title, String xLabel, String yLabel
    ) {
        XYSeries[] seriesArray = {s1, s2, s3, s4};
        String[] labels = {"ASK", "FSK", "PSK","BPSK"};

        JPanel mainPanel = new JPanel(new GridLayout(4, 1));

        for (int i = 0; i < 4; i++) {
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(seriesArray[i]);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    labels[i], xLabel, yLabel,
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