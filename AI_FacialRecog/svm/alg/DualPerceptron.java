package alg;

import svm.SVM;

public class DualPerceptron extends Algorithm {

    private volatile boolean running   = false;
    private volatile boolean suspended = false;

    public DualPerceptron(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Dual Perceptron";
            svm.outd.algorithm = name;
            svm.outd.max_stages_count = P;
            svm.outd.showInputData();
        }
    }

    @Override
    public void suspend_() {
        suspended = true;
    }

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

    private int label(int Y) { return (Y == 0) ? -1 : 1; }

    private float[] buildW(float[] alpha, float b) {
        float[] w = new float[dim + 1];
        for (int i = 0; i < N; i++) {
            float coef = alpha[i] * label(svm.ind.V[i].cl.Y);
            for (int j = 0; j < dim; j++)
                w[j] += coef * svm.ind.V[i].X[j];
        }
        w[dim] = b;
        return w;
    }

    @Override
    public void run() {
        t       = System.currentTimeMillis();
        running = true;

        float[] alpha = new float[N];  
        float   b     = 0f;            

        long    stage     = 0;
        boolean converged = false;

        while (running && stage < P && !converged) {

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

            converged = true;
            stage++;

            for (int i = 0; i < N; i++) {
                float s = b;
                for (int k = 0; k < N; k++) {
                    if (alpha[k] == 0f) continue;
                    float dot = 0f;
                    for (int j = 0; j < dim; j++)
                        dot += svm.ind.V[k].X[j] * svm.ind.V[i].X[j];
                    s += alpha[k] * label(svm.ind.V[k].cl.Y) * dot;
                }

                int yTrue = label(svm.ind.V[i].cl.Y);
                if (yTrue * s <= 0) {
                    alpha[i] += 1f;
                    b        += eta * yTrue;
                    converged = false;
                }
            }

            float[] w = buildW(alpha, b);

            svm.outd.stages_count    = stage;
            svm.outd.computing_time  = System.currentTimeMillis() - t;
            svm.outd.w               = w;
            svm.outd.accuracy        = getAccuracy(w);
            svm.outd.showOutputData();

            svm.design.setPointsOfLine(w);
            svm.design.repaint();
        }

        float[] w = buildW(alpha, b);
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
}
