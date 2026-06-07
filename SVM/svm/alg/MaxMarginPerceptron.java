package alg;

import svm.SVM;

public class MaxMarginPerceptron extends Algorithm {

    public MaxMarginPerceptron(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Max Margin Perceptron";
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

        t = System.currentTimeMillis();

        svm.control.clearOutput();
        svm.control.appendOutput("Max Margin Perceptron\n\n");
        svm.control.appendOutput(String.format("Learning rate η = %.4f\n\n", eta));
        svm.control.appendOutput("Phase 1: Perceptron convergence\n\n");

        float[] w = new float[dim + 1];
        int epoch = 0;
        boolean converged = false;
        int misclassified;
        int totalUpdates = 0;

        while (epoch < P && !converged && shouldContinue()) {
            long epochStart = System.currentTimeMillis();
            misclassified = 0;

            for (int i = 0; i < N && shouldContinue(); i++) {
                checkPause();
                if (!shouldContinue()) break;

                float activation = 0;
                for (int j = 0; j < dim; j++) activation += w[j] * svm.ind.V[i].X[j];
                activation += w[dim];

                int predicted = (activation >= 0) ? 1 : 0;
                int actual = svm.ind.V[i].cl.Y;

                if (predicted != actual) {
                    misclassified++;
                    totalUpdates++;
                    int error = actual - predicted;
                    for (int j = 0; j < dim; j++) w[j] += eta * error * svm.ind.V[i].X[j];
                    w[dim] += eta * error;
                }
            }

            if (!shouldContinue()) break;

            if (misclassified == 0) converged = true;
            epoch++;

            long epochTime = System.currentTimeMillis() - epochStart;
            int accuracy = getAccuracy(w);

            svm.control.logPerceptronEpoch(epoch, misclassified, N, accuracy, (int) epochTime);
            svm.control.showOutputData(String.valueOf(epoch), (System.currentTimeMillis() - t) + " ms", w);

            if (dim == 2 && epoch % 10 == 0) {
                svm.design.setPointsOfLine(w);
                svm.design.repaint();
            }
        }

        if (!shouldContinue()) {
            finalize(w, epoch, false);
            return;
        }

        svm.control.appendOutput(String.format(
            "\nPhase 1 done: %d epochs, %s\n\n",
            epoch, converged ? "converged" : "max epochs reached"
        ));

        if (!converged)
            svm.control.appendOutput("Warning: data may not be linearly separable.\nMargin maximization applied on best found hyperplane.\n\n");

        svm.control.appendOutput("Phase 2: Margin maximization\n\n");

        float norm = 0;
        for (int j = 0; j < dim; j++) norm += w[j] * w[j];
        norm = (float) Math.sqrt(norm);

        if (norm < 1e-10f) {
            svm.control.appendOutput("Degenerate hyperplane (w=0), cannot maximize margin.\n");
            finalize(w, epoch, false);
            return;
        }

        float minPos = Float.MAX_VALUE;
        float maxNeg = -Float.MAX_VALUE;

        for (int i = 0; i < N; i++) {
            float dot = 0;
            for (int j = 0; j < dim; j++) dot += w[j] * svm.ind.V[i].X[j];
            float dist = (dot + w[dim]) / norm;

            if (svm.ind.V[i].cl.Y == 1) {
                if (dist < minPos) minPos = dist;
            } else {
                if (dist > maxNeg) maxNeg = dist;
            }
        }

        float margin = minPos - maxNeg;
        float asymmetry = minPos + maxNeg;
        float shift = asymmetry / 2.0f;

        w[dim] -= shift * norm;

        float minPosAfter = minPos - shift;
        float maxNegAfter = maxNeg - shift;

        svm.control.appendOutput(String.format("Closest class 1: %.4f\n", minPos));
        svm.control.appendOutput(String.format("Closest class 0: %.4f\n", maxNeg));
        svm.control.appendOutput(String.format("Asymmetry before shift: %.4f\n", asymmetry));
        svm.control.appendOutput(String.format("Bias shift applied: %.4f\n\n", shift));
        svm.control.appendOutput(String.format("Margin: %.4f (%.4f each side)\n\n", margin, margin / 2.0f));

        long finalTime = System.currentTimeMillis() - t;
        int finalAccuracy = getAccuracy(w);

        svm.control.appendOutput(String.format(
            "=== MAX MARGIN PERCEPTRON ===\n" +
            "Learning Rate (η): %.4f\n" +
            "Epochs (phase 1): %d\n" +
            "Accuracy: %d%%\n" +
            "Total Updates: %d\n" +
            "Final Margin: %.4f\n" +
            "Time: %dms\n" +
            "Status: %s\n",
            eta, epoch, finalAccuracy, totalUpdates,
            margin, finalTime,
            converged ? "CONVERGED" : "MAX EPOCHS"
        ));

        if (dim == 2)
            svm.design.setMarginLines(w, minPosAfter, maxNegAfter);

        finalize(w, epoch, true);
    }

    private void finalize(float[] w, int epoch, boolean showMargin) {
        long finalTime = System.currentTimeMillis() - t;
        int finalAccuracy = getAccuracy(w);

        svm.outd.stages_count = epoch;
        svm.outd.max_stages_count = P;
        svm.outd.computing_time = finalTime;
        svm.outd.w = w;
        svm.outd.accuracy = finalAccuracy;
        svm.outd.showOutputData();

        svm.control.showOutputData(String.valueOf(epoch), finalTime + " ms", w);

        if (dim == 2) {
            svm.design.setPointsOfLine(w);
            if (!showMargin) svm.design.show_margin = false;
        }

        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
        svm.control.start.setLabel("Start");
        svm.control.init = true;
    }
}
