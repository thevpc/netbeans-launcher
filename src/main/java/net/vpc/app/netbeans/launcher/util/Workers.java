package net.vpc.app.netbeans.launcher.util;

import javax.swing.*;

public class Workers {

    public static Worker swingWorker(Runnable r){
        return new Worker(true,r);
    }

    public static Worker nonSwingWorker(Runnable r){
        return new Worker(false,r);
    }

    public static SwingWorker richWorker(){
        return new SwingWorker();
    }


    public static class Worker {
        private boolean swing;
        private Runnable runnable;
        private Worker then;

        public Worker(boolean swing, Runnable runnable) {
            this.swing = swing;
            this.runnable = runnable;
        }

        void then(Worker w) {
            Worker x = this;
            while (x.then != null) {
                x = x.then;
            }
            x.then = w;
        }

        public void start() {
            if (swing) {
                if (SwingUtilities.isEventDispatchThread()) {
                    runnable.run();
                    if (then != null) {
                        then.start();
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            runnable.run();
                            if (then != null) {
                                then.start();
                            }
                        }
                    });
                }
            } else {
                if (SwingUtilities.isEventDispatchThread()) {
                    new Thread() {
                        @Override
                        public void run() {
                            runnable.run();
                            if (then != null) {
                                then.start();
                            }
                        }
                    }.start();
                } else {
                    runnable.run();
                    if (then != null) {
                        then.start();
                    }
                }
            }
        }
    }


}
