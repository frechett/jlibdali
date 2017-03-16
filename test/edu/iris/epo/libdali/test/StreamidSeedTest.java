package edu.iris.epo.libdali.test;

import edu.iris.epo.libdali.StreamidSeed;

public class StreamidSeedTest implements Runnable {

    public static void main(String[] args) {
        new StreamidSeedTest().run();
    }

    private String chan;
    private String loc;
    private String net;
    private String sta;
    private StreamidSeed streamid;
    private String text;

    public StreamidSeedTest() {
    }

    @Override
    public void run() {
        text = "";
        streamid = new StreamidSeed(text);
        validate(net, streamid.getNetworkCode());
        validate(sta, streamid.getStationCode());
        validate(loc, streamid.getLocationCode());
        validate(chan, streamid.getChannelCode());

        net = "IU";
        sta = "ANMO";
        loc = "00";
        chan = "BHZ";
        streamid = new StreamidSeed(net, sta, loc, chan);
        text = streamid.getText();
        System.out.println(text);

        validate(net, streamid.getNetworkCode());
        validate(sta, streamid.getStationCode());
        validate(loc, streamid.getLocationCode());
        validate(chan, streamid.getChannelCode());

        streamid = new StreamidSeed(text);
        validate(net, streamid.getNetworkCode());
        validate(sta, streamid.getStationCode());
        validate(loc, streamid.getLocationCode());
        validate(chan, streamid.getChannelCode());
    }

    private void validate(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        if (!s1.equals(s2)) {
            System.out.printf("\"%s\" does not match \"%s\"\n", s1, s2);
        }
    }
}
