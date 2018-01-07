/*
 * GPLv3
 */
package ximagejwalker;

import FileManager.RoiProcessor;
import bsh.EvalError;
import bsh.Interpreter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.StackWindow;
import ij.plugin.frame.RoiManager;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 *
 * @author Xiao Zhou
 */
public class XImageJWalkerPickupWindow extends StackWindow {

    private final XImageJWalkingThread xiwt;
    private final String fileName;
    private final RoiManager rm;

    private final Button refreshBn;
    private final Button doneBn;

    public XImageJWalkerPickupWindow(String fileName, XImageJWalkingThread xiwt) {
        super(IJ.openImage(fileName));
        this.imp.setOverlay(null);
        this.imp.deleteRoi();
        this.xiwt = xiwt;
        this.fileName = fileName;
        rm= new RoiManager();
        rm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                rm.setVisible(true);
            }
        });

        Panel pn=new Panel();
        pn.setLayout(new GridLayout(0,2));
        
        refreshBn = new Button();
        refreshBn.setLabel("Refresh");
        refreshBn.addActionListener((ActionEvent e) -> {
            refreshBeanShellRun();
        });
        pn.add(this.refreshBn);

        doneBn = new Button();
        doneBn.setLabel(">>> Done <<<");

        //when press done button add all roi to the overlay
        doneBn.addActionListener((ActionEvent e) -> {
            xiwt.resumeWorker(RoiProcessor.measureAll(rm.getRoisAsArray(), this.imp));
            RoiProcessor.saveImage(this.imp, rm.getRoisAsArray(), this.fileName);
            this.close();

        });
        pn.add(doneBn);
        this.add(pn);

    }

    @Override
    public boolean close() {
        rm.close();
        return super.close();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        this.close();
        xiwt.resumeWorker(null);
    }

    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
        pack();
        rm.setLocation(this.getLocation().x + this.getWidth(), this.getLocation().y);
        rm.setVisible(true);
        refreshBeanShellRun();
    }

    public void canel() {
        xiwt.cancel(true);
        this.close();
    }

    private void refreshBeanShellRun() {

        try {
            Interpreter itp = new Interpreter();
            ImagePlus impd = this.imp.duplicate();
            impd.setOverlay(null);
            impd.deleteRoi();
            itp.set("imp", impd);
            itp.source(xiwt.getBsFileName());
            this.imp.setOverlay(impd.getOverlay());
            impd.close();

        } catch (EvalError | IOException ex) {
            IJ.log("BeanShell Script Error: \n" + ex.getMessage());
        }
    }

}
