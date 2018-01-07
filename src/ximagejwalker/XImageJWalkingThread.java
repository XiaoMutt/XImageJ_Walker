/*
 * GPLv3
 */
package ximagejwalker;

import FileManager.RoiProcessor;
import FileManager.FileIterator;
import FileManager.ResultsSaver;
import bsh.EvalError;
import bsh.Interpreter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;

/**
 *
 * @author Xiao Zhou
 */
public class XImageJWalkingThread extends SwingWorker<Object, String> {

    private String OpenFolder;
    private Pattern FileNameRegex;
    private String bsFileName;
    private boolean ignoreRois;
    protected volatile List<Roi> rois;
    private ResultsSaver rs;

    public void setIgnoreRois(boolean ignoreRois) {
        this.ignoreRois = ignoreRois;
    }

    /**
     * Fire a PausedAt property change, and send fileName to event-dispatching
     * thread that catches this property change. This thread will be paused.
     *
     * @param fileName the fileName
     */
    public synchronized void pauseWorker(String fileName) {
        firePropertyChange("PausedAt", "", fileName);

        try {
            wait();
        } catch (InterruptedException ex) {
            publish("WARINING: processing is canceled");
        }

    }

    /**
     * Resume current thread.
     *
     * @param rois ArrayList sent back to worker thread;
     */
    public synchronized void resumeWorker(List<Roi> rois) {
        //firePropertyChange("ResumedWith", "", PickedRois);
        this.rois = rois;
        notify();
    }

    @Override
    protected void process(List<String> chunks) {
        chunks.stream().forEach((str) -> {
            IJ.log(str);
        });
    }

    public String getBsFileName() {
        return bsFileName;
    }

    public void setBsFileName(String bsFileName) {
        this.bsFileName = bsFileName;
    }

    public void setOpenFolder(String OpenFolder) {
        this.OpenFolder = OpenFolder;
    }

    public void setFileNameRegex(String fileNameRegex) {
        FileNameRegex = Pattern.compile(fileNameRegex);
    }

    protected void processImage(String filePath) {

    }

    @Override
    protected void done() {
        if (rs != null) {
            //the done may be called several times during cancel or normal finish. This is to make sure the DataSaver is closed();
            rs.close();
        }

    }

    private ImagePlus temperImageForMeasurement(ImagePlus imp) throws EvalError, IOException {

        Interpreter ip = new Interpreter();
        imp.setOverlay(null);
        imp.deleteRoi();

        ip.set("imp", imp);
        ip.source(bsFileName);
        ip.eval("after()");
        return (ImagePlus) ip.get("imp");

    }

    @Override
    protected Void doInBackground() {
        String runTag = java.util.UUID.randomUUID().toString();
        String workingFilePath;
        List<String> results = null;
        boolean first = true;

        FileIterator xfi = new FileIterator(OpenFolder);
        try {
            rs = new ResultsSaver(OpenFolder + File.separator + runTag + "-Result.csv");

            while ((workingFilePath = xfi.nextFilePath()) != null && !isCancelled()) {
                //check name regex;
                File file = new File(workingFilePath);
                Matcher matcher = FileNameRegex.matcher(file.getName());
                if (matcher.matches()) {
                    publish("INFO: processing " + workingFilePath);
                    ImagePlus imp = IJ.openImage(workingFilePath);
                    if (!ignoreRois) {
                        this.rois = RoiProcessor.getRois(imp);
                    }

                    if (ignoreRois || this.rois == null || this.rois.isEmpty()) {
                        pauseWorker(workingFilePath);
                    }

                    if (this.rois != null && !this.rois.isEmpty()) {
                        try {
                            imp = this.temperImageForMeasurement(imp);
                            results = RoiProcessor.measureAll(this.rois, imp);
                            //add header;
                            if (first) {
                                String[] colums = Analyzer.getResultsTable().getHeadings();
                                rs.appendln(String.join(",", colums));
                                first = false;
                            }
                            
                            for (String s : results) {
                                rs.appendln(s);

                            }
                            publish("INFO: " + this.rois.size() + " Rois processed.");
                        } catch (EvalError ex) {
                            IJ.log("BeanShell Script Error: " + ex.getMessage());
                        }

                    } else {
                        publish("INTO: No Rois are processed.");
                    }
                }

            }
        } catch (IOException ex) {
            publish("Cannot save results: \n" + ex.getMessage());
        }

        return null;
    }

}
