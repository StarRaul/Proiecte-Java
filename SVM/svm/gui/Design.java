package gui;

import java.awt.*;
import java.awt.event.*;
import svm.SVM;

public class Design extends Panel implements MouseListener, MouseMotionListener {
	SVM svm;
	Image im;
	Graphics img;
	int ww, hh;
	int Ox, Oy, cx, cy, ccx, ccy;
	boolean init = true;
	String coords = "";
	public boolean show_coords = false;
	public boolean show_line = false;
	public boolean show_margin = false;
	public boolean calculates = false;
	public int x1, y1, x2, y2;
	public int mx1, my1, mx2, my2;
	public int nx1, ny1, nx2, ny2;

	public Design(SVM svm) {
		this.svm = svm;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void initO() {
		Ox = ww / 2; Oy = hh / 2;
		cx = 0; cy = 0;
		repaint();
	}

	public void paint(Graphics g) { update(g); }

	public void update(Graphics g) {
		if (init) {
			ww = size().width;
			hh = size().height;
			im = createImage(ww, hh);
			img = im.getGraphics();
			initO();
			init = false;
		}

		img.setColor(svm.settings.background_color);
		img.fillRect(0, 0, ww, hh);

		if (svm.ind.V != null && svm.ind.V[0].getDimension() == 2) {
			drawAxis(img);

			if (show_line) {
				img.setColor(svm.settings.line_color);
				img.drawLine(Ox + x1, Oy - y1, Ox + x2, Oy - y2);
			}

			if (show_margin) {
				img.setColor(Color.red);
				img.drawLine(Ox + mx1, Oy - my1, Ox + mx2, Oy - my2);
				img.setColor(Color.blue);
				img.drawLine(Ox + nx1, Oy - ny1, Ox + nx2, Oy - ny2);
			}

			for (int i = 0; i < svm.ind.V.length; i++) {
				Point p = new Point(Ox + (int)(svm.ind.V[i].X[0] + 0.5), Oy - (int)(svm.ind.V[i].X[1] + 0.5));
				img.setColor(svm.ind.V[i].cl.color);
				int r = svm.settings.point_radius;
				img.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
				img.setColor(Color.black);
				img.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);
			}

			if (show_coords) {
				img.setColor(svm.settings.string_color);
				img.drawString(coords, ccx + 15, ccy + 30);
			}
		}

		g.drawImage(im, 0, 0, this);
	}

	public void drawAxis(Graphics g) {
		if (svm.settings.grid) {
			g.setColor(svm.settings.grid_color);
			for (int i = Ox + svm.settings.axis_min; i <= Ox + svm.settings.axis_max; i += svm.settings.grid_size)
				g.drawLine(i, Oy + svm.settings.axis_min, i, Oy + svm.settings.axis_max);
			for (int j = Oy + svm.settings.axis_min; j <= Oy + svm.settings.axis_max; j += svm.settings.grid_size)
				g.drawLine(Ox + svm.settings.axis_min, j, Ox + svm.settings.axis_max, j);
		}

		if (svm.settings.axis) {
			g.setColor(svm.settings.axis_color);
			g.drawLine(Ox + svm.settings.axis_min, Oy, Ox + svm.settings.axis_max, Oy);
			g.drawLine(Ox, Oy + svm.settings.axis_min, Ox, Oy + svm.settings.axis_max);

			if (svm.settings.gradations) {
				for (int i = Ox; i <= Ox + svm.settings.axis_max; i += svm.settings.axis_gradations) g.drawLine(i, Oy - 2, i, Oy + 2);
				for (int i = Ox; i >= Ox + svm.settings.axis_min; i -= svm.settings.axis_gradations) g.drawLine(i, Oy - 2, i, Oy + 2);
				for (int j = Oy; j <= Oy + svm.settings.axis_max; j += svm.settings.axis_gradations) g.drawLine(Ox - 2, j, Ox + 2, j);
				for (int j = Oy; j >= Oy + svm.settings.axis_min; j -= svm.settings.axis_gradations) g.drawLine(Ox - 2, j, Ox + 2, j);
			}
		}
	}

	public void setPointsOfLine(float[] w) {
		show_line = true;
		if (Math.abs(w[0]) < Math.abs(w[1])) {
			x1 = svm.settings.axis_min;
			y1 = (int)((-w[2] - w[0] * x1) / w[1] + 0.5);
			x2 = svm.settings.axis_max;
			y2 = (int)((-w[2] - w[0] * x2) / w[1] + 0.5);
		} else {
			y1 = svm.settings.axis_min;
			x1 = (int)((-w[2] - w[1] * y1) / w[0] + 0.5);
			y2 = svm.settings.axis_max;
			x2 = (int)((-w[2] - w[1] * y2) / w[0] + 0.5);
		}
		repaint();
	}

	public void setMarginLines(float[] w, float distPos, float distNeg) {
		show_margin = true;
		float norm = 0;
		for (int j = 0; j < w.length - 1; j++) norm += w[j] * w[j];
		norm = (float) Math.sqrt(norm);

		float b = w[w.length - 1];
		float bPos = b - distPos * norm;
		float bNeg = b - distNeg * norm;

		if (Math.abs(w[0]) < Math.abs(w[1])) {
			mx1 = svm.settings.axis_min;
			my1 = (int)((-bPos - w[0] * mx1) / w[1] + 0.5);
			mx2 = svm.settings.axis_max;
			my2 = (int)((-bPos - w[0] * mx2) / w[1] + 0.5);
			nx1 = svm.settings.axis_min;
			ny1 = (int)((-bNeg - w[0] * nx1) / w[1] + 0.5);
			nx2 = svm.settings.axis_max;
			ny2 = (int)((-bNeg - w[0] * nx2) / w[1] + 0.5);
		} else {
			my1 = svm.settings.axis_min;
			mx1 = (int)((-bPos - w[1] * my1) / w[0] + 0.5);
			my2 = svm.settings.axis_max;
			mx2 = (int)((-bPos - w[1] * my2) / w[0] + 0.5);
			ny1 = svm.settings.axis_min;
			nx1 = (int)((-bNeg - w[1] * ny1) / w[0] + 0.5);
			ny2 = svm.settings.axis_max;
			nx2 = (int)((-bNeg - w[1] * ny2) / w[0] + 0.5);
		}
		repaint();
	}

	public void mouseClicked(MouseEvent me) { initO(); }
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}

	public void mouseMoved(MouseEvent me) {
		ccx = me.getX(); ccy = me.getY();
		coords = "(" + (ccx - Ox) + "," + (Oy - ccy) + ")";
		if (ccx <= 2 || ccx >= ww - 5 || ccy <= 5 || ccy >= hh - 5) coords = "";
		repaint();
	}

	public void mousePressed(MouseEvent me) {
		cx = me.getX(); cy = me.getY();
		coords = "";
	}

	public void mouseDragged(MouseEvent me) {
		int x = me.getX(), y = me.getY();
		if (svm.ind.V != null && svm.ind.V[0].getDimension() == 2) {
			cx = x - cx; cy = y - cy;
			Ox += cx; Oy += cy;
			cx = x; cy = y;
			coords = "";
			repaint();
		}
	}

	public void mouseReleased(MouseEvent me) {}
}
