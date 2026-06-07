package tools;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import svm.SVM;

public class InputDataGenerator extends Dialog {
    SVM svm;
    TextField attributes_count, vectors_count, min, max, classes_count;
    TextField mg;  
    Checkbox liniar, als, cs, rs, nonSeparable;  
    Button generate, save;
    public TextArea ta;
    String dir = ".\\svm\\data", path;

    public InputDataGenerator(SVM svm) {
        super(svm, "Input Data Generator", true);
        this.svm = svm;
        setBackground(svm.settings.background_color_default);
        setResizable(false);
        resize(640, 480);
        move((svm.res.width - 640) / 2, (svm.res.height - 480) / 2);
        setLayout(null);

        int x1 = 10, x2 = 380;
        int y = 35;

        attributes_count = new TextField("2");
        vectors_count = new TextField("1000");
        min = new TextField("-1000");
        max = new TextField("1000");
        classes_count = new TextField("2");
        mg = new TextField("50");

        Label attLabel = new Label("Attributes Count:");
        attLabel.setBounds(x1, y, 150, 20);
        attLabel.setForeground(Color.white);
        add(attLabel);
        attributes_count.setBounds(x1 + 150, y, 100, 20);
        add(attributes_count);

        Label vecLabel = new Label("Vectors Count:");
        vecLabel.setBounds(x2, y, 150, 20);
        vecLabel.setForeground(Color.white);
        add(vecLabel);
        vectors_count.setBounds(x2 + 150, y, 100, 20);
        add(vectors_count);

        y += 30;
        
        Label minLabel = new Label("Minimum Coordinates:");
        minLabel.setBounds(x1, y, 150, 20);
        minLabel.setForeground(Color.white);
        add(minLabel);
        min.setBounds(x1 + 150, y, 100, 20);
        add(min);

        Label maxLabel = new Label("Maximum Coordinates:");
        maxLabel.setBounds(x2, y, 150, 20);
        maxLabel.setForeground(Color.white);
        add(maxLabel);
        max.setBounds(x2 + 150, y, 100, 20);
        add(max);

        y += 30;
        
        Label classLabel = new Label("Classes Count:");
        classLabel.setBounds(x1, y, 150, 20);
        classLabel.setForeground(Color.white);
        add(classLabel);
        classes_count.setBounds(x1 + 150, y, 100, 20);
        add(classes_count);

        liniar = new Checkbox("Linear separated");
        liniar.setBounds(x2, y, 150, 20);
        liniar.setForeground(Color.white);
        liniar.setState(true);
        add(liniar);

        y += 30;
        
        als = new Checkbox("Almost linear separated");
        als.setBounds(x1, y, 180, 20);
        als.setForeground(Color.white);
        add(als);

        cs = new Checkbox("Circular separated");
        cs.setBounds(x2, y, 150, 20);
        cs.setForeground(Color.white);
        add(cs);

        y += 30;
        
        rs = new Checkbox("Random separated");
        rs.setBounds(x1, y, 150, 20);
        rs.setForeground(Color.white);
        add(rs);

        nonSeparable = new Checkbox("Nonseparable linear");
        nonSeparable.setBounds(x2, y, 180, 20);
        nonSeparable.setForeground(Color.white);
        add(nonSeparable);

        y += 30;
        
        Label mgLabel = new Label("Margin / Noise %:");
        mgLabel.setBounds(x1, y, 150, 20);
        mgLabel.setForeground(Color.white);
        add(mgLabel);
        mg.setBounds(x1 + 150, y, 100, 20);
        add(mg);

        y += 40;
        generate = new Button("Generate");
        generate.setBounds(x1, y, 120, 30);
        generate.setBackground(svm.settings.button_color_default);
        generate.setForeground(svm.settings.button_label_default);
        add(generate);

        save = new Button("Save");
        save.setBounds(x2, y, 120, 30);
        save.setBackground(svm.settings.button_color_default);
        save.setForeground(svm.settings.button_label_default);
        add(save);

        y += 40;
        ta = new TextArea("");
        ta.setBounds(x1, y, size().width - 2 * x1, size().height - y - 10);
        ta.setBackground(svm.settings.button_color_default);
        ta.setForeground(svm.settings.string_color_default);
        add(ta);

        show();
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) {
            dispose();
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == generate) {
            generateData();
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == save) {
            saveGeneratedData();
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == liniar) {
            if (liniar.getState()) {
                als.setState(false); cs.setState(false); rs.setState(false); nonSeparable.setState(false);
                classes_count.setEnabled(false);
            }
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == nonSeparable) {
            if (nonSeparable.getState()) {
                liniar.setState(false); als.setState(false); cs.setState(false); rs.setState(false);
                classes_count.setEnabled(false);
            }
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == als) {
            if (als.getState()) {
                liniar.setState(false); cs.setState(false); rs.setState(false); nonSeparable.setState(false);
                classes_count.setEnabled(false);
            }
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == cs) {
            if (cs.getState()) {
                liniar.setState(false); als.setState(false); rs.setState(false); nonSeparable.setState(false);
                classes_count.setEnabled(false);
            }
            return true;
        } else if (e.id == Event.ACTION_EVENT && e.target == rs) {
            if (rs.getState()) {
                liniar.setState(false); als.setState(false); cs.setState(false); nonSeparable.setState(false);
                classes_count.setEnabled(true);
            }
            return true;
        }
        return super.handleEvent(e);
    }

    void generateData() {
        ta.setText("");
        try {
            int N = Integer.parseInt(vectors_count.getText());
            int n = Integer.parseInt(attributes_count.getText());
            int MIN = Integer.parseInt(min.getText());
            int MAX = Integer.parseInt(max.getText());
            float param = Float.parseFloat(mg.getText());

            if (N <= 1 || n <= 1 || MIN >= MAX) {
                ta.append("Error: Invalid parameters.\n");
                return;
            }

            if (liniar.getState()) generateLiniarData(N, n, MIN, MAX, param);
            else if (nonSeparable.getState()) generateNonSeparableLinearData(N, n, MIN, MAX, param);
            else if (als.getState()) generateAlmostLinearData(N, n, MIN, MAX, param);
            else if (cs.getState()) generateCircularData(N, n, MIN, MAX, param);
            else if (rs.getState()) generateRandomData(N, n, MIN, MAX, param);
            else ta.append("Please select a generation type.\n");
        } catch (NumberFormatException ex) {
            ta.append("Error: Please enter valid numbers.\n");
        }
    }

    private void generateLiniarData(int N, int n, int MIN, int MAX, float margin) {
        ta.append("% Linear separable, margin = " + margin + "\n");
        for (int i = 1; i <= n; i++) ta.append("@attribute attrib_" + i + " numeric\n");
        ta.append("@attribute class {0,1}\n@data\n");
        float[] w = new float[n + 1];
        for (int i = 0; i <= n; i++) w[i] = MIN + (float) Math.random() * (MAX - MIN);
        int generated = 0;
        while (generated < N) {
            float[] x = new float[n];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                x[i] = MIN + (float) Math.random() * (MAX - MIN);
                sb.append(x[i]).append(",");
            }
            float z = 0;
            for (int i = 0; i < n; i++) z += w[i] * x[i];
            z += w[n];
            int y;
            if (z > margin) y = 1;
            else if (z < -margin) y = 0;
            else continue;
            sb.append(y);
            ta.append(sb.toString() + "\n");
            generated++;
        }
    }

    private void generateNonSeparableLinearData(int N, int n, int MIN, int MAX, float noisePercent) {
        if (noisePercent < 0) noisePercent = 0;
        if (noisePercent > 100) noisePercent = 100;
        ta.append("% Non‑separable linear, noise = " + noisePercent + "%\n");
        for (int i = 1; i <= n; i++) ta.append("@attribute attrib_" + i + " numeric\n");
        ta.append("@attribute class {0,1}\n@data\n");
        float[] w = new float[n + 1];
        for (int i = 0; i <= n; i++) w[i] = MIN + (float) Math.random() * (MAX - MIN);
        int flipped = 0;
        for (int k = 0; k < N; k++) {
            float[] x = new float[n];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                x[i] = MIN + (float) Math.random() * (MAX - MIN);
                sb.append(x[i]).append(",");
            }
            float z = 0;
            for (int i = 0; i < n; i++) z += w[i] * x[i];
            z += w[n];
            int trueClass = (z >= 0) ? 1 : 0;
            int y = trueClass;
            if (Math.random() * 100 < noisePercent) {
                y = 1 - y;
                flipped++;
            }
            sb.append(y);
            ta.append(sb.toString() + "\n");
        }
        ta.append("% Flipped " + flipped + " out of " + N + " labels.\n");
    }

    private void generateAlmostLinearData(int N, int n, int MIN, int MAX, float margin) {
        ta.append("% Almost linear separated, margin = " + margin + "\n");
        for (int i = 1; i <= n; i++) ta.append("@attribute attrib_" + i + " numeric\n");
        ta.append("@attribute class {0,1}\n@data\n");
        float[] w = new float[n + 1];
        for (int i = 0; i <= n; i++) w[i] = MIN + (float) Math.random() * (MAX - MIN);
        for (int k = 0; k < N; k++) {
            float[] x = new float[n];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                x[i] = MIN + (float) Math.random() * (MAX - MIN);
                sb.append(x[i]).append(",");
            }
            float z = 0;
            for (int i = 0; i < n; i++) z += w[i] * x[i];
            z += w[n];
            int y;
            if (Math.abs(z) < margin) y = (Math.random() < 0.5) ? 0 : 1;
            else y = (z >= 0) ? 1 : 0;
            sb.append(y);
            ta.append(sb.toString() + "\n");
        }
    }

    private void generateCircularData(int N, int n, int MIN, int MAX, float radius) {
        ta.append("% Circular separated, radius = " + radius + "\n");
        for (int i = 1; i <= n; i++) ta.append("@attribute attrib_" + i + " numeric\n");
        ta.append("@attribute class {0,1}\n@data\n");
        float[] center = new float[n];
        for (int i = 0; i < n; i++) center[i] = (MIN + MAX) / 2.0f;
        int inside = 0, outside = 0;
        for (int k = 0; k < N; k++) {
            float[] x = new float[n];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                x[i] = MIN + (float) Math.random() * (MAX - MIN);
                sb.append(x[i]).append(",");
            }
            float distSq = 0;
            for (int i = 0; i < n; i++) distSq += (x[i] - center[i]) * (x[i] - center[i]);
            int y = (Math.sqrt(distSq) <= radius) ? 1 : 0;
            if (y == 1) inside++; else outside++;
            sb.append(y);
            ta.append(sb.toString() + "\n");
        }
        ta.append("% Inside: " + inside + ", Outside: " + outside + "\n");
    }

    private void generateRandomData(int N, int n, int MIN, int MAX, float dummy) {
        int C = Integer.parseInt(classes_count.getText());
        if (C < 2) C = 2;
        ta.append("% Random separated, classes = " + C + "\n");
        for (int i = 1; i <= n; i++) ta.append("@attribute attrib_" + i + " numeric\n");
        StringBuilder classList = new StringBuilder();
        for (int i = 0; i < C; i++) classList.append(i).append(i == C-1 ? "" : ",");
        ta.append("@attribute class {" + classList.toString() + "}\n@data\n");
        for (int k = 0; k < N; k++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append(MIN + (float) Math.random() * (MAX - MIN)).append(",");
            sb.append((int)(Math.random() * C));
            ta.append(sb.toString() + "\n");
        }
    }

    void saveGeneratedData() {
        if (ta.getText().isEmpty()) return;
        FileDialog fd = new FileDialog(this, "Save Generated Input Data", FileDialog.SAVE);
        if (dir != null) fd.setDirectory(dir);
        fd.setFile("*.csv");
        fd.setVisible(true);
        if (fd.getFile() != null) {
            dir = fd.getDirectory();
            String filePath = dir + fd.getFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                bw.write(ta.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}