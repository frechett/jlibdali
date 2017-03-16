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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

/**
 * DataLink constants
 * 
 * @author kevin
 */
public interface DataLinkConst {
    /** The ASCII character set */
    public static final Charset ASCII = Charset.forName("US-ASCII");
    /** The DataLink address separator */
    public static final char DATALINK_ADDRESS_SEP = ':';
    /** The default DataLink host name */
    public static final String DATALINK_HOSTNAME = "localhost";
    /** The DataLink ID */
    public static final String DATALINK_ID = "ID DATALINK";
    /** The default DataLink I/O timeout */
    public static final int DATALINK_IOTIMEOUT = 2000;
    /** The default DataLink port */
    public static final int DATALINK_PORT = 16000;
    /** Earliest position in the DataLink buffer */
    public static final long DATALINK_POSITION_EARLIEST = -2L;
    /** Latest position in the DataLink buffer */
    public static final long DATALINK_POSITION_LATEST = -3L;
    /** EMPTY */
    public static final String EMPTY = "";
    /** Line separator. */
    public static final String LINE_SEPARATOR =
            System.getProperty("line.separator");
    /** Maximum client ID length */
    public static final int MAXCLIENTIDLEN = 200;
    /** Maximum header length */
    public static final int MAXHEADERLEN = 255;
    /** Maximum packet size */
    public static final int MAXPACKETSIZE = 16384;
    /** The miniSEED type */
    public static final String MSEED_TYPE = "MSEED";
    /** Quote text. */
    public static final String QUOTE = "\"";
    /** The regular expression for matching whitespace */
    public static final String RE_WS = "\\s+";
    /** The SeedLink packet size */
    public static final int SLINKPACKETSIZE = 512;
    /** Space text. */
    public static final String SPACE = " ";

    /** The default DataLink address */
    public static final SocketAddress DATALINK_ADDRESS =
            new InetSocketAddress(DATALINK_HOSTNAME, DATALINK_PORT);
    /** The default DataLink address text */
    public static final String DATALINK_ADDRESS_TEXT =
            DATALINK_HOSTNAME + DATALINK_ADDRESS_SEP + DATALINK_PORT;
}
