package Controller;

import Model.LoadBoardFromFile;
import Model.Model;
import Model.SaveBoardToFile;
import View.HomeFrame;
import View.OptionsPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.*;
import java.io.File;
import java.nio.file.FileSystems;

public class OptionPanelController implements ActionListener, Cloneable {

    private final OptionsPanel panel;
    private HomeFrame home;
    private Model model;

    private String path;


    public OptionPanelController(OptionsPanel panel, Model model) {
        this.model = model;
        this.panel = panel;
    }

    public void setHomeFrame(HomeFrame home) {
        this.home = home;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "switchToBuild":
                System.out.println("swap to build");
                removeListeners(home.getBoardPanel());
                home.swapToBuild();
                home.showNotification("Welcome to build mode");
                break;
            case "switchToRun":
                System.out.println("swap to run");
                removeListeners(home.getBoardPanel());
                home.swapToRun();
                home.getWindow().requestFocus();
                home.showNotification("Gizmoball");
                break;
            case "Save":
                JFileChooser jfc1 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                FileNameExtensionFilter filter1 = new FileNameExtensionFilter(".txt only!", "txt");
                jfc1.addChoosableFileFilter(filter1);
                jfc1.setFileFilter(filter1);
                jfc1.setCurrentDirectory(FileSystems.getDefault().getPath(".").toFile());
                int returnValue1 = jfc1.showDialog(this.panel, "save");
                if (returnValue1 == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc1.getSelectedFile();
                    String path = selectedFile.getAbsolutePath();

                    try {
                        SaveBoardToFile.saveToFile(path, model);
                    } catch (Exception ex) {
                        System.out.println("cant read");
                    }
                }
                break;
            case "Open":
                JFileChooser jfc2 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                FileNameExtensionFilter filter2 = new FileNameExtensionFilter(".txt", "txt");
                jfc2.addChoosableFileFilter(filter2);
                jfc2.setFileFilter(filter2);
                jfc2.setCurrentDirectory(FileSystems.getDefault().getPath(".").toFile());
                int returnValue2 = jfc2.showDialog(this.panel, "Open");
                if (returnValue2 == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc2.getSelectedFile();
                    String path = selectedFile.getAbsolutePath();
                    this.path = path;
                    try {
                        LoadBoardFromFile.readFromFile(path, model);
                        SaveBoardToFile.saveToFile(System.getProperty("java.io.tmpdir") + "gizmoTemp", model);
                        home.getRunModePanel().refreshTriggers();
                    } catch (Exception ex) {
                        System.out.println("cant read");
                    }
                }
                break;
            case "Reload":
                model.clear();
                try {
                    LoadBoardFromFile.readFromFile(path, model);
                } catch (Exception ex) {
                    System.out.println("cant read");
                }
                break;
            case "Quit":
                if (JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to quit?",
                        "Gizmoball",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
                final JOptionPane quitPane = new JOptionPane("Do you want to quit?\nBe sure you have saved!", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
                break;
        }
    }

    public void removeListeners(JPanel panel) {
        for (MouseListener ml : panel.getMouseListeners()) {
            panel.removeMouseListener(ml);
        }
        for (MouseMotionListener mm : panel.getMouseMotionListeners()) {
            panel.removeMouseMotionListener(mm);
        }
        for (KeyListener kl : panel.getKeyListeners()) {
            panel.removeKeyListener(kl);
        }
    }
}
