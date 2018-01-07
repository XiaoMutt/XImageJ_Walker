

import ij.plugin.PlugIn;
import ximagejwalker.XImageJWalkerGUI;

/**
 *
 * @author xiao
 */
public class XImageJ_Walker implements PlugIn{


    @Override
    public void run(String string) {
        new XImageJWalkerGUI().setVisible(true);
    }
    
}
