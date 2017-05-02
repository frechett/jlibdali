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

public class StreamidSeed extends Streamid implements DataLinkConst {
    /**
     * Get the location code replacing the
     * {@link DataLinkConst#SEED_LOC_NULL_STRING} with an empty string if
     * needed.
     * 
     * @param loc
     *            the location code.
     * @return the location code.
     */
    public static String getLoc(String loc) {
        if (SEED_LOC_NULL_STRING.equals(loc)) {
            loc = EMPTY;
        }
        return loc;
    }

    /**
     * Create the SEED stream ID.
     * <p>
     * For example, the stream ID text:
     * <p>
     * <code>IU_ANMO_00_BHZ/MSEED</code>
     * 
     * @param s
     *            the stream ID text in the composite form:
     *            "NET_STA_LOC_CHAN/MSEED" where the underscores and slash
     *            separate the components and where NETwork, STAtion, LOCation
     *            and CHANnel follow the FDSN SEED conventions.
     */
    public StreamidSeed(String s) {
        super(s);
    }

    /**
     * Create the SEED stream ID.
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
    public StreamidSeed(String net, String sta, String loc, String chan) {
        super(net, sta, getLoc(loc), chan, MSEED_TYPE);
    }

    /**
     * Returns the channel code.
     * 
     * @return The channel code string.
     */
    public String getChannelCode() {
        return getZ();
    }

    /**
     * Returns the location code.
     * 
     * @return The location code string.
     */
    public String getLocationCode() {
        return getY();
    }

    /**
     * Returns the network code.
     * 
     * @return The network code string.
     */
    public String getNetworkCode() {
        return getW();
    }

    /**
     * Returns the station code.
     * 
     * @return The station code string.
     */
    public String getStationCode() {
        return getX();
    }

    /**
     * Get the stream identifier text.
     * 
     * @return the stream ID text in the composite form:
     *         "NET_STA_LOC_CHAN/MSEED" where the underscores and slash separate
     *         the components and where NETwork, STAtion, LOCation and CHANnel
     *         follow the FDSN SEED conventions.
     */
    @Override
    public String getText() {
        return super.getText();
    }
}
