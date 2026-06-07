package svm;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import gui.*;
import alg.*;
import tools.*;
import io.*;
import face.*;
import webcam.*;
import model.*;


public class SVM extends Frame {

    public Toolkit   tool;
    public MenuBar   mb;
    public Dimension res;
    public Image     ico, bkg, color, calculates;

    public Design           design;
    public Settings         settings;
    public SimulationControl control;
    public About            about;
    public Options          options;

    public OutputData outd;
    public InputData  ind;

    public Algorithm algorithm;

    public FaceDetector faceDetector;
    public DataCollector dataCollector;
    public ImageViewer imageViewer;
    public FaceRecognizer faceRecognizer;
    public static final String TRAINING_DIR = "training_data";
    public static final int IMAGES_PER_PERSON = 500;

    public static void main(String args[]) { new SVM(); }

    public SVM() {
        tool = getToolkit();
        res  = tool.getScreenSize();
        loadImages();
        setIconImage(ico);
        setTitle("SVM Simulator - Face Recognition");
        adaugaMenuBar();

        design = new Design(this);
        add("Center", design);

        settings = new Settings(this);
        settings.resize(376, 600);
        settings.move((res.width - 376) / 2, (res.height - 600) / 2);

        about = new About(this);
        about.resize(712, 410);
        about.move((res.width - 712) / 2, (res.height - 410) / 2);

        control = new SimulationControl(this, 400, res.height - 80);
        control.resize(400, res.height - 80);
        control.move(res.width - 405, 35);

        options = new Options(this);

        outd = new OutputData(this);
        ind  = new InputData(this);

        initFaceComponents();

        setResizable(false);
        setBackground(settings.background_color);
        resize(res.width, res.height - 40);
        move(0, 0);
        show();
    }

    private void initFaceComponents() {
        faceDetector = new FaceDetector();
        File modelFile = new File(FaceDetector.DEFAULT_MODEL_PATH);
        if (modelFile.exists()) {
            faceDetector.loadModel(FaceDetector.DEFAULT_MODEL_PATH);
            System.out.println("SVM: model detectie cap incarcat.");
        } else {
            System.out.println("SVM: modelul de detectie cap nu exista inca.");
            System.out.println("     Folositi Face Detection > Train Head Detector.");
        }

        faceRecognizer = new FaceRecognizer(faceDetector, TRAINING_DIR);

        File recogFile = new File(FaceRecognizer.MODELS_PATH);
        if (recogFile.exists()) {
            faceRecognizer.loadModels(FaceRecognizer.MODELS_PATH);
            System.out.println("SVM: modele recunoastere incarcate: "
                             + faceRecognizer.getPersonCount() + " persoane.");
        }

        dataCollector = new DataCollector(faceDetector,
                                          faceRecognizer.getWebcam(),
                                          TRAINING_DIR, IMAGES_PER_PERSON);

        imageViewer = new ImageViewer(this, TRAINING_DIR);

        faceRecognizer.setFrameListener(new FaceRecognizer.FrameListener() {
            public void onFrame(BufferedImage frame, String[] names) {
                design.setLiveFrame(frame);
                design.repaint();
            }
        });
    }

    void adaugaMenuBar() {
        mb = new MenuBar();

        Menu file = new Menu("File");
        file.add("Load Input Data");
        file.add("-");
        file.add("Exit");
        mb.add(file);

        Menu algorithms = new Menu("Algorithms");
        algorithms.add("Median");
        algorithms.add("Perceptron");
        algorithms.add("Median-Perceptron");
        algorithms.add("Dual Perceptron");
        algorithms.add("Dual Perceptron NS");
        algorithms.add("-");
        algorithms.add("SMO Sigmoid");
        mb.add(algorithms);

        Menu view = new Menu("View");
        view.add("Show Simulation Control");
        view.add("Show Input Data");
        view.add("Show Output Data");
        view.add("-");
        view.add("Show Cursor Coordinates");
        mb.add(view);

        Menu faceDetMenu = new Menu("Face Detection");
        faceDetMenu.add("Train Head Detector");
        faceDetMenu.add("Load Head Detector");
        mb.add(faceDetMenu);

        Menu faceRecMenu = new Menu("Face Recognition");
        faceRecMenu.add("Collect Training Data");
        faceRecMenu.add("View Training Data");
        faceRecMenu.add("-");
        faceRecMenu.add("Train Recognizer");
        faceRecMenu.add("Load Recognizer");
        faceRecMenu.add("-");
        faceRecMenu.add("Start Live Recognition"); 
        faceRecMenu.add("Stop Live Recognition");  
        mb.add(faceRecMenu);

        Menu tools = new Menu("Tools");
        tools.add("Input Data Generator");
        tools.add("-");
        tools.add("Settings");
        mb.add(tools);

        Menu help = new Menu("Help");
        help.add("Help");
        help.add("About");
        mb.add(help);

        setMenuBar(mb);
    }

    public URL getResources(String s) { return this.getClass().getResource(s); }

    public void loadImages() {
        try {
            bkg        = tool.getImage(getResources("res/bkg.jpg"));
            ico        = tool.getImage(getResources("res/ico.png"));
            color      = tool.getImage(getResources("res/color.png"));
            calculates = tool.getImage(getResources("res/calculates.gif"));
        } catch (Throwable e) {
            System.out.println("Eroare la incarcarea imaginilor!");
        }
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) {
            if (faceRecognizer != null && faceRecognizer.isRunning())
                faceRecognizer.stopLive();
            System.exit(0);

        } else if (e.id == Event.ACTION_EVENT && e.target instanceof MenuItem) {
            String cmd = (String) e.arg;

            if ("Exit".equals(cmd)) {
                if (faceRecognizer != null && faceRecognizer.isRunning())
                    faceRecognizer.stopLive();
                System.exit(0);

            } else if ("Load Input Data".equals(cmd)) {
                ind.loadInputData();
                return true;

            } else if ("Median".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new Median(this);
                    showControl();
                }
                return true;

            } else if ("Perceptron".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new Perceptron(this);
                    showControl();
                }
                return true;

            } else if ("Median-Perceptron".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new MPerceptron(this);
                    showControl();
                }
                return true;

            } else if ("Dual Perceptron".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new DualPerceptron(this);
                    showControl();
                }
                return true;

            } else if ("Dual Perceptron NS".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new DualPerceptronNS(this);
                    showControl();
                }
                return true;

            } else if ("SMO Sigmoid".equals(cmd)) {
                if (ind.V != null) {
                    resetAlgorithm();
                    algorithm = new SMO(this);
                    showControl();
                }
                return true;

            } else if ("Show Simulation Control".equals(cmd)) {
                control.show();
                mb.getMenu(2).getItem(0).setLabel("Hide Simulation Control");
                return true;
            } else if ("Hide Simulation Control".equals(cmd)) {
                control.hide();
                mb.getMenu(2).getItem(0).setLabel("Show Simulation Control");
                return true;
            } else if ("Show Input Data".equals(cmd)) {
                ind.show();
                mb.getMenu(2).getItem(1).setLabel("Hide Input Data");
                return true;
            } else if ("Hide Input Data".equals(cmd)) {
                ind.hide();
                mb.getMenu(2).getItem(1).setLabel("Show Input Data");
                return true;
            } else if ("Show Output Data".equals(cmd)) {
                outd.show();
                mb.getMenu(2).getItem(2).setLabel("Hide Output Data");
                return true;
            } else if ("Hide Output Data".equals(cmd)) {
                outd.hide();
                mb.getMenu(2).getItem(2).setLabel("Show Output Data");
                return true;
            } else if ("Show Cursor Coordinates".equals(cmd)) {
                design.show_coords = true;
                design.repaint();
                mb.getMenu(2).getItem(4).setLabel("Hide Cursor Coordinates");
                return true;
            } else if ("Hide Cursor Coordinates".equals(cmd)) {
                design.show_coords = false;
                design.repaint();
                mb.getMenu(2).getItem(4).setLabel("Show Cursor Coordinates");
                return true;

            } else if ("Train Head Detector".equals(cmd)) {
                handleTrainHeadDetector();
                return true;

            } else if ("Load Head Detector".equals(cmd)) {
                boolean ok = faceDetector.loadModel(FaceDetector.DEFAULT_MODEL_PATH);
                showMessage(ok ? "Model detectie cap incarcat cu succes!"
                              : "Eroare: modelul nu a putut fi incarcat.\n"
                              + "Verificati ca fisierul '"
                              + FaceDetector.DEFAULT_MODEL_PATH + "' exista.");
                return true;

            } else if ("Collect Training Data".equals(cmd)) {
                handleCollectTrainingData();
                return true;

            } else if ("View Training Data".equals(cmd)) {
                imageViewer.showViewer();
                return true;

            } else if ("Train Recognizer".equals(cmd)) {
                handleTrainRecognizer();
                return true;

            } else if ("Load Recognizer".equals(cmd)) {
                boolean ok = faceRecognizer.loadModels(FaceRecognizer.MODELS_PATH);
                showMessage(ok ? "Modele incarcate: "
                              + faceRecognizer.getPersonCount() + " persoane."
                              : "Eroare: modelele nu au putut fi incarcate.");
                return true;

            } else if ("Start Live Recognition".equals(cmd)) {
                handleStartLive();
                return true;

            } else if ("Stop Live Recognition".equals(cmd)) {
                faceRecognizer.stopLive();
                design.setLiveFrame(null);
                design.repaint();
                return true;

            } else if ("Input Data Generator".equals(cmd)) {
                new InputDataGenerator(this);
                return true;
            } else if ("Settings".equals(cmd)) {
                settings.loadSettings();
                settings.show();
                return true;

            } else if ("Help".equals(cmd)) {
                File helpFile = new File("svm/SVM.pdf");
                try {
                    if (helpFile.toString().endsWith(".pdf"))
                        Runtime.getRuntime().exec(
                            "rundll32 url.dll,FileProtocolHandler " + helpFile);
                    else
                        Desktop.getDesktop().open(helpFile);
                } catch (IOException ex) {
                    System.out.println("No application registered for PDFs!");
                }
                return true;
            } else if ("About".equals(cmd)) {
                about.show();
                return true;
            }

        } else {
            return false;
        }
        return super.handleEvent(e);
    }

    private void handleTrainHeadDetector() {
        if (!faceDetector.isReady()) {
            showMessage("ATENTIE: Antrenarea poate dura mult timp (minute).\n"
                      + "Asigurati-va ca aveti:\n"
                      + "  - Directorul 'positives/' cu imagini de fete\n"
                      + "  - Directorul 'negatives/' cu imagini fara fete\n\n"
                      + "Antrenarea incepe acum in fundal.");
        }
        Thread t = new Thread(new Runnable() {
            public void run() {
                faceDetector.train(
                    "positives",          
                    "negatives",          
                    1,                    
                    FaceDetector.DEFAULT_MODEL_PATH,
                    FaceDetector.DEFAULT_HOG_PATH
                );
                showMessage("Antrenare detector cap finalizata!\n"
                          + "Model salvat in: " + FaceDetector.DEFAULT_MODEL_PATH);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleCollectTrainingData() {
        if (!faceDetector.isReady()) {
            showMessage("Eroare: Detectorul de cap nu este incarcat!\n"
                      + "Folositi mai intai Face Detection > Train/Load Head Detector.");
            return;
        }

        final Dialog dlg = new Dialog(this, "Colectare imagini", true);
        dlg.setLayout(new BorderLayout(5, 5));
        dlg.setBackground(Color.darkGray);
        dlg.resize(320, 160);
        dlg.move((res.width - 320) / 2, (res.height - 160) / 2);

        Panel centerP = new Panel(new GridLayout(2, 2, 5, 5));
        centerP.setBackground(Color.darkGray);

        Label lbl = new Label("Pseudonim persoana:");
        lbl.setForeground(Color.white);
        final TextField tf = new TextField("", 20);
        tf.setBackground(Color.black);
        tf.setForeground(Color.green);

        Label lblN = new Label("Numar imagini:");
        lblN.setForeground(Color.white);
        final TextField tfN = new TextField(IMAGES_PER_PERSON + "", 10);
        tfN.setBackground(Color.black);
        tfN.setForeground(Color.green);

        centerP.add(lbl); centerP.add(tf);
        centerP.add(lblN); centerP.add(tfN);
        dlg.add("Center", centerP);

        Panel btnP = new Panel(new FlowLayout());
        btnP.setBackground(Color.darkGray);
        Button btnOk  = new Button("Start colectare");
        Button btnCancel = new Button("Anuleaza");
        btnOk.setBackground(new Color(50, 120, 50));
        btnOk.setForeground(Color.white);
        btnCancel.setBackground(Color.gray);
        btnCancel.setForeground(Color.white);

        final boolean[] ok = {false};
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ok[0] = true; dlg.dispose(); }
        });
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dlg.dispose(); }
        });

        btnP.add(btnOk); btnP.add(btnCancel);
        dlg.add("South", btnP);
        dlg.setVisible(true); 

        if (!ok[0]) return;

        final String personName = tf.getText().trim();
        if (personName.isEmpty()) {
            showMessage("Pseudonimul nu poate fi gol!");
            return;
        }

        int nImages = IMAGES_PER_PERSON;
        try {
            nImages = Integer.parseInt(tfN.getText().trim());
        } catch (NumberFormatException ex) {
            nImages = IMAGES_PER_PERSON;
        }
        final int finalN = nImages;

        dataCollector.setImagesPerPerson(finalN);

        dataCollector.setProgressListener(new DataCollector.ProgressListener() {
            public void onProgress(String name, int saved, int total,
                                   BufferedImage lastFrame) {
                System.out.println("Colectare " + name + ": "
                                 + saved + "/" + total);
                design.setLiveFrame(lastFrame);
                design.setCollectingMode(true);
                design.repaint();
            }
            public void onDone(String name, int saved) {
                design.setLiveFrame(null);
                design.setCollectingMode(false);
                design.repaint();
                showMessage("Colectare finalizata pentru " + name
                          + ": " + saved + " imagini salvate.");
            }
            public void onNoFaceDetected(String name) {
            }
        });

        Thread t = new Thread(new Runnable() {
            public void run() { dataCollector.collect(personName); }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleTrainRecognizer() {
        File trainDir = new File(TRAINING_DIR);
        if (!trainDir.exists() || trainDir.listFiles() == null) {
            showMessage("Eroare: directorul '" + TRAINING_DIR + "' nu exista!\n"
                      + "Folositi mai intai Face Recognition > Collect Training Data.");
            return;
        }

        showMessage("Antrenarea clasificatoarelor incepe acum in fundal.\n"
                  + "Poate dura cateva minute in functie de numarul de persoane.");

        Thread t = new Thread(new Runnable() {
            public void run() {
                faceRecognizer.trainAll(FaceRecognizer.MODELS_PATH);
                showMessage("Antrenare finalizata!\n"
                          + faceRecognizer.getPersonCount()
                          + " clasificatoare salvate.");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleStartLive() {
        if (!faceDetector.isReady()) {
            showMessage("Eroare: Detectorul de cap nu este incarcat!");
            return;
        }
        if (faceRecognizer.getPersonCount() == 0) {
            showMessage("Eroare: Nu exista modele de recunoastere incarcate!\n"
                      + "Folositi Face Recognition > Train/Load Recognizer.");
            return;
        }
        if (faceRecognizer.isRunning()) {
            showMessage("Recunoasterea live ruleaza deja!");
            return;
        }
        faceRecognizer.startLiveAsync();
        showMessage("Recunoastere live pornita la 10 FPS.\n"
                  + "Persoane in model: " + faceRecognizer.getPersonCount()
                  + "\nFolositi Stop Live Recognition pentru oprire.");
    }

    private void showMessage(String msg) {
        final String message = msg;
        final Dialog dlg = new Dialog(this, "Informatie", true); 
        dlg.setLayout(new BorderLayout(5, 5));
        dlg.setBackground(Color.darkGray);
        dlg.resize(380, 160);
        dlg.move((res.width - 380) / 2, (res.height - 160) / 2);

        TextArea ta = new TextArea(message, 4, 40,
                                    TextArea.SCROLLBARS_VERTICAL_ONLY);
        ta.setBackground(Color.black);
        ta.setForeground(Color.white);
        ta.setEditable(false);
        dlg.add("Center", ta);

        Panel p = new Panel(new FlowLayout());
        p.setBackground(Color.darkGray);
        Button btn = new Button("OK");
        btn.setBackground(Color.gray);
        btn.setForeground(Color.white);
        
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e2) {
                dlg.dispose();
            }
        });
        
        p.add(btn);
        dlg.add("South", p);
        dlg.setVisible(true);
    }

    private void resetAlgorithm() {
        if (algorithm != null) {
            algorithm.stop_();
            algorithm = null;
            init2();
        }
    }

    private void showControl() {
        control.show();
        mb.getMenu(2).getItem(0).setLabel("Hide Simulation Control");
    }

    public void init() {
        if (algorithm != null) {
            algorithm.stop_();
            algorithm = null;
        }
        ind.input_file = null;
        ind.V          = null;
        design.show_line   = false;
        design.calculates  = false;
        control.init       = true;
        control.start.setLabel("Start Simulation");
        design.repaint();
    }

    public void init2() {
        design.show_line  = false;
        design.calculates = false;
        control.init      = true;
        control.start.setLabel("Start Simulation");
        design.repaint();
        ind.init();
    }
}
