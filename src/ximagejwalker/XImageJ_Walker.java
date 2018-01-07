/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
