/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.util;

import net.thevpc.netbeans.launcher.model.WritableLongOperation;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;

/**
 *
 * @author vpc
 */
public class OpNInputStreamProgressMonitor implements NProgressListener {
    
    private final WritableLongOperation op;

    public OpNInputStreamProgressMonitor(WritableLongOperation op) {
        this.op = op;
    }

    @Override
    public boolean onProgress(NProgressEvent event) {
        switch (event.getState()) {
            case START:
                {
                    op.start(event.isIndeterminate());
                    break;
                }
            case COMPLETE:
                {
                    op.end();
                    break;
                }
            case PROGRESS:
                {
                    op.setPercent((float) (event.getProgress() * 100));
                    break;
                }
        }
        return true;
    }
    
}
