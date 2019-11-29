package quantum.qm1d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

import javax.swing.JPanel;

import quantum.math.MathUtil;
import quantum.qm2d.QuantumBox;
import quantum.qmshared.AbsorbingBoundary;
import quantum.qmshared.Boundary;
import quantum.qmutil.MiscUtil;

/**
 * @author Charles Xie
 */
class DynamicsView extends JPanel {

    private static final long serialVersionUID = 1L;
    private double position, velocity, kinE, potE;
    private ElectricField1D eField;
    private double[] pot, prob;
    private GeneralPath path;
    private static Stroke probabilityStroke = new BasicStroke(1);
    private static Stroke potentialStroke = new BasicStroke(5);
    private static Stroke velocityStroke = new BasicStroke(2);
    private static Stroke positionStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{2}, 0);
    private static Font font = new Font(null, Font.PLAIN, 11);
    private static Color waveColor = new Color(127, 127, 127, 127);
    private static Color eFieldColor = new Color(0, 0, 127, 127);
    private double vmin = -10, vmax = 10;
    private double upperBound, lowerBound;
    private int margin = 50;
    private float potentialScale = 0.5f;
    private float probabilityScale = 5f;
    private NumberFormat format;
    private double time, timeStep;
    private Boundary boundary;
    private int n;
    private boolean frank = true;
    private BufferedImage bimg;

    DynamicsView() {
        super();
        format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                processMousePressed(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                processMouseMoved(e);
            }
        });
    }

    void setFrank(boolean b) {
        frank = b;
    }

    void setPotential(Potential1D potential, boolean init) {
        this.pot = potential.pot;
        if (init) {
            n = pot.length;
            probabilityScale = 5 * n * 0.01f;
            upperBound = potential.getUpperBound();
            lowerBound = potential.getLowerBound();
            vmin = MathUtil.getMin(pot);
            vmax = MathUtil.getMax(pot);
            if (vmax > 10)
                vmax = 10;
            if (vmin < -10)
                vmin = -10;
        }
    }

    void setBoundaryLayer(Boundary bl) {
        boundary = bl;
    }

    void setElectricField(ElectricField1D eField, double time) {
        this.eField = eField;
        this.time = time;
    }

    void setTimeStep(double timeStep) {
        this.timeStep = timeStep;
    }

    void setProbability(double[] prob) {
        this.prob = prob;
    }

    void setPosition(double position) {
        this.position = position;
    }

    void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    void setKineticEnergy(double kinE) {
        this.kinE = kinE;
    }

    void setPotentialEnergy(double potE) {
        this.potE = potE;
    }

    private Graphics2D createGraphics2D() {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g;
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
            bimg = (BufferedImage) createImage(w, h);
        }
        g = bimg.createGraphics();
        g.setBackground(getBackground());
        g.clearRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    /* Need to use this old double-buffering technique in order to avoid flickering when run as an applet on the Mac */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = createGraphics2D();
        draw(g2);
        g2.dispose();
        if (bimg != null)
            g.drawImage(bimg, 0, 0, this);
    }

    private void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        drawAxes(g);
        drawPotentialFunction(g2);
        drawProbabilityFunction(g2);
        if (boundary == null)
            drawPositionAndVelocity(g2);
        if (frank) {
			g2.setFont(new Font("Arial", Font.BOLD, 12));
            int frankLength = g2.getFontMetrics().stringWidth(QuantumBox.BRAND_NAME);
            MiscUtil.drawFrank(g2, getWidth() - frankLength - 5, getHeight() - 10);
        }
    }

    private void drawProbabilityFunction(Graphics2D g2) {
        if (pot == null || prob == null)
            return;
        if (path == null)
            path = new GeneralPath();
        else
            path.reset();
        double dy = probabilityScale * (getHeight() - 2 * margin);
        double dx = (double) getWidth();
        int beg, end;
        if (boundary instanceof AbsorbingBoundary) {
            beg = (int) (((AbsorbingBoundary) boundary).getLengthPercentage() * n);
            end = prob.length - beg;
            if (beg == 0)
                end -= 1;
            dx /= (double) (prob.length - 2 * beg);
        } else {
            beg = 0;
            end = prob.length - 1;
            dx /= (double) prob.length;
        }
        path.moveTo(0, (float) (getHeight() - margin));
        for (int i = beg; i <= end; i++) {
            path.lineTo((float) ((i - beg) * dx), (float) (getHeight() - margin - prob[i] * dy));
        }
        path.lineTo(getWidth(), getHeight() - margin);
        g2.setColor(waveColor);
        path.closePath();
        g2.fill(path);
        g2.setColor(Color.gray);
        g2.setStroke(probabilityStroke);
        g2.draw(path);
    }

    private void drawPositionAndVelocity(Graphics2D g2) {
        if (prob == null)
            return;
        double dx = (double) getWidth() / (double) prob.length;
        int x0 = (int) (dx * position);
        g2.setColor(Color.red);
        g2.setStroke(positionStroke);
        g2.drawLine(x0, 0, x0, getHeight());
        g2.setColor(Color.green);
        g2.setStroke(velocityStroke);
        int x1 = (int) (dx * position + velocity * 20);
        int y0 = getHeight() / 2;
        int y1 = getHeight() / 2;
        g2.drawLine(x0, y0, x1, y1);
        if (Math.abs(velocity) > 0.0001) {
            int dv = (int) (5 * Math.signum(velocity));
            g2.drawLine(x1, y1, x1 - dv, y1 - dv);
            g2.drawLine(x1, y1, x1 - dv, y1 + dv);
        }
        g2.setColor(Color.black);
        g2.drawString("T. E.", 5, 20);
        g2.drawString("P. E.", 5, 35);
        g2.drawString("K. E.", 5, 50);
        int ke = (int) Math.round(Math.abs(kinE) * 100);
        int pe = (int) Math.round(Math.abs(potE) * 100);
        int te = (int) Math.round(Math.abs(kinE + potE) * 100);
        if (te > 0) {
            g2.setColor(Color.magenta);
            g2.fillRect(40, 10, te, 10);
        }
        if (pe > 0) {
            g2.setColor(Color.blue);
            g2.fillRect(40, 25, pe, 10);
        }
        if (ke > 0) {
            g2.setColor(Color.red);
            g2.fillRect(40, 40, ke, 10);
        }
        g2.setColor(Color.black);
        if (te > 0)
            g2.drawRect(40, 10, te, 10);
        if (pe > 0)
            g2.drawRect(40, 25, pe, 10);
        if (ke > 0)
            g2.drawRect(40, 40, ke, 10);
    }

    private void drawAxes(Graphics g) {
        g.setColor(Color.lightGray);
        int bl;
        if (boundary instanceof AbsorbingBoundary) {
            bl = (int) (((AbsorbingBoundary) boundary).getLengthPercentage() * n);
        } else {
            bl = 0;
        }
        double dx = getWidth() / ((upperBound - lowerBound) * (double) (n - 2 * bl) / (double) n);
        int cx = (int) (dx * (-lowerBound - (double) bl / (double) n * (upperBound - lowerBound)));
        g.drawLine(cx, 0, cx, getHeight());
        double dy = potentialScale * (getHeight() - 2 * margin) / (vmax - vmin);
        int nticks = 2;
        double v;
        for (int i = 0; i <= nticks; i++) {
            v = vmin + i * (vmax - vmin) / nticks;
            g.drawString(format.format(v), cx + 5, (int) (getHeight() - margin - (v - vmin) * dy));
        }
    }

    private void drawPotentialFunction(Graphics2D g2) {
        if (pot == null)
            return;
        if (path == null)
            path = new GeneralPath();
        else
            path.reset();
        double dy = potentialScale * (getHeight() - 2 * margin) / (vmax - vmin);
        double dx = (double) getWidth();
        int beg, end;
        if (boundary instanceof AbsorbingBoundary) {
            beg = (int) (((AbsorbingBoundary) boundary).getLengthPercentage() * n);
            end = pot.length - beg;
            if (beg == 0)
                end -= 1;
            dx /= (double) (pot.length - 2 * beg);
        } else {
            beg = 0;
            end = pot.length - 1;
            dx /= (double) pot.length;
        }
        path.moveTo(0, (float) (getHeight() - margin - (pot[beg] - vmin) * dy));
        float y = 0;
        for (int i = beg; i <= end; i++) {
            y = (float) (getHeight() - margin - (pot[i] - vmin) * dy);
            path.lineTo((float) ((i - beg) * dx), y);
        }
        g2.setColor(Color.gray);
        g2.setStroke(potentialStroke);
        g2.draw(path);
        double eFieldValue = eField == null ? 0 : eField.getValue(time);
        if (eFieldValue != 0) {
            int w = getWidth();
            g2.setStroke(probabilityStroke);
            g2.setColor(Color.black);
            g2.drawLine(w - 60, 40, w - 20, 40);
            if (eFieldValue < 0) {
                g2.drawLine(w - 20, 40, w - 25, 35);
                g2.drawLine(w - 20, 40, w - 25, 45);
            } else {
                g2.drawLine(w - 60, 40, w - 55, 35);
                g2.drawLine(w - 60, 40, w - 55, 45);
            }
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String s = "Electric Field";
            g2.drawString(s, getWidth() - fm.stringWidth(s) - 10, 20);
            float x0 = w - 40;
            float y0 = 50;
            dx = 40;
            dy = 60;
            g2.setColor(Color.cyan);
            g2.fillRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
            g2.setColor(Color.black);
            g2.drawRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
            g2.drawLine((int) x0, (int) y0, (int) x0, (int) (y0 + dy));
            float x = (float) (eFieldValue / eField.getIntensity() * 0.5 * dx);
            path.reset();
            path.moveTo(x0 - x, y0);
            g2.setColor(Color.red);
            g2.fillOval((int) (x0 - x - 2), (int) (y0 - 2), 4, 4);
            for (int i = 1; i < dy; i++) {
                x = (float) (eField.getValue(time - timeStep * i * RealTimePropagator1D.OUTPUT_INTERVAL) / eField.getIntensity() * 0.5 * dx);
                path.lineTo(x0 - x, y0 + i);
            }
            g2.setColor(eFieldColor);
            g2.draw(path);
        }
    }

    private void processMousePressed(MouseEvent e) {
    }

    private void processMouseMoved(MouseEvent e) {
    }

}