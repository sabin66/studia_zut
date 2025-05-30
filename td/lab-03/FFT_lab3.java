// liczba prazkow - piloksztalny :11, trojkatny i prostokatny : 6
// Dorian Sobieranski 55617
// implementacja FFT oraz SignalPlot stworzona z chatGPT
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        XYSeries sawshape = new XYSeries("Sawshape");
        XYSeries triangle = new XYSeries("Triangle");
        XYSeries rectangle = new XYSeries("Rectangle");
        XYSeries m_sawshape = new XYSeries("Sawshape spectrum");
        XYSeries m_triangle = new XYSeries("Triangle spectrum");
        XYSeries m_rectangle = new XYSeries("Rectangle spectrum");

        int H = 1;
        int H2 = 1;
        int m = 1;
        int l = 1;
        double freq = 50;
        double samplePeriod = 0.128;
        double sampleFreq = 1000;
        int N = (int) Math.round(samplePeriod * sampleFreq);

        while (true) {
            double alfa = m * freq;
            if (alfa > sampleFreq / 2) {
                break;
            }
            m += 1;
            H += 1;
        }
        System.out.println("H ( dla piloksztaltnego ): " + H);

        while (true) {
            double alfa1 = (2 * l - 1) * freq;
            if (alfa1 > sampleFreq / 2) {
                break;
            }
            l += 1;
            H2 += 1;
        }
        System.out.println("H2 (dla prostokatnego i trojkatnego ) : " + H2);

        double[] sig1 = new double[N];
        double[] sig2 = new double[N];
        double[] sig3 = new double[N];

        for (int i = 0; i < N; i++) {
            double t = i / sampleFreq;
            double sum1 = 0;
            double sum2 = 0;
            double sum3 = 0;

            for (int k = 1; k <= H; k++) {
                sum1 += Math.pow(-1.0, k + 1) * Math.sin(2.0 * Math.PI * k * freq * t) / k;
            }
            for (int k = 1; k <= H2; k += 2) {
                sum2 += Math.pow(-1.0, ((k / 2) - 1)) * Math.sin(2.0 * Math.PI * k * freq * t) / (k * k);
            }
            for (int k = 1; k <= H2; k += 2) {
                sum3 += Math.sin(2.0 * Math.PI * k * freq * t) / k;
            }

            sig1[i] = sum1 * (2.0 / Math.PI);
            sig2[i] = sum2 * (8.0 / (Math.PI * Math.PI));
            sig3[i] = sum3 * (4.0 / Math.PI);

            sawshape.add(t, sig1[i]);
            triangle.add(t, sig2[i]);
            rectangle.add(t, sig3[i]);
        }


        PlotSignal(sawshape, "Sawtooth Wave", "Time [s]", "Amplitude");
        PlotSignal(triangle, "Triangle Wave", "Time [s]", "Amplitude");
        PlotSignal(rectangle, "Rectangle Wave", "Time [s]", "Amplitude");

        double[] real1 = Arrays.copyOf(sig1, N);
        double[] imag1 = new double[N];
        double[] real2 = Arrays.copyOf(sig2, N);
        double[] imag2 = new double[N];
        double[] real3 = Arrays.copyOf(sig3, N);
        double[] imag3 = new double[N];

        fastFourierTransform(real1, imag1, N);
        fastFourierTransform(real2, imag2, N);
        fastFourierTransform(real3, imag3, N);

        for (int j = 0; j < (N / 2); j++) {
            double fk = j * (sampleFreq / N);
            double msig1 = Math.sqrt(real1[j] * real1[j] + imag1[j] * imag1[j]);
            double msig2 = Math.sqrt(real2[j] * real2[j] + imag2[j] * imag2[j]);
            double msig3 = Math.sqrt(real3[j] * real3[j] + imag3[j] * imag3[j]);
            m_sawshape.add(fk, msig1);
            m_triangle.add(fk, msig2);
            m_rectangle.add(fk, msig3);
        }

        PlotSignal(m_sawshape, "Sawtooth Spectrum", "Frequency [Hz]", "Amplitude");
        PlotSignal(m_triangle, "Triangle Spectrum", "Frequency [Hz]", "Amplitude");
        PlotSignal(m_rectangle, "Rectangle Spectrum", "Frequency [Hz]", "Amplitude");

        // czesc 2

        XYSeries xSeries = new XYSeries("x(t)");
        XYSeries ySeries = new XYSeries("y(t)");
        sampleFreq = 1600;
        double f1 = 30;
        double f2 = 70;

        XYSeries mprim_series = new XYSeries("m'z");

        double[] sig4 = new double[N];
        double[] sig5 = new double[N];
        for (int i = 0; i < N; i++) {
            double t = i / sampleFreq;
            sig4[i] = 0.5 * Math.sin(2 * Math.PI * f1 * t);
            sig5[i] = Math.sin(2 * Math.PI * f2 * t) + 0.7 * Math.sin(2 * Math.PI * f1 * t);
            xSeries.add(t, sig4[i]);
            ySeries.add(t, sig5[i]);
        }
        PlotSignal(xSeries, "Sygnał x(t)", "Czas [s]", "Amplituda");
        PlotSignal(ySeries, "Sygnał y(t)", "Czas [s]", "Amplituda");
        double a = 2.5;
        double b = 3.0;
        XYSeries zSeries = new XYSeries("z(t) = αx(t) + βy(t)");
        double[] sig6 = new double[N];
        for (int i = 0; i < N; i++) {
            sig6[i] = a * sig4[i] + b * sig5[i];
            zSeries.add(i / sampleFreq, sig6[i]);
        }
        PlotSignal(zSeries, "Sygnał z(t)", "Czas [s]", "Amplituda");

        double[] real4 = Arrays.copyOf(sig4, N);
        double[] imag4 = new double[N];
        double[] real5 = Arrays.copyOf(sig5, N);
        double[] imag5 = new double[N];
        double[] real6 = Arrays.copyOf(sig6, N);
        double[] imag6 = new double[N];

        fastFourierTransform(real4, imag4, N);
        fastFourierTransform(real5, imag5, N);
        fastFourierTransform(real6, imag6, N);

        XYSeries m_xSeries = new XYSeries("x(t) spectrum");
        XYSeries m_ySeries = new XYSeries("y(t) spectrum");
        XYSeries m_zSeries = new XYSeries("x(t) spectrum");

        for (int j = 0; j < (N / 2); j++) {
            double fk = j * (sampleFreq / N);
            double msig4 = Math.sqrt(real4[j] * real4[j] + imag4[j] * imag4[j]);
            double msig5 = Math.sqrt(real5[j] * real5[j] + imag5[j] * imag5[j]);
            double msig6 = Math.sqrt(real6[j] * real6[j] + imag6[j] * imag6[j]);
            double msig7 = a * msig4 + b * msig5;
            mprim_series.add(fk,msig7);
            m_xSeries.add(fk, msig4);
            m_ySeries.add(fk, msig5);
            m_zSeries.add(fk, msig6);
        }
        PlotSignal(m_xSeries, "x(t) Spectrum", "Frequency [Hz]", "Amplitude");
        PlotSignal(m_ySeries, "y(t) Spectrum", "Frequency [Hz]", "Amplitude");
        PlotSignal(m_zSeries, "z(t) Spectrum", "Frequency [Hz]", "Amplitude");
        PlotSignal(mprim_series, "m'z Spectrum", "Frequency [Hz]", "Amplitude");

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
}