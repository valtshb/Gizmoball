package Controller;

import Model.Model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Model.LoadBoardFromFile;

import javax.swing.*;

import View.HomeFrame;
import View.RunModePanel;

public class RunModeController implements ActionListener {

    private Timer timer;
    private Model model;
    private RunModePanel runPanel;
    private HomeFrame home;

    public RunModeController(Model m, RunModePanel rp) {
        model = m;
        runPanel = rp;
        timer = new Timer(50, this);
    }

    public void setHome(HomeFrame home) {
        this.home = home;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            model.moveBalls();
        } else {

            switch (e.getActionCommand()) {

                case "":
                    timer.start();
                    runPanel.setStop();
                    home.showNotification("Running");
                    break;
                case " ":
                    timer.stop();
                    runPanel.setStart();
                    home.showNotification("Paused");
                case "Tick":
                    model.moveBalls();
                    break;
                case "Refresh":
                    model.clear();
                    try {
                        LoadBoardFromFile.readFromFile(System.getProperty("java.io.tmpdir") + "gizmoTemp.txt", model);
                    } catch (Exception ex) {
                        System.out.println("can't read");
                    }
                    break;
            }
        }
    }

    public void stopTime() {
        timer.stop();
    }

    public void startTime() {
        timer.start();
    }

}