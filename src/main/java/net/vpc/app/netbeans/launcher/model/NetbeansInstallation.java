/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NetbeansInstallation  implements Serializable{

    public static final long serialVersionUID = 1;
    private String path;
    private String version;
    private String name;
    private String userdir;
    private String cachedir;
    private String jdkhome;
    private String options;

    public String getJdkhome() {
        return jdkhome;
    }

    public void setJdkhome(String jdkhome) {
        if (jdkhome != null && jdkhome.trim().length() == 0) {
            jdkhome = null;
        }
        this.jdkhome = jdkhome;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return name;//+" (version "+version+" , path "+path+")";
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
    
        public String getUserdir() {
        return userdir;
    }

    public void setUserdir(String userdir) {
        if (userdir != null && userdir.trim().length() == 0) {
            userdir = null;
        }
        this.userdir = userdir;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.path);
        hash = 29 * hash + Objects.hashCode(this.version);
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.options);
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
        final NetbeansInstallation other = (NetbeansInstallation) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.options, other.options)) {
            return false;
        }
        return true;
    }
    
}
