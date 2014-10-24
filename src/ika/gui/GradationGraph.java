package ika.gui;

import ika.utils.CatmullRomSpline;
import ika.utils.GraphicsUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import javax.swing.JComponent;

public class GradationGraph extends JComponent implements MouseListener, MouseMotionListener, KeyListener {

    public static final String CURVE_CHANGED = "curve changed";
    public static final String SELECTION_CHANGED = "selection changed";
    
    private static final Color EXTRA_LIGHT_GRAY = new Color(220, 220, 220);
    private static final int GRADIENT_BAR_WIDTH = 5;
    private static final int HANLDE_SIZE = 5;
    private CatmullRomSpline[] curves;
    private CatmullRomSpline curve;
    private int whichCurve = 0;
    private int selected = -1;
    private boolean showFirstCurveOnly = true;
    private boolean showGradientBars = true;
    private boolean showDiagonal = true;
    private int[] histogram = null;
    private int histogramHighlight = -1;
    private String label;

    public GradationGraph() {
        addMouseListener(this);
        addKeyListener(this);
        setCurves(new CatmullRomSpline[]{new CatmullRomSpline(),
        new CatmullRomSpline(), new CatmullRomSpline()});
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(257 + GRADIENT_BAR_WIDTH, 257 + GRADIENT_BAR_WIDTH);
    }

    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(257 + GRADIENT_BAR_WIDTH, 257 + GRADIENT_BAR_WIDTH);
    }

    public void setCurves(CatmullRomSpline[] curves) {
        this.curves = curves;
        if (whichCurve > curves.length) {
            whichCurve = 0;
        }
        curve = curves[whichCurve];
        firePropertyChange(CURVE_CHANGED, null, null);
        repaint();
    }

    public void setCurve(CatmullRomSpline curve) {
        this.setCurves(new CatmullRomSpline[]{curve});
    }

    public CatmullRomSpline getCurve(int i) {
        return this.curves[i];
    }

    public void setWhichCurve(int which) {
        whichCurve = which;
        if (whichCurve == -1) {
            whichCurve = 0;
            showFirstCurveOnly = true;
        } else {
            showFirstCurveOnly = false;
        }
        if (curves != null) {
            curve = curves[whichCurve];
            repaint();
        }
    }

    protected void paintLabel(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.addRenderingHints(GraphicsUtils.antialiasedTextHints);
        Dimension size = this.getSize();
        ika.utils.CenteredStringRenderer.drawCentered(g2d, this.label,
                size.width / 2, (int)(size.height * 0.4),
                ika.utils.CenteredStringRenderer.NOFLIP);
    }

    protected void paintHistogram(Graphics2D g) {
        if (histogram == null) {
            return;
        }
        Color histoColor = isEnabled() ? Color.lightGray : EXTRA_LIGHT_GRAY;
        Color highlightColor = isEnabled() ? Color.BLACK : Color.LIGHT_GRAY;
        g.setColor(histoColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int i = 0; i < histogram.length; i++) {
            if (i == histogramHighlight) {
                g.setColor(highlightColor);
                g.drawLine(i, 255 - Math.max(1, histogram[i]), i, 255);
                g.setColor(histoColor);
            } else {
                g.drawLine(i, 255 - histogram[i], i, 255);
            }
        }
    }
    
    protected void paintGrid(Graphics2D g) {
        g.setColor(isEnabled() ? Color.lightGray : EXTRA_LIGHT_GRAY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawLine(0, 64, 255, 64);
        g.drawLine(0, 128, 255, 128);
        g.drawLine(0, 191, 255, 191);
        g.drawLine(64, 0, 64, 255);
        g.drawLine(128, 0, 128, 255);
        g.drawLine(191, 0, 191, 255);
    }

    protected void paintHandles(Graphics2D g, CatmullRomSpline curve) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int i = 0; i < curve.x.length; i++) {
            int x = (int) (255 * curve.x[i]) + 1;
            int y = 255 - (int) (255 * curve.y[i]) + 1;
            //g.setColor(color);
            //g.fillRect(x - HANLDE_SIZE / 2, y - HANLDE_SIZE / 2, HANLDE_SIZE, HANLDE_SIZE);
            g.setColor(isEnabled() ? Color.BLACK : Color.LIGHT_GRAY);
            g.fillRect(x - HANLDE_SIZE / 2, y - HANLDE_SIZE / 2, HANLDE_SIZE, HANLDE_SIZE);
        }
    }

    protected void paintCurve(Graphics2D g, CatmullRomSpline curve, Color color) {
        GeneralPath p = new GeneralPath();
        int numKnots = curve.x.length;
        float[] nx = new float[numKnots + 2];
        float[] ny = new float[numKnots + 2];
        System.arraycopy(curve.x, 0, nx, 1, numKnots);
        System.arraycopy(curve.y, 0, ny, 1, numKnots);
        nx[0] = nx[1];
        ny[0] = ny[1];
        nx[numKnots + 1] = nx[numKnots];
        ny[numKnots + 1] = ny[numKnots];
        g.setColor(color);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int x = 1; x <= 255; x++) {
            int y = 256 - (int) (255 * curve.evaluateSimilarToPrevious(x / 255f));
            x = x < 0 ? 0 : x > 255 ? 255 : x;
            y = y < 0 ? 0 : y > 255 ? 255 : y;
            if (x == 1) {
                p.moveTo(x, y);
            } else {
                p.lineTo(x, y);
            }
        }
        g.draw(p);
    }

    private void paintGradientBars(Graphics2D g2d) {

        if (!showGradientBars) {
            return;
        }

        Color darkColor = isEnabled() ? Color.BLACK : Color.GRAY;
        
        // vertical bar
        GradientPaint vGrad = new GradientPaint(0, 1, Color.WHITE, 0, 255, darkColor);
        g2d.setPaint(vGrad);
        g2d.fillRect(0, 1, GRADIENT_BAR_WIDTH, 255);

        // horizontal bar
        int x1 = 1 + GRADIENT_BAR_WIDTH;
        int x2 = 1 + GRADIENT_BAR_WIDTH + 255;
        GradientPaint hGrad = new GradientPaint(x1, 0, darkColor, x2, 0, Color.WHITE);
        g2d.setPaint(hGrad);
        g2d.fillRect(x1, 256, 255, GRADIENT_BAR_WIDTH);

    }

    
    @Override
    public void paintComponent(Graphics g) {
        if (curves == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create(); //copy g. Recomended by Sun tutorial.
        try {

            paintGradientBars(g2d);

            g2d.translate(GRADIENT_BAR_WIDTH, 0);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // paint background
            g.setColor(Color.white);
            g.fillRect(1 + GRADIENT_BAR_WIDTH, 1, 255, 255);

            // draw oblique baseline
            if (showDiagonal) {
                g.setColor(isEnabled() ? Color.lightGray : EXTRA_LIGHT_GRAY);
                g.drawLine(1 + GRADIENT_BAR_WIDTH, 255, 255 + GRADIENT_BAR_WIDTH, 1);
            }
            
            paintGrid(g2d);

            // draw frame
            g.setColor(isEnabled() ? Color.black : Color.LIGHT_GRAY);
            g.drawRect(GRADIENT_BAR_WIDTH, 0, 255, 255);

            paintHistogram(g2d);

            paintHandles(g2d, curves[whichCurve]);
            Color grayCurveColor = isEnabled() ? Color.BLACK : EXTRA_LIGHT_GRAY;
            paintCurve(g2d, curves[0], showFirstCurveOnly ? grayCurveColor : Color.red);
            if (!showFirstCurveOnly && curves.length == 3) {
                paintCurve(g2d, curves[1], Color.green);
                paintCurve(g2d, curves[2], Color.blue);
            }

            paintLabel(g2d);

        } finally {
            g2d.dispose(); //release the copy's resources. Recomended by Sun tutorial.
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isEnabled()) {
            return;
        }
        requestFocus();
        int x = e.getX() - 1 - GRADIENT_BAR_WIDTH;
        int y = e.getY() - 1;
        int newSelected = -1;
        for (int i = 0; i < curve.x.length; i++) {
            int kx = (int) (255 * curve.x[i]);
            int ky = 255 - (int) (255 * curve.y[i]) + 1;
            if (Math.abs(x - kx) < 5 && Math.abs(y - ky) < 5) {
                newSelected = i;
                addMouseMotionListener(this);
                break;
            }
        }
        if (newSelected != selected) {
            selected = newSelected;
            repaint();
        }
        if (newSelected == -1) {
            selected = curve.addKnot(x / 255.0f, 1 - y / 255.0f);
            addMouseMotionListener(this);
            firePropertyChange(SELECTION_CHANGED, null, null);
            repaint();
        }
    }

    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isEnabled()) {
            return;
        }
        addMouseMotionListener(this);
        if (selected != -1) {
            int x = e.getX() - 1 - GRADIENT_BAR_WIDTH;
            int y = e.getY() - 1;
            if (selected != 0 && selected != curve.x.length - 1 && (x < 0 || x >= getWidth() || y < 0 || y > getHeight())) {
                curve.removeKnot(selected);
                repaint();
            }
            firePropertyChange(CURVE_CHANGED, null, null);
            selected = -1;
        }
    }

    
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    
    @Override
    public void mouseExited(MouseEvent e) {
    }

    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isEnabled()) {
            return;
        }
        if (selected != -1) {
            int x = e.getX() - 1 - GRADIENT_BAR_WIDTH;
            int y = e.getY() - 1;
            x = (x < 0) ? 0 : (x > 255) ? 255 : x;
            y = (y < 0) ? 0 : (y > 255) ? 255 : y;
            float fx = x / 255.0f;
            float fy = 1 - y / 255.0f;
            if (selected > 0) {
                if (fx < curve.x[selected - 1]) {
                    fx = curve.x[selected - 1];
                }
            } else {
                fx = 0;
            }
            if (selected < curve.x.length - 1) {
                if (fx > curve.x[selected + 1]) {
                    fx = curve.x[selected + 1];
                }
            } else {
                fx = 1;
            }
            curve.x[selected] = fx;
            curve.y[selected] = fy;
            repaint();
        }
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        if (!isEnabled()) {
            return;
        }
        switch (e.getKeyChar()) {
            case '1':
            case '2':
            case '3':
                setWhichCurve(e.getKeyChar() - '1');
                break;
        }
    }

    
    @Override
    public void keyReleased(KeyEvent e) {
    }

    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    void setHistogram(int[] histogram) {
        this.histogram = histogram;
        this.repaint();
    }

    /**
     * @param histogramHighlight the histogramHighlight to set
     */
    public void setHistogramHighlight(int histogramHighlight) {
        this.histogramHighlight = histogramHighlight;
        this.repaint();
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the showGradientBars
     */
    public boolean isShowGradientBars() {
        return showGradientBars;
    }

    /**
     * @param showGradientBars the showGradientBars to set
     */
    public void setShowGradientBars(boolean showGradientBars) {
        this.showGradientBars = showGradientBars;
        this.repaint();
    }

    /**
     * @return the showDiagonal
     */
    public boolean isShowDiagonal() {
        return showDiagonal;
    }

    /**
     * @param showDiagonal the showDiagonal to set
     */
    public void setShowDiagonal(boolean showDiagonal) {
        this.showDiagonal = showDiagonal;
        this.repaint();
    }

}