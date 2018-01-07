/*
 * GPLv3
 */
package FileManager;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author xiao
 */
public class RoiProcessor {

    static public List<Roi> getRois(ImagePlus imp) {
        Overlay ol = imp.getOverlay();
        Roi[] rois = ol.toArray();
        return new ArrayList<>(Arrays.asList(rois));
    }

    static public List<String> measureAll(List<Roi> rois, ImagePlus imp) {
        Analyzer ana = new Analyzer(imp);
        List<String> results = new ArrayList<>();
        for (Roi roi : rois) {
            imp.setRoi(roi);
            ana.measure();
            ResultsTable rt = Analyzer.getResultsTable();
            String[] col = rt.getHeadings();
            String[] value = new String[col.length];
            for (int c = 0; c < col.length; c++) {
                value[c] = rt.getStringValue(col[c], 0);
            }
            results.add(String.join(",", value));
        }
        return results;

    }

    static public void saveImage(ImagePlus imp, Roi[] rois, String fileName) {
        Overlay ol = new Overlay();
        for (Roi roi : rois) {
            ol.add(roi);
        }
        imp.setOverlay(ol);

        FileSaver fs = new FileSaver(imp);

        String ext = "";
        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (i > p) {
            ext = fileName.substring(i + 1);
        }
        switch (ext.toLowerCase()) {
            case "bmp":
                fs.saveAsBmp(fileName);
                break;
            case "fits":
                fs.saveAsFits(fileName);
                break;
            case "gif":
                fs.saveAsGif(fileName);
                break;
            case "jpg":
            case "jpeg":
                fs.saveAsJpeg(fileName);
                break;
            case "lut":
                fs.saveAsLut(fileName);
                break;
            case "png":
                fs.saveAsPng(fileName);
                break;
            case "raw":
                fs.saveAsRaw(fileName);
                break;
            case "pgm":
                fs.saveAsPgm(fileName);
                break;
            case "tif":
            case "tiff":
                fs.saveAsTiff(fileName);
                break;
            case "zip":
                fs.saveAsZip(fileName);
                break;

        }

    }

}
