package alg;

import svm.SVM;
import io.Vector;

public class SMO extends Algorithm {

    public double gamma;
    public double coef0;
    public double C;
    public double tol;
    public long maxIter;

    private double[] alpha;
    private double b;
    private double[] errors;
    private int[] labels;
    private Vector[] trainVectors;

    private volatile boolean running = false;
    private volatile boolean suspended = false;

    public SMO(SVM svm) {
        super(svm);
        this.gamma   = 0.01;
        this.coef0   = -1.0;
        this.C       = 1.0;
        this.tol     = 0.001;
        this.maxIter = P;

        if (svm.ind.V != null) {
            name = "SMO Sigmoid";
            svm.outd.algorithm = name;
            svm.outd.max_stages_count = P;
            svm.outd.showInputData();
        }
    }

    public SMO(SVM svm, double C, double gamma, double coef0, double tol) {
        super(svm);
        this.C       = C;
        this.gamma   = gamma;
        this.coef0   = coef0;
        this.tol     = tol;
        this.maxIter = P;

        if (svm.ind.V != null) {
            name = "SMO Sigmoid";
            svm.outd.algorithm = name;
            svm.outd.max_stages_count = P;
            svm.outd.showInputData();
        }
    }

    private SMO(boolean standalone) {
        super(standalone);
    }

    public static SMO createStandalone(double C, double gamma, double coef0,
                                       double tol, long maxIter) {
        SMO smo      = new SMO(true);
        smo.C        = C;
        smo.gamma    = gamma;
        smo.coef0    = coef0;
        smo.tol      = tol;
        smo.maxIter  = maxIter;
        smo.N        = 0;
        smo.dim      = 0;
        smo.eta      = 0f;
        return smo;
    }

    @Override public void suspend_() { suspended = true; }

    @Override
    public void resume_() {
        suspended = false;
        synchronized (this) { notify(); }
    }

    @Override
    public void stop_() {
        running   = false;
        suspended = false;
        synchronized (this) { notify(); }
    }

    public double kernel(float[] x, float[] z) {
        double dot = 0.0;
        for (int j = 0; j < x.length; j++)
            dot += (double)x[j] * (double)z[j];
        return Math.tanh(gamma * dot + coef0);
    }

    private double decisionFunction(int i, Vector[] V) {
        double s = b;
        for (int k = 0; k < N; k++) {
            if (alpha[k] == 0.0) continue;
            s += alpha[k] * labels[k] * kernel(V[k].X, V[i].X);
        }
        return s;
    }

    private int step(int i1, int i2, Vector[] V) {
        if (i1 == i2) return 0;

        double alpha1 = alpha[i1];
        double alpha2 = alpha[i2];
        int    y1     = labels[i1];
        int    y2     = labels[i2];
        double E1     = errors[i1];
        double E2     = errors[i2];
        int    s      = y1 * y2;

        double L, H;
        if (s == -1) {
            L = Math.max(0.0, alpha2 - alpha1);
            H = Math.min(C,   C + alpha2 - alpha1);
        } else {
            L = Math.max(0.0, alpha1 + alpha2 - C);
            H = Math.min(C,   alpha1 + alpha2);
        }
        if (L >= H) return 0;

        double k11 = kernel(V[i1].X, V[i1].X);
        double k12 = kernel(V[i1].X, V[i2].X);
        double k22 = kernel(V[i2].X, V[i2].X);

        double eta = k11 + k22 - 2.0 * k12;

        double alpha2New;
        if (eta > 1e-12) {
            alpha2New = alpha2 + y2 * (E1 - E2) / eta;
            if      (alpha2New < L) alpha2New = L;
            else if (alpha2New > H) alpha2New = H;
        } else {
            double a1L = alpha1 + s * (alpha2 - L);
            double a1H = alpha1 + s * (alpha2 - H);
            double objL = a1L + L
                        - 0.5*k11*a1L*a1L - 0.5*k22*L*L - s*k12*a1L*L
                        - y1*a1L*E1 - y2*L*E2;
            double objH = a1H + H
                        - 0.5*k11*a1H*a1H - 0.5*k22*H*H - s*k12*a1H*H
                        - y1*a1H*E1 - y2*H*E2;
            if      (objL > objH + tol) alpha2New = L;
            else if (objH > objL + tol) alpha2New = H;
            else                        alpha2New = alpha2;
        }

        if (Math.abs(alpha2New - alpha2) < tol * (alpha2New + alpha2 + tol))
            return 0;

        double alpha1New = alpha1 + s * (alpha2 - alpha2New);

        double b1 = b - E1
                    - y1 * (alpha1New - alpha1) * k11
                    - y2 * (alpha2New - alpha2) * k12;
        double b2 = b - E2
                    - y1 * (alpha1New - alpha1) * k12
                    - y2 * (alpha2New - alpha2) * k22;

        if      (alpha1New > 0 && alpha1New < C) b = b1;
        else if (alpha2New > 0 && alpha2New < C) b = b2;
        else                                     b = (b1 + b2) / 2.0;

        alpha[i1] = alpha1New;
        alpha[i2] = alpha2New;

        errors[i1] = decisionFunction(i1, V) - labels[i1];
        errors[i2] = decisionFunction(i2, V) - labels[i2];

        return 1;
    }

    private int examineExample(int i2, Vector[] V) {
        int    y2     = labels[i2];
        double alpha2 = alpha[i2];
        double E2     = errors[i2];
        double r2     = E2 * y2;

        if (!((r2 < -tol && alpha2 < C) || (r2 > tol && alpha2 > 0)))
            return 0;

        int    i1Best   = -1;
        double maxDiff  = 0.0;
        for (int k = 0; k < N; k++) {
            if (alpha[k] > 0 && alpha[k] < C) {
                double diff = Math.abs(errors[k] - E2);
                if (diff > maxDiff) { maxDiff = diff; i1Best = k; }
            }
        }
        if (i1Best >= 0 && step(i1Best, i2, V) == 1) return 1;

        int start = (int)(Math.random() * N);
        for (int k = 0; k < N; k++) {
            int i1 = (start + k) % N;
            if (alpha[i1] > 0 && alpha[i1] < C)
                if (step(i1, i2, V) == 1) return 1;
        }

        start = (int)(Math.random() * N);
        for (int k = 0; k < N; k++) {
            int i1 = (start + k) % N;
            if (step(i1, i2, V) == 1) return 1;
        }

        return 0;
    }

    public void train(Vector[] V) {
        this.N            = V.length;
        this.dim          = V[0].getDimension();
        this.trainVectors = V;

        alpha  = new double[N];
        b      = 0.0;
        labels = new int[N];
        errors = new double[N];

        for (int i = 0; i < N; i++)
            labels[i] = (V[i].cl.Y == 1) ? 1 : -1;

        for (int i = 0; i < N; i++)
            errors[i] = -labels[i];

        int     numChanged = 0;
        boolean examineAll = true;
        long    iter       = 0;

        while ((numChanged > 0 || examineAll) && iter < maxIter) {
            iter++;
            numChanged = 0;

            if (examineAll) {
                for (int i = 0; i < N; i++)
                    numChanged += examineExample(i, V);
            } else {
                for (int i = 0; i < N; i++)
                    if (alpha[i] > 0 && alpha[i] < C)
                        numChanged += examineExample(i, V);
            }

            if      (examineAll)      examineAll = false;
            else if (numChanged == 0) examineAll = true;
        }
    }

    @Override
    public void run() {
        t       = System.currentTimeMillis();
        running = true;

        Vector[] V        = svm.ind.V;
        this.N            = V.length;
        this.dim          = V[0].getDimension();
        this.trainVectors = V;

        alpha  = new double[N];
        b      = 0.0;
        labels = new int[N];
        errors = new double[N];

        for (int i = 0; i < N; i++) {
            labels[i] = (V[i].cl.Y == 1) ? 1 : -1;
            errors[i] = -labels[i];
        }

        int     numChanged = 0;
        boolean examineAll = true;
        long    stage      = 0;

        while (running && (numChanged > 0 || examineAll) && stage < P) {

            synchronized (this) {
                while (suspended) {
                    try { wait(); }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                }
            }

            stage++;
            numChanged = 0;

            if (examineAll) {
                for (int i = 0; i < N && running; i++)
                    numChanged += examineExample(i, V);
            } else {
                for (int i = 0; i < N && running; i++)
                    if (alpha[i] > 0 && alpha[i] < C)
                        numChanged += examineExample(i, V);
            }

            if      (examineAll)      examineAll = false;
            else if (numChanged == 0) examineAll = true;

            float[] w = buildW(V);
            svm.outd.stages_count   = stage;
            svm.outd.computing_time = System.currentTimeMillis() - t;
            svm.outd.w              = w;
            svm.outd.accuracy       = getAccuracy(w);
            svm.outd.showOutputData();
            svm.design.setPointsOfLine(w);
            svm.design.repaint();
        }

        float[] w = buildW(V);
        svm.outd.stages_count   = stage;
        svm.outd.computing_time = System.currentTimeMillis() - t;
        svm.outd.w              = w;
        svm.outd.accuracy       = getAccuracy(w);
        svm.outd.showInputData();
        svm.outd.showOutputData();
        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
    }

    private float[] buildW(Vector[] V) {
        float[] w = new float[dim + 1];
        for (int i = 0; i < N; i++) {
            double coef = alpha[i] * labels[i];
            for (int j = 0; j < dim; j++)
                w[j] += (float)(coef * V[i].X[j]);
        }
        w[dim] = (float) b;
        return w;
    }

    public int classify(float[] x) {
        return score(x) >= 0.0 ? 1 : -1;
    }

    public double score(float[] x) {
        if (trainVectors == null || alpha == null)
            throw new IllegalStateException("SMO: clasificatorul nu a fost antrenat!");
        double s = b;
        for (int i = 0; i < N; i++) {
            if (alpha[i] == 0.0) continue;
            s += alpha[i] * labels[i] * kernel(trainVectors[i].X, x);
        }
        return s;
    }

    public double[] getAlpha() { return alpha; }
    public double getB() { return b; }
    public int[] getLabels() { return labels; }
    public Vector[] getTrainVectors() { return trainVectors; }

    public void setState(double[] alpha, double b, int[] labels, Vector[] trainVectors) {
        this.alpha        = alpha;
        this.b            = b;
        this.labels       = labels;
        this.trainVectors = trainVectors;
        this.N            = alpha.length;
        this.dim          = (trainVectors != null && trainVectors.length > 0)
                            ? trainVectors[0].getDimension() : 0;
    }
}
