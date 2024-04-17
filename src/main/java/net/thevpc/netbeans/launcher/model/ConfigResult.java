/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.model;

/**
 *
 * @author vpc
 */
public class ConfigResult {
    
    private int found = 0;
    private int installed = 0;

    public int getFound() {
        return found;
    }

    public ConfigResult setFound(int found) {
        this.found = found;
        return this;
    }

    public int getInstalled() {
        return installed;
    }

    public ConfigResult setInstalled(int installed) {
        this.installed = installed;
        return this;
    }

    public ConfigResult add(ConfigResult y) {
        if (y != null) {
            this.found += y.found;
            this.installed += y.installed;
        }
        return this;
    }
    
}
