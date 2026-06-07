package alg;

import svm.SVM;
import io.Vector;

public class WolfeDual extends Algorithm {

    public WolfeDual(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Wolfe Dual";
            svm.outd.algorithm = name;
            svm.outd.showInputData();
            svm.control.showInputData(
                svm.ind.input_file != null ? svm.ind.input_file : "",
                String.valueOf(N),
                String.valueOf(dim),
                name,
                String.valueOf(P)
            );
        }
    }

    public void run() {
        float eta = svm.settings.learning_rate;
        if (eta <= 0) eta = 0.1f;

        System.out.println("WOLFE DUAL: learning rate = " + eta);
        
        t = System.currentTimeMillis();

        svm.control.clearOutput();
        svm.control.appendOutput("Wolfe Dual\n\n");
        svm.control.appendOutput(String.format("Learning rate eta = %.4f\n\n", eta));

        float[] y = new float[N];
        for (int i = 0; i < N; i++)
            y[i] = (svm.ind.V[i].cl.Y == 1) ? 1.0f : -1.0f;

        // Precompute Gram matrix (NO scaling - use raw dot products)
        float[][] K = new float[N][N];
        for (int i = 0; i < N; i++)
            for (int j = i; j < N; j++) {
                float dot = 0;
                for (int d = 0; d < dim; d++)
                    dot += svm.ind.V[i].X[d] * svm.ind.V[j].X[d];
                K[i][j] = dot;
                K[j][i] = dot;
            }

        float[] alpha = new float[N];
        float[] w = new float[dim + 1];

        int epoch = 0;
        boolean converged = false;
        int totalUpdates = 0;

        while (epoch < P && !converged && shouldContinue()) {
            long epochStart = System.currentTimeMillis();
            checkPause();
            if (!shouldContinue()) break;

            // --- Gradient of dual objective ---
            float[] grad = new float[N];
            for (int i = 0; i < N; i++) {
                grad[i] = 1.0f;
                for (int j = 0; j < N; j++)
                    grad[i] -= alpha[j] * y[i] * y[j] * K[i][j];
            }

            // --- Project gradient: enforce Σ α_i y_i = 0 ---
            float sumGY = 0;
            for (int i = 0; i < N; i++)
                sumGY += grad[i] * y[i];
            float correction = sumGY / N;

            float maxDelta = 0;
            for (int i = 0; i < N; i++) {
                float projGrad = grad[i] - correction * y[i];
                float oldAlpha = alpha[i];
                alpha[i] = Math.max(0, alpha[i] + eta * projGrad);
                float delta = Math.abs(alpha[i] - oldAlpha);
                if (delta > maxDelta) maxDelta = delta;
                if (delta > 1e-12f) totalUpdates++;
            }

            // --- Re-project to fix any drift: make sum(alpha*y) = 0 ---
            float posSum = 0, negSum = 0;
            for (int i = 0; i < N; i++) {
                if (y[i] > 0) posSum += alpha[i];
                else negSum += alpha[i];
            }
            float avg = (posSum + negSum) / 2.0f;
            if (posSum > 1e-10f) {
                float s = avg / posSum;
                for (int i = 0; i < N; i++)
                    if (y[i] > 0) alpha[i] *= s;
            }
            if (negSum > 1e-10f) {
                float s = avg / negSum;
                for (int i = 0; i < N; i++)
                    if (y[i] < 0) alpha[i] *= s;
            }

            // --- Recover w from alpha ---
            for (int d = 0; d < dim; d++) w[d] = 0;
            for (int i = 0; i < N; i++) {
                if (alpha[i] > 1e-7f) {
                    for (int d = 0; d < dim; d++)
                        w[d] += alpha[i] * y[i] * svm.ind.V[i].X[d];
                }
            }
            float bias = 0;
            int svCount = 0;
            for (int i = 0; i < N; i++) {
                if (alpha[i] > 1e-5f) {
                    float f = 0;
                    for (int d = 0; d < dim; d++)
                        f += w[d] * svm.ind.V[i].X[d];
                    bias += y[i] - f;
                    svCount++;
                }
            }
            w[dim] = (svCount > 0) ? bias / svCount : 0;

            epoch++;

            long epochTime = System.currentTimeMillis() - epochStart;
            int accuracy = getAccuracy(w);

            // Log like DualPerceptron: epoch | misclass | DualObj | SV | accuracy | time
            float dualObj = 0;
            for (int i = 0; i < N; i++) {
                dualObj += alpha[i];
                for (int j = 0; j < N; j++)
                    dualObj -= 0.5f * alpha[i] * alpha[j] * y[i] * y[j] * K[i][j];
            }

            String line = String.format("%d | Obj:%.2f | SV:%d | %d%% | %dms\n",
                epoch, dualObj, svCount, accuracy, epochTime);
            svm.control.appendOutput(line);

            svm.control.showOutputData(String.valueOf(epoch),
                (System.currentTimeMillis() - t) + " ms", w);

            if (dim == 2) {
                svm.design.setPointsOfLine(w);
                svm.design.show_margin = false;
                svm.design.repaint();
            }

            if (maxDelta < 1e-6f) converged = true;
        }

        // --- Final w ---
        for (int d = 0; d < dim; d++) w[d] = 0;
        for (int i = 0; i < N; i++) {
            if (alpha[i] > 1e-7f)
                for (int d = 0; d < dim; d++)
                    w[d] += alpha[i] * y[i] * svm.ind.V[i].X[d];
        }
        float bias = 0;
        int svCount = 0;
        for (int i = 0; i < N; i++) {
            if (alpha[i] > 1e-5f) {
                float f = 0;
                for (int d = 0; d < dim; d++)
                    f += w[d] * svm.ind.V[i].X[d];
                bias += y[i] - f;
                svCount++;
            }
        }
        w[dim] = (svCount > 0) ? bias / svCount : 0;

        long finalTime = System.currentTimeMillis() - t;
        int finalAccuracy = getAccuracy(w);

        // Final summary
        String result = String.format(
            "\n=== WOLFE DUAL ===\n" +
            "Learning Rate (eta): %.4f\n" +
            "Epochs: %d\n" +
            "Accuracy: %d%%\n" +
            "Support Vectors: %d/%d\n" +
            "Total Updates: %d\n" +
            "Time: %dms\n" +
            "Status: %s\n",
            eta, epoch, finalAccuracy, svCount, N,
            totalUpdates, finalTime,
            converged ? "CONVERGED" : "MAX EPOCHS"
        );
        svm.control.appendOutput(result);

        svm.outd.stages_count = epoch;
        svm.outd.max_stages_count = P;
        svm.outd.computing_time = finalTime;
        svm.outd.w = w;
        svm.outd.accuracy = finalAccuracy;
        svm.outd.showOutputData();

        svm.control.showOutputData(String.valueOf(epoch), finalTime + " ms", w);

        if (dim == 2) {
            svm.design.setPointsOfLine(w);
            // Compute margin lines
            float norm = 0;
            for (int d = 0; d < dim; d++) norm += w[d] * w[d];
            norm = (float) Math.sqrt(norm);
            if (norm > 1e-10f) {
                float minPos = Float.MAX_VALUE, maxNeg = -Float.MAX_VALUE;
                for (int i = 0; i < N; i++) {
                    float f = 0;
                    for (int d = 0; d < dim; d++) f += w[d] * svm.ind.V[i].X[d];
                    float dist = (f + w[dim]) / norm;
                    if (y[i] > 0 && dist < minPos) minPos = dist;
                    if (y[i] < 0 && dist > maxNeg) maxNeg = dist;
                }
                svm.design.setMarginLines(w, minPos, maxNeg);
            }
        }

        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
        svm.control.start.setLabel("Start");
        svm.control.init = true;
    }
}