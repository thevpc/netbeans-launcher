/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class ProcessMonitor implements Runnable {

    private Process process;
    private LinkedList<String> rows = new LinkedList<String>();
    private int maxRows = 100;

    public ProcessMonitor(Process process) {
        this.process = process;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = r.readLine()) != null) {
                synchronized (rows) {
                    rows.add(line);
                    if (rows.size() > maxRows) {
                        rows.removeFirst();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int waitFor() throws InterruptedException {
        int x = this.process.waitFor();
        if (x != 0) {
            StringBuilder sb = new StringBuilder();
            int logRows = 3;
            synchronized (rows) {
                for (String row : rows) {
                    if (sb.length() > 0) {
                        sb.append(" ; ");
                    }
                    sb.append(row);
                    logRows--;
                    if (logRows < 0) {
                        break;
                    }
                }
            }
            throw new RuntimeException("Execution Failed " + x + " : " + sb);
        }
        return x;
    }

    public Process getProcess() {
        return process;
    }

}
