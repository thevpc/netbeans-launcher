/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.model;

/**
 *
 * @author thevpc
 */
public class NbOsConfig {

    public static final long serialVersionUID = 1;
    private String[] programFolders;
    private String[] jdkFolders;
    private String configRoot;
    private String cacheRoot;
    private String netbeansExe;
    private String javaExe;

    public NbOsConfig() {
    }

    public NbOsConfig(String[] programFolders, String[] jdkFolders, String configRoot, String cacheRoot, String netbeansExe, String javaExe) {
        this.programFolders = programFolders;
        this.jdkFolders = jdkFolders;
        this.configRoot = configRoot;
        this.cacheRoot = cacheRoot;
        this.netbeansExe = netbeansExe;
        this.javaExe = javaExe;
    }

    public String getJavaExe() {
        return javaExe;
    }
    

    public String getNetbeansExe() {
        return netbeansExe;
    }

    public String[] getProgramFolders() {
        return programFolders;
    }

    public String[] getJdkFolders() {
        return jdkFolders;
    }

    public String getConfigRoot() {
        return configRoot;
    }

    public String getCacheRoot() {
        return cacheRoot;
    }


}
