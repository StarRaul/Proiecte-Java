package webcam;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import face.ImageUtils;

public class ImageViewer extends Dialog implements ActionListener, ItemListener {

    private String rootDir;
    private java.awt.List personList;
    private String currentPerson;
    private java.util.List<File> imageFiles;
    private int currentIndex;
    private ImagePanel imagePanel;
    private Label infoLabel;
    private Button btnPrev, btnNext, btnDelete, btnClose;

    private static final int DISPLAY_SIZE = 256;

    public ImageViewer(Frame parent, String rootDir) {
        super(parent, "Image Viewer - Training Data", false);
        this.rootDir    = rootDir;
        this.imageFiles = new java.util.ArrayList<File>();
        this.currentIndex = 0;

        setBackground(Color.darkGray);
        setLayout(new BorderLayout(5, 5));
        resize(700, 520);
        move(100, 100);

        buildUI();
        populatePersonList();
    }

    private void buildUI() {
        Panel leftPanel = new Panel(new BorderLayout());
        leftPanel.setBackground(Color.darkGray);

        Label persLabel = new Label("Persoane:", Label.CENTER);
        persLabel.setForeground(Color.white);
        leftPanel.add("North", persLabel);

        personList = new java.awt.List(10, false);
        personList.setBackground(Color.black);
        personList.setForeground(Color.green);
        personList.addItemListener(this);
        leftPanel.add("Center", personList);

        add("West", leftPanel);

        imagePanel = new ImagePanel();
        imagePanel.setBackground(Color.black);
        add("Center", imagePanel);

        Panel bottomPanel = new Panel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.darkGray);

        infoLabel = new Label("Selectati o persoana din lista.", Label.CENTER);
        infoLabel.setForeground(Color.white);
        bottomPanel.add("North", infoLabel);

        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(Color.darkGray);

        btnPrev   = new Button("< Inapoi");
        btnNext   = new Button("Inainte >");
        btnDelete = new Button("Sterge");
        btnClose  = new Button("Inchide");

        btnPrev.setBackground(Color.gray);
        btnNext.setBackground(Color.gray);
        btnDelete.setBackground(new Color(180, 50, 50));
        btnClose.setBackground(Color.gray);

        btnPrev.setForeground(Color.white);
        btnNext.setForeground(Color.white);
        btnDelete.setForeground(Color.white);
        btnClose.setForeground(Color.white);

        btnPrev.addActionListener(this);
        btnNext.addActionListener(this);
        btnDelete.addActionListener(this);
        btnClose.addActionListener(this);

        btnPanel.add(btnPrev);
        btnPanel.add(btnNext);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClose);

        bottomPanel.add("South", btnPanel);
        add("South", bottomPanel);
    }

    private void populatePersonList() {
        personList.removeAll();
        File root = new File(rootDir);
        if (!root.exists()) {
            infoLabel.setText("Directorul " + rootDir + " nu exista!");
            return;
        }
        File[] dirs = root.listFiles(new FileFilter() {
            public boolean accept(File f) { return f.isDirectory(); }
        });
        if (dirs == null || dirs.length == 0) {
            infoLabel.setText("Nu exista persoane in " + rootDir);
            return;
        }
        Arrays.sort(dirs);
        for (File d : dirs) {
            int cnt = countImages(d);
            personList.add(d.getName() + " (" + cnt + ")");
        }
    }

    private int countImages(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int cnt = 0;
        for (File f : files)
            if (ImageUtils.isImageFile(f.getName())) cnt++;
        return cnt;
    }

    private void loadPersonImages(String personName) {
        currentPerson = personName;
        imageFiles.clear();
        currentIndex = 0;

        File dir = new File(rootDir + File.separator + personName);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files)
            if (ImageUtils.isImageFile(f.getName()))
                imageFiles.add(f);

        Collections.sort(imageFiles);

        if (!imageFiles.isEmpty()) {
            showImage(0);
        } else {
            infoLabel.setText(personName + ": nicio imagine gasita.");
            imagePanel.setImage(null);
            imagePanel.repaint();
        }
    }

    private void showImage(int index) {
        if (imageFiles.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= imageFiles.size()) index = imageFiles.size() - 1;
        currentIndex = index;

        File f = imageFiles.get(currentIndex);
        BufferedImage img = ImageUtils.load(f.getAbsolutePath());

        if (img != null) {
            BufferedImage displayed = ImageUtils.resize(img, DISPLAY_SIZE, DISPLAY_SIZE);
            imagePanel.setImage(displayed);
            imagePanel.repaint();
        } else {
            imagePanel.setImage(null);
            imagePanel.repaint();
        }

        infoLabel.setText(currentPerson + "  |  Imaginea " + (currentIndex + 1)
                        + " din " + imageFiles.size()
                        + "  |  " + f.getName());

        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < imageFiles.size() - 1);
    }

    private void deleteCurrentImage() {
        if (imageFiles.isEmpty() || currentPerson == null) return;

        File toDelete = imageFiles.get(currentIndex);
        String name   = toDelete.getName();

        boolean deleted = toDelete.delete();
        if (deleted) {
            imageFiles.remove(currentIndex);
            populatePersonList();
            if (imageFiles.isEmpty()) {
                infoLabel.setText(currentPerson + ": toate imaginile au fost sterse.");
                imagePanel.setImage(null);
                imagePanel.repaint();
            } else {
                if (currentIndex >= imageFiles.size())
                    currentIndex = imageFiles.size() - 1;
                showImage(currentIndex);
            }
        } else {
            infoLabel.setText("Eroare: nu am putut sterge " + name);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if      (src == btnPrev)   showImage(currentIndex - 1);
        else if (src == btnNext)   showImage(currentIndex + 1);
        else if (src == btnDelete) deleteCurrentImage();
        else if (src == btnClose)  setVisible(false);
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == personList) {
            String selected = personList.getSelectedItem();
            if (selected != null) {
                int parenIdx = selected.lastIndexOf(" (");
                String name  = parenIdx >= 0
                             ? selected.substring(0, parenIdx)
                             : selected;
                loadPersonImages(name);
            }
        }
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) {
            setVisible(false);
            return true;
        }
        return super.handleEvent(e);
    }

    public void showViewer() {
        populatePersonList();
        setVisible(true);
    }

    private static class ImagePanel extends Panel {
        private BufferedImage image;

        public ImagePanel() { setBackground(Color.black); }

        public void setImage(BufferedImage img) { this.image = img; }

        public void paint(Graphics g) {
            g.setColor(Color.black);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (image == null) {
                g.setColor(Color.gray);
                g.drawString("Nicio imagine selectata",
                             getWidth()/2 - 60, getHeight()/2);
                return;
            }
            int x = (getWidth()  - image.getWidth())  / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            g.drawImage(image, x, y, this);
            g.setColor(Color.darkGray);
            g.drawRect(x-1, y-1, image.getWidth()+1, image.getHeight()+1);
        }

        public void update(Graphics g) { paint(g); }
    }
}
