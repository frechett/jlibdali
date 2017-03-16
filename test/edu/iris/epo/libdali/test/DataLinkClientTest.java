package edu.iris.epo.libdali.test;

import edu.iris.epo.libdali.DataLinkClient;
import edu.iris.epo.libdali.DataLinkConst;
import edu.iris.epo.libdali.DataLinkUtils;
import edu.iris.epo.libdali.IStreamid;
import edu.iris.epo.libdali.DataLinkClient.DL_RETVAL;

/**
 * DataLink Client Test
 * 
 * @author kevin
 */
public class DataLinkClientTest implements DataLinkConst, Runnable {
    public static void main(String[] args) {
        new DataLinkClientTest().run();
    }

    private boolean ack = true;
    private boolean collectFlag = true;
    private long dataend;
    private long datastart;
    private boolean endflag;
    private boolean getInfoFlag = false;
    private int maxpktsize;
    private byte[] packet;
    private int packetlen;
    private long read_pktid;
    private String rejectpattern;
    private String stream_matchpattern = "";
    // packet ID, 0 for after, DATALINK_POSITION_EARLIEST,
    // DATALINK_POSITION_LATEST
    private long stream_pktid = 0;
    private long stream_pkttime = 1;
    private IStreamid streamid;
    private boolean writeFlag = false;

    @Override
    public void run() {
        String s;
        String infotype = "STATUS";
        String infomatch = null;
        DataLinkClient dlc = new DataLinkClient(
                DataLinkClientTest.class.getSimpleName(), null);

        if (writeFlag) {
            packetlen = SLINKPACKETSIZE;
            packet = new byte[packetlen];
            datastart = System.currentTimeMillis() * 1000;
            dataend = datastart + 60 * 1000 * 1000;
            // IU_ANMO_00_BHZ/MSEED
            streamid = DataLinkUtils.createStreamid("IU", "ANMO", "00", "BHZ");
            for (int i = 0; i < packetlen; i++) {
                packet[i] = (byte) i;
            }
        }

        try {
            if (!dlc.connect()) {
                return;
            }

            if (dlc.exchangeIDs().isError()) {
                return;
            }
            if (!dlc.isWritePerm() && writeFlag) {
                System.err.println("Write is not permitted");
                writeFlag = false;
                packetlen = 0;
                packet = null;
            }
            maxpktsize = dlc.getMaxPktSize();

            if (getInfoFlag) {
                if (!dlc.getinfo(infotype, infomatch).isError()) {
                    s = dlc.getReadText();
                    System.out.printf("getinfo header:\n\"%s\" (%d)\n", s,
                            s.length());
                }
            }

            if (streamid != null && packet != null &&
                    packet.length >= packetlen && datastart > 0 &&
                    dataend > datastart) {
                if (packetlen > maxpktsize) {
                    System.err.printf(
                            "Cannot write packet, packet length %d is larger than maximum %d\n",
                            packetlen, maxpktsize);
                } else {
                    dlc.write(packet, packetlen, streamid, datastart, dataend,
                            ack);
                    if (ack) {
                        s = dlc.getReadText();
                        System.out.printf("write header:\n\"%s\" (%d)\n", s,
                                s.length());
                    }
                }
            }

            if (rejectpattern != null) {
                if (!dlc.reject(rejectpattern).isError()) {
                    s = dlc.getReadText();
                    System.out.printf("reject header:\n\"%s\" (%d)\n", s,
                            s.length());
                }
            }

            if (stream_matchpattern != null) {
                if (!dlc.match(stream_matchpattern).isError()) {
                    s = dlc.getReadText();
                    System.out.printf("match header:\n\"%s\" (%d)\n", s,
                            s.length());
                }
            }

            if (stream_pktid != 0 || stream_pkttime != 0) {
                if (stream_pktid == 0) {
                    if (!dlc.positionAfter(stream_pkttime).isError()) {
                        s = dlc.getReadText();
                        System.out.printf(
                                "positionAfter header:\n\"%s\" (%d)\n", s,
                                s.length());
                    }
                } else {
                    if (!dlc.position(stream_pktid, stream_pkttime).isError()) {
                        s = dlc.getReadText();
                        System.out.printf("position header:\n\"%s\" (%d)\n", s,
                                s.length());
                    }
                }
            }

            if (read_pktid != 0) {
                if (!dlc.read(read_pktid).isError()) {
                    System.out.printf("read: %s\n", dlc.getPacket());
                }
            }

            if (collectFlag) {
                DL_RETVAL retVal;
                // endflag = true;
                if (!(retVal = dlc.collect(endflag)).isError() &&
                        dlc.getPacket().getDatasize() != 0) {
                    endflag = true;
                    while ((!(retVal = dlc.collect(endflag)).isError() &&
                            dlc.getPacket().getDatasize() != 0)) {
                        if (!retVal.isError()) {
                            System.out.printf("read: %s\n", dlc.getPacket());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dlc.close();
        }
    }
}
