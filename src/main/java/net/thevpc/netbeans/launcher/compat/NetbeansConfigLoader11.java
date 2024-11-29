/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.compat;

import net.thevpc.netbeans.launcher.model.NetbeansConfig;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class NetbeansConfigLoader11 {

    private static void visit(Object o) {
        if (o instanceof Map) {
            Iterator<Map.Entry> ee = ((Map) o).entrySet().iterator();
            while (ee.hasNext()) {
                Map.Entry e = ee.next();
                String k = (String) e.getKey();
                Object v = e.getValue();
                if (k.equals("creationDate") && v instanceof Map) {
                    ee.remove();
                } else {
                    visit(v);
                }
            }
        } else if (o instanceof List) {
            for (Object object : ((List) o)) {
                visit(object);
            }
        }
    }

    public static NetbeansConfig load(Path file, NSession ws) {
        NElements json = NElements.of().json();
        Map o = json.parse(file, Map.class);
        visit(o);
        StringWriter sw = new StringWriter();
        json.setNtf(false).setValue(o).print(sw);
        NetbeansConfig c = json.parse(new StringReader(sw.toString()), NetbeansConfig.class);
        return c;
    }
}
