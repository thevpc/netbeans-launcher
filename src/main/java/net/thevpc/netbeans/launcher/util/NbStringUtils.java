/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vpc
 */
public class NbStringUtils {

//    public static List<String> extractAll(String expr, String var) {
//        final Matcher m = match(expr, var);
//        List<String> all = new ArrayList<>();
//        if (m != null) {
//            while (m.find()) {
//                all.add(m.group(var));
//            }
//        }
//        return all;
//
//    }

    public static String extract(String str, String expr, String var) {
        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m.group(var);
        }
        return null;
    }

    public static Matcher match(String expr, String str) {
        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m;
        }
        return null;
    }

}
