//Zadanie 1 :  wykres 1 || Zadanie 2 : wykresy 1 || Zadanie 3 : wykres 1
// Zadanie 4 : wykresy 6
// Dorian Sobieranski 55617
// Wszystkie funkcje PlotSignal itp. wygenerowane przez chatGPT
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

public class Main {
    public static void main(String[] args) {

        // Zadanie 1 i 2
        XYSeries func1 = new XYSeries("x(t)");
        XYSeries func2 = new XYSeries("y(t)");
        XYSeries func3 = new XYSeries("z(t)");
        XYSeries func4 = new XYSeries("v(t)");


        double freq = 12;
        double phase = Math.PI / 2;
        double sampleFreq = 22050;
        double samplePeriod = 2;
        double angleSpeed = 12.57; // rad/s
        int N = (int) Math.round(samplePeriod * sampleFreq);

        for (int i = 0; i < N - 1; i++) {
            double t = i / sampleFreq;
            double x = Math.cos(Math.PI * 2 * freq * t + phase) * Math.cos(2.5 * Math.pow(t, 0.2) * Math.PI);
            double y = (x * t) / (3 + Math.cos(Math.PI * 20 * t));
            double z = Math.pow(t, 2) * Math.abs(x * y - (2 / (10 + y)));
            double v = Math.pow(z, 3) + 3 * Math.sin(z * y * Math.abs(y - x));
            func1.add(t, x);
            func2.add(t, y);
            func3.add(t, z);
            func4.add(t, v);
        }
        PlotSignal(func1, "Signal x(t)");
        PlotSignal(func2, "Signal y(t)");
        PlotSignal(func3, "Signal z(t)");
        PlotSignal(func4, "Signal v(t)");



        // Zadanie 3

        XYSeries func5 = new XYSeries("u(t)");

        for(int j = 0; j < N-1;j++){
            double u;
            double t2 = j/sampleFreq;
            if(0 <= t2 && t2< 0.1){
                u = Math.sin(Math.PI*6 * t2) * Math.cos(Math.PI*5 * t2);
                func5.add(t2,u);
            }else if(0.1 <= t2 && t2 < 0.4){
                u = -1.1 * t2 * Math.cos(Math.PI * 41 * Math.pow(t2,2));
                func5.add(t2,u);
            }else if(0.4 <= t2 && t2 < 0.72){
                u = t2* Math.sin(20 * Math.pow(t2,4));
                func5.add(t2,u);
            }else if(0.72 <= t2 && t2 < 1) {
                u = 3.3 * (t2 - 0.72) * Math.cos(27 * t2 + 1.3);
                func5.add(t2,u);
            }
        }
        PlotSignal(func5, "Signal u(t)");




        // Zadanie 4
        XYSeriesCollection dataset = new XYSeriesCollection();
        int[] h = {14,18,22};
        XYSeries seriesH1 = new XYSeries("H1");
        XYSeries seriesH2 = new XYSeries("H2");
        XYSeries seriesH22 = new XYSeries("H22");

        for (int i = 0; i < N - 1; i++)
        {
            double t = i / sampleFreq;
            double bk1 = (Math.sin(h[0] * Math.PI * t)) / (2 + Math.cos(2*h[0] * Math.PI * t));
            double bk2 = (Math.sin(h[1] * Math.PI * t)) / (2 + Math.cos(2*h[1] * Math.PI * t));
            double bk3 = (Math.sin(h[2] * Math.PI * t)) / (2 + Math.cos(2*h[2] * Math.PI * t));
            seriesH1.add(t,bk1);
            seriesH2.add(t,bk2);
            seriesH22.add(t,bk3);

        }
        dataset.addSeries(seriesH1);
        dataset.addSeries(seriesH2);
        dataset.addSeries(seriesH22);
        PlotSignalSet(dataset,"Bk(t)");

    }



    public static void PlotSignal(XYSeries series,String title){
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,"time","Amplitude",
                dataset,PlotOrientation.VERTICAL,
                true,true,false
        );
        ChartFrame frame = new ChartFrame(title,chart);
        frame.pack();
        frame.setVisible(true);
    }
    public static void PlotSignalSet(XYSeriesCollection dataset, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "time", "Amplitude",
                dataset, PlotOrientation.VERTICAL,
                true, true, false
        );
        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);
    }

}