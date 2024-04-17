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
public final class VersionAndDate {
    
    private String version;
    private Instant date;

    public VersionAndDate(String version, Instant date) {
        this.version = version;
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public Instant getDate() {
        return date;
    }
    
}
