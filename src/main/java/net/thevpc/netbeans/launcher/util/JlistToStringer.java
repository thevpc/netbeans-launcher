/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 *
 * @author vpc
 */
public abstract class JlistToStringer {
    private int maxLevel;

    public JlistToStringer(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    public abstract String toString(Object value, int level) ;

    public List<Object> toList(JList values) {
        List<Object> all = new ArrayList<>();
        final int size = values.getModel().getSize();
        for (int i = 0; i < size; i++) {
            all.add(values.getModel().getElementAt(i));
        }
        return all;
    }

    public HashMap<String, List<Object>> mapConflicts(List<Object> values, int level) {
        HashMap<String, List<Object>> vals = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            final Object v = values.get(i);
            String s = toString(v, level);
            List<Object> p = vals.get(s);
            if (p == null) {
                p = new ArrayList<>();
                vals.put(s, p);
            }
            p.add(v);
        }
        return vals;
    }

    public void extractConflicts(HashMap<String, List<Object>> vals, HashMap<String, Object> ok, HashMap<Object, String> okr) {
        for (Iterator<Map.Entry<String, List<Object>>> it = vals.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, List<Object>> entry = it.next();
            if (entry.getValue().size() == 1) {
                ok.put(entry.getKey(), entry.getValue().get(0));
                okr.put(entry.getValue().get(0), entry.getKey());
                it.remove();
            }
        }
    }

    public String toString(JList<?> list, Object value) {
        final ListModel<?> m = list.getModel();
        final int size = m.getSize();
        int level = 0;
        HashMap<String, Object> ok = new HashMap<>();
        HashMap<Object, String> okr = new HashMap<>();

        HashMap<String, List<Object>> vals = null;
        while (level < maxLevel) {
            if (level == 0) {
                vals = mapConflicts(toList(list), 0);
            } else {
                vals = mapConflicts(new ArrayList<>(vals.values()), level);
            }
            extractConflicts(vals, ok, okr);
            if (vals.isEmpty()) {
                break;
            }
            level++;
        }
        String p = okr.get(value);
        if (p == null) {
            p = toString(value, maxLevel - 1);
        }
        return p;
    }
    public String toString(List<?> list, Object value) {
        final int size = list.size();
        int level = 0;
        HashMap<String, Object> ok = new HashMap<>();
        HashMap<Object, String> okr = new HashMap<>();

        HashMap<String, List<Object>> vals = null;
        while (level < maxLevel) {
            if (level == 0) {
                vals = mapConflicts((List) list, 0);
            } else {
                vals = mapConflicts(new ArrayList<>(vals.values()), level);
            }
            extractConflicts(vals, ok, okr);
            if (vals.isEmpty()) {
                break;
            }
            level++;
        }
        String p = okr.get(value);
        if (p == null) {
            p = toString(value, maxLevel - 1);
        }
        return p;
    }
}
