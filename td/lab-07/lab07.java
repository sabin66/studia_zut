// Dorian Sobieranski 55617
// implementacja PlotSignal stworzona z chatGPT

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        XYSeries askSeries = new XYSeries("Ask");
        XYSeries fskSeries = new XYSeries("Fsk");
        XYSeries pskSeries = new XYSeries("Psk");

        double Tb = 0.1;
        double sampleFreq = 8000;
        int W = 30;
        int[] bits = {0,1,0,0,1,1,0,1,0,1,1,1,0,0,0,1,0};
        double freq = W/Tb;
        double fn1 = (W+1)/Tb;
        double fn2 = (W+2)/Tb;
        double A1 = 1;
        double A2 = 2;
        double t = 0.0;
        double dt = 1.0/sampleFreq;
        ArrayList<Double> askList = new ArrayList<Double>();
        ArrayList<Double> fskList = new ArrayList<Double>();
        ArrayList<Double> pskList = new ArrayList<Double>();

        for(int i = 0; i < bits.length; i++) {
            for(double time = 0.0;time < Tb; time+=dt){
                double ask,fsk,psk;
                if(bits[i]==0){
                    ask = A1 * Math.sin(2*Math.PI * freq * t);
                    fsk = Math.sin(2*Math.PI * fn1 * t);
                    psk = Math.sin(2*Math.PI * freq * t);
                }else{
                    ask = A2 * Math.sin(2*Math.PI * freq * t);
                    fsk = Math.sin(2*Math.PI * fn2 * t);
                    psk = Math.sin(2*Math.PI * freq * t + Math.PI);
                }
                askList.add(ask);
                fskList.add(fsk);
                pskList.add(psk);

                askSeries.add(t,ask);
                fskSeries.add(t,fsk);
                pskSeries.add(t,psk);
                t+=dt;
            }
        }

        PlotThreeSignalsInOneWindow(askSeries,fskSeries,pskSeries,"signal","time","amp");


        int N = 1024;
        XYSeries[] series = new XYSeries[3];
        double[][] real = new double[3][N];
        double[][] imag = new double[3][N];

        double[] B = {3.0,6.0,10.0};

        double[] ask = new double[askList.size()];
        double[] fsk = new double[fskList.size()];
        double[] psk = new double[pskList.size()];

        for (int i = 0; i < askList.size(); i++) {
            ask[i] = askList.get(i);
            fsk[i] = fskList.get(i);
            psk[i] = pskList.get(i);
        }

        String[] nazwy = {"ASKm","FSKm","PSKm"};

        double[][] input = {
                Arrays.copyOf(ask,N),
                Arrays.copyOf(fsk,N),
                Arrays.copyOf(psk,N),
        };

        for(int i = 0; i < input.length; i++) {
            series[i] = new XYSeries(nazwy[i]);
            real[i] = input[i];
            imag[i] = new double[N];
            fastFourierTransform(real[i],imag[i],N);
        }

        double[][] magnitude = new double[9][N/2];
        double[] maxVal = new double[9];
        Arrays.fill(maxVal,0.0);
        for(int j = 0; j < N/2;j++){
            double fk = j * (sampleFreq/N);
            for(int i = 0; i < input.length; i++) {
                double mag = Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]);
                double magDB = 10  * Math.log(mag + 0.000000001);
                magnitude[i][j] = magDB;
                series[i].add(fk,magDB);
                if(magDB > maxVal[i]){
                    maxVal[i] = magDB;
                }
            }
        }
        for(int k = 0; k < 3;k++){
            System.out.println("Maksymalna wartosc amplitudy dla sygnalu : " +  (k+1) + " : " + maxVal[k]);
        }
        PlotThreeSignalsInOneWindow(series[0],series[1],series[2],"widma aplitudowe","freq","amp");

        for(int i = 0; i < 3;i++){
            for(int k = 0;k <= 2;k++){
                double h = maxVal[i] - B[k];
                int leftIndex = -1;
                int rightIndex = -1;

                for(int j = 0; j < N/2;j++){
                    if(magnitude[i][j+1] > h && magnitude[i][j] < h){
                        leftIndex = j;
                        break;
                    }
                }
                for(int j = (N/2)-2;j>0;j--){
                    if(magnitude[i][j] > h && magnitude[i][j+1] < h){
                        rightIndex = j;
                        break;
                    }
                }
                if(leftIndex != -1 && rightIndex != -1){
                    double left = leftIndex * (sampleFreq/N);
                    double right = rightIndex * (sampleFreq/N);
                    double width = right - left;
                    System.out.printf("Szerokość pasma sygnału %d (-"+B[k] + "dB): %.2f Hz\n", i + 1, width);
                }
            }

        }


    }

    public static void PlotThreeSignalsInOneWindow(
            XYSeries s1, XYSeries s2, XYSeries s3,
            String title, String xLabel, String yLabel
    ) {
        XYSeries[] seriesArray = {s1, s2, s3};
        String[] labels = {"ASK", "FSK", "PSK"};

        JPanel mainPanel = new JPanel(new GridLayout(4, 1));

        for (int i = 0; i < 3; i++) {
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

}