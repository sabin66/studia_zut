import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class BerPlots {

    /**
     * Rysuje heatmapę BER(α,β).
     *
     * @param alphas   tablica wartości α (oś X)
     * @param betas    tablica wartości β (oś Y)
     * @param bersList lista wartości BER (%) — ten sam rozmiar co alphas/betas
     * @param title    tytuł okna i wykresu
     */
    public static void showHeatmap(double[] alphas, double[] betas, List<Double> bersList, String title) {
        int n = alphas.length;
        if (betas.length != n || bersList.size() != n) {
            throw new IllegalArgumentException("alphas, betas i bersList muszą mieć ten sam rozmiar");
        }

        // 1) liczymy min/max BER
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (double z : bersList) {
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        // 2) jeżeli min==max, dodajemy margines
        if (Math.abs(maxZ - minZ) < 1e-9) {
            double eps = (minZ == 0.0 ? 1.0 : Math.abs(minZ)*0.1);
            minZ -= eps;
            maxZ += eps;
        }

        // 3) konwersja List<Double> -> double[]
        double[] bers = new double[n];
        for (int i = 0; i < n; i++) {
            bers[i] = bersList.get(i);
        }

        // 4) dataset
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("BER", new double[][] { alphas, betas, bers });

        // 5) skala kolorów
        PaintScale scale = new GrayPaintScale(minZ, maxZ);

        // 6) renderer blokowy
        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(scale);

        // 7) wyliczenie kroku po X i Y po unikalnych wartościach
        SortedSet<Double> ux = new TreeSet<>(), uy = new TreeSet<>();
        for (double a : alphas) ux.add(a);
        for (double b : betas ) uy.add(b);
        double xStep = 1.0, yStep = 1.0;
        if (ux.size() > 1) {
            Iterator<Double> it = ux.iterator();
            double a1 = it.next(), a2 = it.next();
            xStep = a2 - a1;
        }
        if (uy.size() > 1) {
            Iterator<Double> it = uy.iterator();
            double b1 = it.next(), b2 = it.next();
            yStep = b2 - b1;
        }
        renderer.setBlockWidth(xStep);
        renderer.setBlockHeight(yStep);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);


        // 8) osie i plot
        NumberAxis xAxis = new NumberAxis("Alpha");
        NumberAxis yAxis = new NumberAxis("Beta");
        xAxis.setAutoRange(false);
        xAxis.setRange(0.0, 3.0);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);

        yAxis.setAutoRange(false);
        yAxis.setRange(0.0, 3.0);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

        // 9) legenda
        PaintScaleLegend legend = new PaintScaleLegend(scale, new NumberAxis("BER (%)"));
        legend.setPosition(RectangleEdge.RIGHT);

        // 10) tworzymy i pokazujemy wykres
        JFreeChart chart = new JFreeChart(title + " — Heatmapa",
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                false);
        chart.addSubtitle(legend);

        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }


}
