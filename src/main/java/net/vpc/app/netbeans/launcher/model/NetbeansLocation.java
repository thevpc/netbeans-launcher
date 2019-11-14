/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author vpc
 */
public abstract class NetbeansLocation implements Serializable {

    public static final long serialVersionUID = 1;

    public abstract String getVersion();

}
