import java.awt.*;
import java.net.URL;

public class Buffon extends Frame {
    Toolkit tool;
    int ww, hh;
    public Image backg;
    public InputPanel inputPanel;
    public InfoPanel infoPanel;
    public GraphPanel graphPanel;
    Font f = new Font("TimesRoman", Font.BOLD, 14);
    java.awt.TextField[] textFields = new java.awt.TextField[6];
    java.awt.TextField[] inputFields = new java.awt.TextField[6];
    public static void main(String args[]) {new Buffon();}
    
    public Buffon() {
        tool = getToolkit();
        Dimension res = tool.getScreenSize();
        ww = res.width;
        hh = res.height;
        setResizable(false);
        setTitle("Conice");
        setIconImage(tool.getImage(getResource("images/ico.gif")));
        setBackground(new Color(67, 134, 187));
        setLayout(null);
        loadImage();
        int panelHeight = hh / 2;
        int topGap = 50;
        int leftMargin = 25;
        int panelWidth = 600;
        int firstPanelHeight = panelHeight - topGap - 30;
        int gapBetweenPanels = 25;
        inputPanel = new InputPanel(this);
        add(inputPanel);
        inputPanel.setBounds(leftMargin, topGap, panelWidth, firstPanelHeight);
        infoPanel = new InfoPanel(this);
        add(infoPanel);
        infoPanel.setBounds(leftMargin, topGap + firstPanelHeight + gapBetweenPanels, panelWidth, firstPanelHeight);
        int graphPanelX = leftMargin + panelWidth + 25;
        int graphPanelY = topGap;
        int graphPanelWidth = ww - graphPanelX - leftMargin;
        int graphPanelHeight = 2 * firstPanelHeight + gapBetweenPanels;
        graphPanel = new GraphPanel(this);
        add(graphPanel);
        graphPanel.setBounds(graphPanelX, graphPanelY, graphPanelWidth, graphPanelHeight);
        resize(ww, hh);
        move(0, 0);
        setVisible(true);
    }
    
    public URL getResource(String s) {
        return this.getClass().getResource(s);
    }
    
    public void loadImage() {
        try {
            MediaTracker mediatracker = new MediaTracker(this); 
            backg = tool.getImage(getResource("images/backg.jpg")); 
            mediatracker.addImage(backg, 0); 
            mediatracker.waitForAll();
        } catch(Throwable throwable) {}
    }
    
    public void paint(Graphics g) {
        for(int i = 0; i <= (int)(ww/200); i++)
            for(int j = 0; j <= (int)(hh/200); j++)
                g.drawImage(backg, i*200, j*200, this);
    }
    
    public boolean handleEvent(Event e) {
        if(e.id == Event.WINDOW_DESTROY) { 
            System.exit(0);
        }
        return super.handleEvent(e);
    }
    
    public void handleDrawButton() {
        try {
            double[] coeffs = new double[6];
            for (int i = 0; i < inputPanel.textFields.length; i++) {
                String value = inputPanel.textFields[i].getText().trim();
                coeffs[i] = Double.parseDouble(value);
            }
            infoPanel.setConicCoefficients(coeffs);
            graphPanel.setConicCoefficients(coeffs);
        } catch (NumberFormatException e) {
            System.out.println("Invalid!");
        }
    }
}

//=====================================//

class InputPanel extends Panels {
    Buffon buffon;
    java.awt.TextField[] textFields;
    Button drawButton;
    String[] labels = {"a11", "a12", "a22", "a13", "a23", "a33"};
    public InputPanel(Buffon buffon) {
        super(buffon.backg);
        this.buffon = buffon;
        this.textFields = buffon.textFields;
        setLayout(null);
        int startY = 40;
        int verticalSpacing = 30;
        for (int i = 0; i < 6; i++) {
            textFields[i] = new java.awt.TextField();
            textFields[i].setFont(buffon.f);
            textFields[i].setForeground(Color.blue);
            textFields[i].setBackground(new Color(255, 255, 255, 200));
            add(textFields[i]);
            textFields[i].setBounds(300, startY + i * verticalSpacing, 250, 25);
        }
        drawButton = new Button("Deseneaza");
        drawButton.setFont(buffon.f);
        add(drawButton);
        drawButton.setBounds(250, startY + 6 * verticalSpacing + 20, 100, 30);
        drawButton.addActionListener(e -> buffon.handleDrawButton());
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        g.setFont(buffon.f);
        g.setColor(Color.white);
        int startY = 40;
        int verticalSpacing = 30;
        for (int i = 0; i < 6; i++) {
            g.drawString(labels[i] + ":", 50, startY + i * verticalSpacing + 18);
        }
    }
}

//=====================================//

class InfoPanel extends Panels {
    Buffon buffon;
    private String actualEquation = "";
    private String determinantA = "";
    private String determinantB = "";
    private String determinantC = "";
    private String invariantI = "";
    private String invariantDelta = "";
    private String invariantITimesDelta = "";
    private String invariantDelta1 = "";
    private String genul = "";
    private String tipul = "";
    private String denumire = "";
    public InfoPanel(Buffon buffon) {
        super(buffon.backg);
        this.buffon = buffon;
    }
    public void setConicCoefficients(double[] coeffs) {
        double a11 = coeffs[0], a12 = coeffs[1], a22 = coeffs[2];
        double a13 = coeffs[3], a23 = coeffs[4], a33 = coeffs[5];
        calculateProperties(a11, a12, a22, a13, a23, a33);
        repaint();
    }
    private void calculateProperties(double a11, double a12, double a22, double a13, double a23, double a33) {
        try {
            double A_value = a11*a22 - a12*a12;
            determinantA = String.format("A = %.4f", A_value);
            double B_value = a11*a33 - a13*a13;
            determinantB = String.format("B = %.4f", B_value);
            double C_value = a22*a33 - a23*a23;
            determinantC = String.format("C = %.4f", C_value);
            double I_value = a11 + a22;
            invariantI = String.format("I = %.4f", I_value);
            double delta_value = a11*a22*a33 + 2*a12*a23*a13 - a13*a22*a13 - a23*a23*a11 - a33*a12*a12;
            invariantDelta = String.format("Δ = %.4f", delta_value);
            double I_times_Delta = I_value * delta_value;
            invariantITimesDelta = String.format("I × Δ = %.4f", I_times_Delta);
            double delta1_value = A_value + B_value + C_value;
            invariantDelta1 = String.format("Δ1 = %.4f", delta1_value);
            actualEquation = buildEquationString(a11, a12, a22, a13, a23, a33);
            determineConicClassification(I_value, delta_value, I_times_Delta, delta1_value);
        } catch (Exception e) {
            genul = "Eroare";
            tipul = "Eroare";
            denumire = "Eroare";
        }
    }
    private String buildEquationString(double a11, double a12, double a22, double a13, double a23, double a33) {
        StringBuilder sb = new StringBuilder();
        if (Math.abs(a11) > 1e-10) {
            sb.append(formatTerm(a11, "x²"));
        }
        if (Math.abs(a12) > 1e-10) {
            if (sb.length() > 0 && 2*a12 > 0) sb.append(" + ");
            if (2*a12 < 0) sb.append(" - ");
            sb.append(formatTerm(Math.abs(2*a12), "xy"));
        }
        if (Math.abs(a22) > 1e-10) {
            if (sb.length() > 0 && a22 > 0) sb.append(" + ");
            if (a22 < 0) sb.append(" - ");
            sb.append(formatTerm(Math.abs(a22), "y²"));
        }
        if (Math.abs(a13) > 1e-10) {
            if (sb.length() > 0 && 2*a13 > 0) sb.append(" + ");
            if (2*a13 < 0) sb.append(" - ");
            sb.append(formatTerm(Math.abs(2*a13), "x"));
        }
        if (Math.abs(a23) > 1e-10) {
            if (sb.length() > 0 && 2*a23 > 0) sb.append(" + ");
            if (2*a23 < 0) sb.append(" - ");
            sb.append(formatTerm(Math.abs(2*a23), "y"));
        }
        if (Math.abs(a33) > 1e-10) {
            if (sb.length() > 0 && a33 > 0) sb.append(" + ");
            if (a33 < 0) sb.append(" - ");
            sb.append(String.format("%.2f", Math.abs(a33)));
        }
        if (sb.length() == 0) {
            return "0 = 0";
        } else {
            return sb.toString() + " = 0";
        }
    }

    private String formatTerm(double coeff, String variable) {
        if (Math.abs(coeff - 1.0) < 1e-10) {
            return variable;
        } else {
            return String.format("%.2f%s", coeff, variable);
        }
    }
    
    private void determineConicClassification(double I, double delta, double iTimesDelta, double delta1) {
        double eps = 1e-10;
        if (I > eps) {
            genul = "Eliptic";
            if (Math.abs(delta) > eps) {
                tipul = "Nedegenerata";
                if (iTimesDelta < 0) {
                    denumire = "Elipsa";
                } else {
                    denumire = "Elipsa imaginara";
                }
            } else {
                tipul = "Degenerata";
                denumire = "Punct dublu";
            }
        } else if (I < -eps) {
            genul = "Hiperbolic";
            if (Math.abs(delta) > eps) {
                tipul = "Nedegenerata";
                denumire = "Hiperbola";
            } else {
                tipul = "Degenerata";
                denumire = "Drepte concurente";
            }
        } else {
            genul = "Parabolic";
            if (Math.abs(delta) > eps) {
                tipul = "Nedegenerata";
                denumire = "Parabola";
            } else {
                tipul = "Degenerata";
                if (delta1 < -eps) {
                    denumire = "Drepte paralele";
                } else if (Math.abs(delta1) < eps) {
                    denumire = "Dreapta dubla";
                } else {
                    denumire = "Drepte imaginare paralele";
                }
            }
        }
    }
    public void paint(Graphics g) {
        super.paint(g);
        g.setFont(buffon.f);
        int startX = 20;
        int startY = 40;
        int lineSpacing = 25;
        int currentLine = 0;
        g.setColor(Color.white);
        g.drawString("Proprietatile conicei", startX, startY + currentLine * lineSpacing);
        currentLine += 2;
        g.setColor(Color.yellow);
        g.drawString("a11x² + 2a12xy + a22y² + 2a13x + 2a23y + a33 = 0", startX, startY + currentLine * lineSpacing);
        currentLine++;
        g.setColor(Color.red);
        g.drawString(actualEquation.isEmpty() ? " " : actualEquation, startX, startY + currentLine * lineSpacing);
        currentLine += 2;
        g.setColor(Color.white);
        g.drawString("Determinanti:", startX, startY + currentLine * lineSpacing);
        currentLine++;
        g.setColor(Color.yellow);
        g.drawString(determinantA.isEmpty() ? "A = " : determinantA, startX + 20, startY + currentLine * lineSpacing);
        g.drawString(determinantB.isEmpty() ? "B = " : determinantB, startX + 200, startY + currentLine * lineSpacing);
        currentLine++;
        g.drawString(determinantC.isEmpty() ? "C = " : determinantC, startX + 20, startY + currentLine * lineSpacing);
        currentLine += 2;
        g.setColor(Color.white);
        g.drawString("Invarianti:", startX, startY + currentLine * lineSpacing);
        currentLine++;
        g.setColor(Color.yellow);
        g.drawString(invariantI.isEmpty() ? "I = " : invariantI, startX + 20, startY + currentLine * lineSpacing);
        g.drawString(invariantDelta.isEmpty() ? "Δ = " : invariantDelta, startX + 200, startY + currentLine * lineSpacing);
        currentLine++;
        g.drawString(invariantITimesDelta.isEmpty() ? "I × Δ = " : invariantITimesDelta, startX + 20, startY + currentLine * lineSpacing);
        g.drawString(invariantDelta1.isEmpty() ? "Δ1 = " : invariantDelta1, startX + 200, startY + currentLine * lineSpacing);
        currentLine += 2;
        g.setColor(Color.white);
        g.drawString("Clasificare:", startX, startY + currentLine * lineSpacing);
        currentLine++;
        g.setColor(Color.green);
        g.drawString("Genul: " + (genul.isEmpty() ? "" : genul), startX + 20, startY + currentLine * lineSpacing);
        currentLine++;
        g.drawString("Tipul: " + (tipul.isEmpty() ? "" : tipul), startX + 20, startY + currentLine * lineSpacing);
        currentLine++;
        g.drawString("Denumire: " + (denumire.isEmpty() ? "" : denumire), startX + 20, startY + currentLine * lineSpacing);
    }
}

//=====================================//

class GraphPanel extends Panel {
    Buffon buffon;
    private double a11, a12, a22, a13, a23, a33;
    private int centerX, centerY;
    private double scale = 60.0;
    public GraphPanel(Buffon buffon) {
        this.buffon = buffon;
        setBackground(Color.white);
    }
    public void setConicCoefficients(double[] coeffs) {
        this.a11 = coeffs[0];
        this.a12 = coeffs[1];
        this.a22 = coeffs[2];
        this.a13 = coeffs[3];
        this.a23 = coeffs[4];
        this.a33 = coeffs[5];
        repaint();
    }
    public void update(Graphics g) {
        paint(g);
    }
    public void paint(Graphics g) {
        Dimension size = getSize();
        int width = size.width;
        int height = size.height;
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        centerX = width / 2;
        centerY = height / 2;
        drawCoordinateSystem(g, width, height);
        if (hasValidCoefficients()) {
            drawConic(g, width, height);
            drawCenter(g, width, height);
            drawSymmetryAxes(g, width, height);
        }
    }
    
    private boolean hasValidCoefficients() {
        return !(Math.abs(a11) < 1e-10 && Math.abs(a12) < 1e-10 && Math.abs(a22) < 1e-10 && Math.abs(a13) < 1e-10 && Math.abs(a23) < 1e-10 && Math.abs(a33) < 1e-10);
    }
    
    private void drawCoordinateSystem(Graphics g, int width, int height) {
        g.setColor(Color.black);
        g.drawLine(0, centerY, width, centerY);
        g.drawLine(centerX, 0, centerX, height);
        int tickLength = 5;
        for (int i = -10; i <= 10; i++) {
            if (i != 0) {
                int x = centerX + i * 40;
                g.drawLine(x, centerY - tickLength, x, centerY + tickLength);
            }
        }
        for (int i = -10; i <= 10; i++) {
            if (i != 0) {
                int y = centerY + i * 40;
                g.drawLine(centerX - tickLength, y, centerX + tickLength, y);
            }
        }
        g.drawString("X", width - 15, centerY - 10);
        g.drawString("Y", centerX + 10, 15);
    }
    
private void drawConic(Graphics g, int width, int height) {
    g.setColor(Color.red);
    double eps = 0.15;
    for (int px = 0; px < width; px++) {
        for (int py = 0; py < height; py++) {
            double x = (px - centerX) / scale;
            double y = (centerY - py) / scale;
            double F =a11*x*x +2*a12*x*y +a22*y*y +2*a13*x +2*a23*y +a33;
            if (Math.abs(F) < eps) {
                g.fillOval(px - 1, py - 1, 3, 3);
            }
        }
    }
}
    private void drawPoint(Graphics g, double x, double y) {
        int px = centerX + (int)(x * scale);
        int py = centerY - (int)(y * scale);
        if (px >= 0 && px < getSize().width && py >= 0 && py < getSize().height) {
            g.fillRect(px - 1, py - 1, 3, 3);
        }
    }
    
    private void drawCenter(Graphics g, int width, int height) {
        double A = a11*a22 - a12*a12;
        if (Math.abs(A) > 1e-10) {
            double centerXMath = (a12*a23 - a22*a13) / A;
            double centerYMath = (a12*a13 - a11*a23) / A;
            int centerXPixel = centerX + (int)(centerXMath * scale);
            int centerYPixel = centerY - (int)(centerYMath * scale);
            g.setColor(Color.blue);
            g.fillOval(centerXPixel - 4, centerYPixel - 4, 8, 8);
        }
    }
    
    private void drawSymmetryAxes(Graphics g, int width, int height) {
        double A = a11*a22 - a12*a12;
        if (Math.abs(A) > 1e-10) {
            double centerXMath = (a12*a23 - a22*a13) / A;
            double centerYMath = (a12*a13 - a11*a23) / A;
            int centerXPixel = centerX + (int)(centerXMath * scale);
            int centerYPixel = centerY - (int)(centerYMath * scale);
            g.setColor(Color.blue);
            if (Math.abs(a12) < 1e-10) {
                g.drawLine(0, centerYPixel, width, centerYPixel);
                g.drawLine(centerXPixel, 0, centerXPixel, height);
            } else {
                double angle = 0.5 * Math.atan(2*a12/(a11 - a22));
                drawAxis(g, centerXPixel, centerYPixel, angle, width, height);
                drawAxis(g, centerXPixel, centerYPixel, angle + Math.PI/2, width, height);
            }
        }
    }
    
    private void drawAxis(Graphics g, int cx, int cy, double angle, int width, int height) {
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        double t1 = -cx / dx;
        double t2 = (width - cx) / dx;
        double t3 = -cy / dy;
        double t4 = (height - cy) / dy;
        double minT = Double.MAX_VALUE;
        double maxT = -Double.MAX_VALUE;
        double[] ts = {t1, t2, t3, t4};
        for (double t : ts) {
            int x = cx + (int)(t * dx);
            int y = cy + (int)(t * dy);
            if (x >= 0 && x <= width && y >= 0 && y <= height) {
                minT = Math.min(minT, t);
                maxT = Math.max(maxT, t);
            }
        }
        if (minT != Double.MAX_VALUE && maxT != -Double.MAX_VALUE) {
            int x1 = cx + (int)(minT * dx);
            int y1 = cy + (int)(minT * dy);
            int x2 = cx + (int)(maxT * dx);
            int y2 = cy + (int)(maxT * dy);
            g.drawLine(x1, y1, x2, y2);
        }
    }
}

//=====================================//

class Panels extends Panel {
    public Image im, iml;
    public Panels(Image im) {
        this.im = im;
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        Dimension dimension = size();
        iml = createImage(dimension.width, dimension.height); 
        pan(iml.getGraphics());
        g.drawImage(iml, 0, 0, this);
    }
    
    public void pan(Graphics g) {
        Dimension dimension = size();
        int w = dimension.width;
        int h = dimension.height; 
        Color color = getBackground(); 
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        for(int k = 0; k < w; k += im.getWidth(this))
            for(int l = 0; l < h; l += im.getHeight(this))
                g.drawImage(im, k, l, this);
        g.setColor(color.brighter());
        g.drawRect(1, 1, w - 2, h - 2);
        g.setColor(color.darker());
        g.drawRect(0, 0, w - 2, h - 2);
    }
}