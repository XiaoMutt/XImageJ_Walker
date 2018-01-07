
# XImageJ_Walker

A ImageJ Plugin lets you transverse all images in a folder, process each image through a BeanShell script, and save the measurement data in a CSV file. 

XImageJ_Walker will ask the following:
- a folder contains your images
- a image file name pattern in regular expression
- a BeanShell script to process the images to get Rois
- a channel you would like to measure

Click Run, this PlugIn will:
- Transverse every file following the name pattern
- Popup the image file, send a copy of the image (the ImagePlus object) to the "imp" variable in the BeanShell script you specified, and run the BeanSehll script to get Rois
- The image file with the RoiManager will popup, for you to select and edit the Rois
- You can edit and rerun the BeanShell file (remember to save the file first) by clicking the Refresh button
- Click the done button, the Rois in the RoiManager will be returned and used to measure the images
- The measurements will be saved in a result file in the same folder.

Notes:
- You can set up the measurement first in "Analyze->Set Measurements", or you can setup the measurement by calling "Set Measurements" in the BeanShell script.
- The Plugin will send the ImagePlus object of an image file to the "imp" variable of the BeanShell script, so the BeanShell script should use the variable name imp to get an process the image.
- The Plugin use the Rois in the RoiManager, so make sure the Rois are loaded into the RoiManager using the BeanShell script.
- If all the image files have an Overlay containiing all Rois, you can uncheck the "Ignore any save Rois...". The Plugin will use the saved Rois.

Example:
- Recognize white objects in a black background and measure the mean intensity as well as the area.
- the BeanShell file will be (you can record using "Plugins->Macros->Record..." and choose BeanShell:
```java
//import the IJ class, you also import any package you need
import ij.IJ;

//delete the irrelevant channel
imp.getImageStack().deleteSlice(2);

//clean up imp and make binary
IJ.run(imp, "8-bit", "");
IJ.run(imp, "Subtract Background...", "rolling=100");
IJ.run(imp, "Median...", "radius=3");
IJ.run(imp, "Auto Threshold", "method=Li white");
IJ.run(imp, "Make Binary", "");

//analyze particles and add to RoiManager
IJ.run(imp, "Analyze Particles...", "size=10-Infinity exclude clear add");

//set measurements 
IJ.run("Set Measurements...", "area mean display redirect=None decimal=2");
```