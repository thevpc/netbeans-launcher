/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.model;

import java.time.Instant;

/**
 *
 * @author vpc
 */
public class VersionData {
    
    private String version;
    private String fullVersion;
    private Instant releaseDate;

    public VersionData(String version, String fullVersion, Instant releaseDate) {
        this.setVersion(version);
        this.setFullVersion(fullVersion);
        this.releaseDate = releaseDate;
    }

    public Instant getReleaseDate() {
        return releaseDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public void setFullVersion(String fullVersion) {
        this.fullVersion = fullVersion;
    }
    
}
