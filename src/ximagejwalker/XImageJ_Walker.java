/*
 * GPLv3
 */
package ximagejwalker;

import ij.plugin.PlugIn;

/**
 *
 * @author xiao
 */
public class XImageJ_Walker implements PlugIn {

    @Override
    public void run(String string) {
        new XImageJWalkerGUI().setVisible(true);
    }

}
