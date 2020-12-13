/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.model;

import java.io.Serializable;

/**
 *
 * @author thevpc
 */
public class NetbeansGroup implements Serializable{

    public static final long serialVersionUID = 1;
    private String name;
    private String fullName;

    public NetbeansGroup(String name, String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    
    public NetbeansGroup() {
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        if (name.isEmpty()) {
            return fullName;
        }
        if (name.equals(fullName)) {
            return fullName;
        }
        return fullName + " (" + name + ")";
    }

}
