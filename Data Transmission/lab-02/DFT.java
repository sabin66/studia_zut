// Dorian Sobieranski 55617
// Wszystkie funkcje PlotSignal itp. wygenerowane przez chatGPT
// displayLogXChart zrobiony pod koniec zajęc razem z Andrzejem Żwirko, by poprawic wyswietlanie 
// logarytmicznej skali
import org.jfree.chart.*;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.*;
import org.jfree.data.xy.*;
import javax.swing.*;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.XYPlot;

public class Main {
    public static void main(String[] args) {

        XYSeries amp = new XYSeries("m(k)");
        XYSeries sin = new XYSeries("x(t)");
        XYSeries amp_db = new XYSeries("mprim(k)");
        //////////---------------- PARAMETRY----------------- //////////
        int A = 1;
        double freq = 450;
        double samplePeriod = 1;
        double sampleFreq = 1600;
        int N = (int) Math.round(samplePeriod * sampleFreq);
        //////////---------------- PARAMETRY----------------- //////////

        double[] signal = new double[N];
        for (int n = 0; n < N; n++) {
            signal[n] = A * Math.sin(2 * Math.PI * freq * n / sampleFreq);
            sin.add(n/sampleFreq, signal[n]);
        }

        // DFT
        double[] real = new double[N];
        double[] imag = new double[N];
        fourier(signal,real,imag,N);
        for (int k = 0; k < N; k++) {
            System.out.println(String.format("%.2f + i * %.8f", real[k], imag[k]));
        }

        // widmo amplitudowe + skala czestotliwosci
        for(int j = 0; j < (N / 2); j++) {
            double mx = Math.sqrt(real[j] * real[j] + imag[j] * imag[j]);
            double fk = j * (sampleFreq / N);
            double mxprim = 10 * Math.log10(mx);
            amp.add(fk, mx);
            amp_db.add(fk, mxprim);
        }
        PlotSignal(amp,"m(k) - widmo amplitudowe","Frequency","Amplitude");
        PlotSignal(sin,"x(t) - przebieg tonu prostego","Time","Amplitude");
        PlotSignal(amp_db,"m'(k) - wartosci amplitudy w skali decybelowej","Frequency","Amplitude [db]");

        //////////---------------- ZADANIE 4----------------- //////////
        XYSeries threeToneSignal = new XYSeries("x(t)");
        XYSeries threeToneSignal_linear = new XYSeries("x(t)");
        XYSeries threeToneSignal_log = new XYSeries("x(t)");
        double fs = 2000;
        double freq1 = 10;
        double freq2 = fs/2 - freq1;
        double freq3 = freq1/2;
        int N1 = (int) Math.round(samplePeriod * fs);
        double[] signalSum = new double[N1];
        for (int n = 0; n < N1; n++) {
            signalSum[n] = A * (Math.sin(2 * Math.PI * freq1 * n / fs)+
                                Math.sin(2 * Math.PI * freq2 * n / fs)+
                                Math.sin(2 * Math.PI * freq3 * n / fs));
            threeToneSignal.add(n/fs, signalSum[n]);
        }
        PlotSignal(threeToneSignal,"Sygnał 3 tonów","Time","Amplitude");

        double[] real3 = new double[N1];
        double[] imag3 = new double[N1];
        fourier(signalSum,real3,imag3,N1);
        for(int j = 0; j < (N1 / 2); j++) {
            double mx3 = Math.sqrt(real3[j] * real3[j] + imag3[j] * imag3[j]);
            double fk3 = j * (fs / N1);
            double fk3prim = Math.log10(fk3);
            double mx3prim = 10 * Math.log10(mx3);
            if(j > 0 && fk3 > 0){
                threeToneSignal_linear.add(fk3, mx3);
                threeToneSignal_log.add(fk3prim, mx3prim);
            }
        }
        PlotSignal(threeToneSignal_linear,"Widmo amplitudowe - liniowo","Frequency","Amplitude");
        displayLogXChart(threeToneSignal_log,"Widmo amplitudowe - logarytmicznie","Frequency [log10(Hz)]","Amplitude");



    }
    public static void fourier(double[] x, double[] real, double[] imag, int N){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                double angle = -2 * Math.PI * i * j / N;
                real[i] += x[j] * Math.cos(angle);
                imag[i] += x[j] * Math.sin(angle);
            }
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
    static void displayLogXChart(XYSeries series, String title, String xLabel, String yLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        // Ustawienie logarytmicznej osi X
        XYPlot plot = (XYPlot) chart.getPlot();
        LogarithmicAxis logAxis = new LogarithmicAxis(xLabel);
        //plot.setDomainAxis(logAxis);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}