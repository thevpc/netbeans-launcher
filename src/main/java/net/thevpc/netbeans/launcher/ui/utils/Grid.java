/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 *
 * @author vpc
 */
public class Grid extends GridBagConstraints {

    public static Grid at(int c, int r) {
        return new Grid().set(c, r);
    }

    public Grid() {
        this.gridwidth = 1;
        this.gridheight = 1;
        this.fill = HORIZONTAL;
        this.ipadx = 3;
        this.ipady = 4;
    }

    public Grid set(int c, int r) {
        this.gridx = c;
        this.gridy = r;
        return this;
    }

    public Grid expandH() {
        this.weightx++;
        return this;
    }

    public Grid expandV() {
        this.weighty++;
        return this;
    }

    public Grid span(int c, int r) {
        this.gridwidth = c;
        this.gridheight = r;
        return this;
    }

    public Grid fillVH() {
        this.fill = GridBagConstraints.BOTH;
        return this;
    }

    public Grid fillReminder() {
        this.fill = GridBagConstraints.REMAINDER;
        return this;
    }

    public Grid fillRelative() {
        this.fill = GridBagConstraints.RELATIVE;
        return this;
    }

    public Grid fillH() {
        this.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    public Grid fillV() {
        this.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    public Grid insets(int top, int left) {
        return insets(top, left, top, left);
    }

    public Grid insets(int top, int left, int bottom, int right) {
        this.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public Grid anchorNoth() {
        return anchor(GridBagConstraints.NORTH);
    }

    public Grid anchorNothWest() {
        return anchor(GridBagConstraints.NORTHWEST);
    }

    public Grid anchorSouth() {
        return anchor(GridBagConstraints.SOUTH);
    }

    public Grid anchorEast() {
        return anchor(GridBagConstraints.EAST);
    }

    public Grid anchorWest() {
        return anchor(GridBagConstraints.WEST);
    }

    public Grid anchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public Grid weight(int weightx, int weighty) {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }
}
