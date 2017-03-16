/**
 *    Copyright (C) 2017 IRIS (http://www.iris.edu/hq/).
 *    
 *    All inquiries should be sent to John Taber <taber@iris.edu>.
 *    
 *    This file is part of Jlibdali.
 *
 *    Jlibdali is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Jlibdali is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Jlibdali.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.iris.epo.libdali;

public class Streamid implements IStreamid {
    private final String text, x, w, y, z, type;

    /**
     * Create the stream ID.
     * 
     * @param s
     *            the stream identifier text for the stream in the composite
     *            form: "W_X_Y_Z/TYPE" where the underscores and slash separate
     *            the components.
     */
    public Streamid(String s) {
        text = s;
        String w = null;
        String x = null;
        String y = null;
        String z = null;
        String type = null;

        if (!s.isEmpty()) {
            int fromIndex = 0;
            int index;

            index = s.indexOf('/');
            if (index >= 0) {
                type = s.substring(index + 1);
                s = s.substring(0, index);
            }

            index = s.indexOf('_', fromIndex);
            if (index >= 0) {
                w = s.substring(fromIndex, index);
                fromIndex = index + 1;
            }

            index = s.indexOf('_', fromIndex);
            if (index >= 0) {
                x = s.substring(fromIndex, index);
                fromIndex = index + 1;
            }

            index = s.indexOf('_', fromIndex);
            if (index >= 0) {
                y = s.substring(fromIndex, index);
                fromIndex = index + 1;
            }

            z = s.substring(fromIndex);
        }

        this.w = DataLinkUtils.getText(w);
        this.x = DataLinkUtils.getText(x);
        this.y = DataLinkUtils.getText(y);
        this.z = DataLinkUtils.getText(z);
        this.type = DataLinkUtils.getText(type);
    }

    /**
     * Create the stream ID.
     * 
     * @param w
     *            the w component.
     * @param x
     *            the x component.
     * @param y
     *            the y component.
     * @param z
     *            the z component.
     * @param type
     *            the type component.
     */
    public Streamid(String w, String x, String y, String z, String type) {
        this.w = DataLinkUtils.getText(w);
        this.x = DataLinkUtils.getText(x);
        this.y = DataLinkUtils.getText(y);
        this.z = DataLinkUtils.getText(z);
        this.type = DataLinkUtils.getText(type);
        text = String.format("%s_%s_%s_%s/%s", this.w, this.x, this.y, this.z,
                this.type);
    }

    @Override
    public boolean equals(Object obj) {
        return getText().equals(obj);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getW() {
        return w;
    }

    @Override
    public String getX() {
        return x;
    }

    @Override
    public String getY() {
        return y;
    }

    @Override
    public String getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public String toString() {
        return getText();
    }
}
