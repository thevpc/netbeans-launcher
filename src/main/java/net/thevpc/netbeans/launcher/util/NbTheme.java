/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.util;

import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author vpc
 */
public class NbTheme {

    public static JTabbedPane prepare(JTabbedPane a) {
        a.setBackground(Color.WHITE);
        return a;
    }

    public static JScrollPane prepare(JScrollPane a) {
        a.getViewport().setBackground(Color.WHITE);
        return a;
    }
}
