/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.thevpc.netbeans.launcher.model.DefaultLongOperation;
import net.thevpc.netbeans.launcher.model.LongOperation;
import net.thevpc.netbeans.launcher.model.LongOperationListener;
import net.thevpc.netbeans.launcher.model.LongOperationStatus;
import net.thevpc.netbeans.launcher.model.WritableLongOperation;

/**
 *
 * @author vpc
 */
public class NetbeansRuntimeService {

    private File currentDirectory = new File(System.getProperty("user.home"));
    private List<WritableLongOperation> operations = new ArrayList<>();
    private List<LongOperationListener> operationListeners = new ArrayList<>();
    private NetbeansLauncherModule module;

    public NetbeansRuntimeService(NetbeansLauncherModule module) {
        this.module = module;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void fire(WritableLongOperation w) {
        if (w.getStatus() == LongOperationStatus.ENDED) {
            operations.remove(w);
        }
        for (LongOperationListener operationListener : operationListeners) {
            operationListener.onLongOperationProgress(w);
        }
    }

    public void addOperationListener(LongOperationListener listener) {
        operationListeners.add(listener);
    }

    public void removeOperationListener(LongOperationListener listener) {
        operationListeners.add(listener);
    }

    public LongOperation[] getOperations() {
        return operations.toArray(new LongOperation[0]);
    }

    public WritableLongOperation addOperation(String name) {
        DefaultLongOperation d = new DefaultLongOperation(module);
        d.setName(name);
        d.setStatus(LongOperationStatus.INIT);
        operations.add(d);
        return d;
    }

}
