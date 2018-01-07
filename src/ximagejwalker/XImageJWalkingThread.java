/*
 * GPLv3
 */
package ximagejwalker;

import FileManager.RoiProcessor;
import FileManager.FileIterator;
import FileManager.ResultsSaver;
import ij.IJ;
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
    protected volatile List<String> results;
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
    public synchronized void resumeWorker(List<String> rois) {
        //firePropertyChange("ResumedWith", "", PickedRois);
        this.results = rois;
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

    @Override
    protected Void doInBackground() {
        String runTag = java.util.UUID.randomUUID().toString();
        String workingFilePath;
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

                    if (!ignoreRois) {
                        this.results = RoiProcessor.measureAll(IJ.openImage(workingFilePath));
                    }
                    
                    if (ignoreRois||this.results.isEmpty()) {
                        pauseWorker(workingFilePath);
                    }

                    if (this.results != null) {
                        if (first) {
                            String[] colums = Analyzer.getResultsTable().getHeadings();
                            rs.appendln(String.join(",", colums));
                            first = false;
                        }
                        for (String s : this.results) {
                            rs.appendln(s);

                        }
                        publish("INFO: " + this.results.size() + " Rois processed.");
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
