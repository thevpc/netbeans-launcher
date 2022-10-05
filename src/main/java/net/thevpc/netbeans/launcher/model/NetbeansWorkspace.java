/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.model;

import net.thevpc.nuts.util.NutsStringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class NetbeansWorkspace implements Cloneable, Serializable {

    public static final long serialVersionUID = 1;
    private String path;
    private String userdir;
    private String cachedir;
    private String laf;
    private String name;
    private String group;
    private String jdkhome;
    private int fontSize = -1;
    private String options;
    private String cpAppend;
    private String cpPrepend;
    private String locale;
    private Instant lastLaunchDate;
    private Instant creationDate;
    private long executionCount;

    public NetbeansWorkspace() {
    }

    public String getJdkhome() {
        return jdkhome;
    }

    public void setJdkhome(String jdkhome) {
        if (jdkhome != null && jdkhome.trim().length() == 0) {
            jdkhome = null;
        }
        this.jdkhome = jdkhome;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCachedir() {
        return cachedir;
    }

    public void setCachedir(String cachedir) {
        if (cachedir != null && cachedir.trim().length() == 0) {
            cachedir = null;
        }
        this.cachedir = cachedir;
    }

    public String getLaf() {
        return laf;
    }

    public void setLaf(String laf) {
        this.laf = laf;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        if (group != null && group.trim().length() == 0) {
            group = null;
        }
        if (group != null && group.equals("--no-group")) {
            group = null;
        }
        this.group = group;
    }

    public String getUserdir() {
        return userdir;
    }

    public void setUserdir(String userdir) {
        if (userdir != null && userdir.trim().length() == 0) {
            userdir = null;
        }
        this.userdir = userdir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String n = name;
        if (n == null) {
            n = "";
        }
        n = n.trim();
        if (n.isEmpty()) {
            n = "NO_NAME";
        }
        return n;
    }

    public NetbeansWorkspace copy() {
        try {
            return (NetbeansWorkspace) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Should not happen");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.path));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.userdir));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.cachedir));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.name));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.group));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.jdkhome));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.laf));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.options));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.cpAppend));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.cpPrepend));
        hash = 23 * hash + Objects.hashCode(NutsStringUtils.trim(this.locale));
        hash = 23 * hash + this.fontSize;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NetbeansWorkspace other = (NetbeansWorkspace) obj;
        if (this.fontSize != other.fontSize) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.path), NutsStringUtils.trim(other.path))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.userdir), NutsStringUtils.trim(other.userdir))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.cachedir), NutsStringUtils.trim(other.cachedir))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.name), NutsStringUtils.trim(other.name))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.group), NutsStringUtils.trim(other.group))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.jdkhome), NutsStringUtils.trim(other.jdkhome))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.laf), NutsStringUtils.trim(other.laf))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.options), NutsStringUtils.trim( other.options))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.cpAppend), NutsStringUtils.trim(other.cpAppend))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.cpPrepend), NutsStringUtils.trim(other.cpPrepend))) {
            return false;
        }
        if (!Objects.equals(NutsStringUtils.trim(this.locale), NutsStringUtils.trim(other.locale))) {
            return false;
        }
        return true;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getCpAppend() {
        return cpAppend;
    }

    public void setCpAppend(String cpAppend) {
        this.cpAppend = cpAppend;
    }

    public String getCpPrepend() {
        return cpPrepend;
    }

    public void setCpPrepend(String cpPrepend) {
        this.cpPrepend = cpPrepend;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void copyFrom(NetbeansWorkspace w) {
        if(w!=this) {
            this.setName(w.getName());
            this.setPath(w.getPath());
            this.setUserdir(w.getUserdir());
            this.setCachedir(w.getCachedir());
            this.setJdkhome(w.getJdkhome());
            this.setGroup(w.getGroup());
            this.setFontSize(w.getFontSize());
            this.setLaf(w.getLaf());
            this.setOptions(w.getOptions());
            this.setCpAppend(w.getCpAppend());
            this.setCpPrepend(w.getCpPrepend());
            this.setLocale(w.getLocale());
        }
    }

    public Instant getLastLaunchDate() {
        return lastLaunchDate;
    }

    public void setLastLaunchDate(Instant lastLaunchDate) {
        this.lastLaunchDate = lastLaunchDate;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(long executionCount) {
        this.executionCount = executionCount;
    }


}
