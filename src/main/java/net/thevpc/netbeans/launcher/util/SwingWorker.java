package net.thevpc.netbeans.launcher.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SwingWorker {
    private Supplier<Boolean> start;
    private Runnable run;
    private Consumer<Exception> error;
    private Runnable success;
    private Runnable finallyRt;
    private Exception theError;
    private boolean startResult;
    private Map<String,Object> values=new LinkedHashMap<>();

    public SwingWorker() {
    }

    public void store(String s,Object v){
        values.put(s,v);
    }

    public <K> K load(String s){
        return (K) values.get(s);
    }

    public Runnable getFinally() {
        return finallyRt;
    }

    public SwingWorker onFinally(Runnable finallyRt) {
        this.finallyRt = finallyRt;
        return this;
    }

    public Supplier<Boolean> getStart() {
        return start;
    }

    public SwingWorker onStart(Supplier<Boolean> start) {
        this.start = start;
        return this;
    }

    public Runnable getRun() {
        return run;
    }

    public SwingWorker run(Runnable run) {
        this.run = run;
        return this;
    }

    public Consumer<Exception> getError() {
        return error;
    }

    public SwingWorker onError(Consumer<Exception> error) {
        this.error = error;
        return this;
    }

    public Runnable getSuccess() {
        return success;
    }

    public SwingWorker onSuccess(Runnable success) {
        this.success = success;
        return this;
    }

    public void start() {
        Workers.Worker a = null;
        startResult=true;
        if (start != null) {
            a = new Workers.Worker(true, new Runnable() {
                @Override
                public void run() {
                    Boolean b = start.get();
                    startResult=b!=null && b.booleanValue();
                }
            });
        }
        Workers.Worker wr = null;
        if (run != null) {
            wr = new Workers.Worker(false, new Runnable() {
                @Override
                public void run() {
                    if(startResult) {
                        try {
                            run.run();
                        } catch (Exception ex) {
                            theError = ex;
                        }
                    }
                }
            });
        }
        if (a != null) {
            if (wr != null) {
                a.then(wr);
            }
        } else {
            if (wr != null) {
                a = wr;
            }
        }
        Workers.Worker result = new Workers.Worker(true, new Runnable() {
            @Override
            public void run() {
                if (theError == null) {
                    if (success != null) {
                        try {
                            success.run();
                        }finally {
                            finallyRt.run();
                        }
                    }
                } else {
                    if (error != null) {
                        try {
                            error.accept(theError);
                        }finally {
                            finallyRt.run();
                        }
                    } else {
                        finallyRt.run();
                        throw new RuntimeException(theError);
                    }
                }
            }
        });
        if (a != null) {
            a.then(result);
        } else {
            a = result;
        }
        a.start();
    }
}
