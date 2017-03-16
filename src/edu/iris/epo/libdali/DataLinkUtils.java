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

/**
 * DataLink Utility methods.
 * 
 * @author kevin
 */
public class DataLinkUtils implements DataLinkConst {
    private static void appendClientidPropertyValue(StringBuilder sb,
            String key) {
        appendClientidValue(sb, System.getProperty(key));
    }

    private static void appendClientidValue(StringBuilder sb, String s) {
        if (!isEmpty(s)) {
            s = s.replaceAll("\\s+", "_");
            sb.append(s);
        }
    }

    /**
     * Append the property value.
     * 
     * @param sb
     *            the string builder.
     * @param name
     *            the property name.
     * @param prefix
     *            the prefix text.
     * @param suffix
     *            the suffix text.
     */
    private static void appendPropertyValue(StringBuilder sb, String name,
            String prefix, String suffix) {
        final String s = System.getProperty(name);
        if (s != null) {
            sb.append(prefix);
            sb.append(s);
            sb.append(suffix);
        }
    }

    /**
     * Create the logger.
     * 
     * @return the logger.
     */
    public static IDataLinkLogger createLogger() {
        return new DataLinkLogger();
    }

    /**
     * Create the stream ID.
     * <p>
     * The stream identifier text for the stream in the composite form:
     * <p>
     * "W_X_Y_Z/TYPE" where the underscores and slash separate the components.
     * <p>
     * For SEED the stream ID text in the composite form:
     * <p>
     * "NET_STA_LOC_CHAN/MSEED" where the underscores and slash separate the
     * components and where NETwork, STAtion, LOCation and CHANnel follow the
     * FDSN SEED conventions.
     * 
     * @param s
     *            the stream identifier text.
     */
    public static IStreamid createStreamid(String s) {
        return new Streamid(s);
    }

    /**
     * Create the stream ID.
     * 
     * @param net
     *            the network code.
     * @param sta
     *            the station code.
     * @param loc
     *            the location code.
     * @param chan
     *            the channel code.
     */
    public static IStreamid createStreamid(String net, String sta, String loc,
            String chan) {
        return new StreamidSeed(net, sta, loc, chan);
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
    public static IStreamid createStreamid(String w, String x, String y,
            String z, String type) {
        return new Streamid(w, x, y, z, type);
    }

    /**
     * Generate a DataLink client ID.
     * 
     * @param progname
     *            the program name, usually the simple name of the main class.
     * @return a DataLink client ID.
     */
    public static String genClientid(String progname) {
        StringBuilder sb = new StringBuilder(MAXCLIENTIDLEN);
        appendClientidValue(sb, progname);
        sb.append(':');
        appendClientidPropertyValue(sb, "user.name");
        sb.append(':');
        appendClientidPropertyValue(sb, "os.name");
        sb.append('-');
        appendClientidPropertyValue(sb, "os.version");
        sb.append(":java_");
        appendClientidPropertyValue(sb, "java.version");
        if (sb.length() > MAXCLIENTIDLEN) {
            sb.setLength(MAXCLIENTIDLEN);
        }
        return sb.toString();
    }

    /**
     * Get the system information.
     * 
     * @return the system information.
     */
    public static String getSystemInfo() {
        StringBuilder sb = new StringBuilder("java version:");
        appendPropertyValue(sb, "java.version", SPACE + QUOTE, QUOTE);
        appendPropertyValue(sb, "java.vm.name", SPACE, EMPTY);
        appendPropertyValue(sb, "java.vm.version", " (", ")");
        sb.append(LINE_SEPARATOR);
        appendPropertyValue(sb, "os.name", "os:", EMPTY);
        appendPropertyValue(sb, "os.version", SPACE, EMPTY);
        appendPropertyValue(sb, "os.arch", " (", ")");
        return sb.toString();
    }

    /**
     * Get the text.
     * 
     * @param s
     *            the text or null if none.
     * @return the text or an empty string if none.
     */
    public static String getText(String s) {
        if (isEmpty(s)) {
            s = EMPTY;
        }
        return s;
    }

    /**
     * Determines if the String is null, empty or all white space.
     * 
     * @param s
     *            the String or null if none.
     * @return true if empty, false otherwise.
     */
    public static boolean isEmpty(String s) {
        if (s != null) {
            for (int index = 0; index < s.length(); index++) {
                if (!Character.isWhitespace(s.charAt(index))) {
                    return false;
                }
            }
        }
        return true;
    }
}
