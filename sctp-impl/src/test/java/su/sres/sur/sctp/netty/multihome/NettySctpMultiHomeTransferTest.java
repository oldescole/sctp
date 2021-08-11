/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package su.sres.sur.sctp.netty.multihome;

import static org.junit.Assert.assertTrue;
import io.netty.buffer.Unpooled;
import javolution.util.FastTable;
import su.sres.sur.sctp.SctpTransferTest;
import su.sres.sur.sctp.api.Association;
import su.sres.sur.sctp.api.AssociationListener;
import su.sres.sur.sctp.api.IpChannelType;
import su.sres.sur.sctp.api.PayloadData;
import su.sres.sur.sctp.netty.NettyAssociationImpl;
import su.sres.sur.sctp.netty.NettySctpManagementImpl;
import su.sres.sur.sctp.netty.NettyServerImpl;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <p>
 * This test is for SCTP Multihoming. Make sure you change SERVER_HOST1 and
 * CLIENT_HOST1 to match your current ip before you run this test.
 * <p>
 * <p>
 * Once this test is started you can randomly bring down loop back interface or
 * real interafce and see that traffic still continues.
 * </p>
 * <p>
 * This is not automated test. Please don't add in automation.
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class NettySctpMultiHomeTransferTest {
    private static final String SERVER_NAME = "testserver";
    private static final String SERVER_HOST = "127.0.0.1";
    private static final String SERVER_HOST1 = "10.2.50.194"; // "10.0.2.15"

    private static final int SERVER_PORT = 2350;

    private static final String SERVER_ASSOCIATION_NAME = "serverAssociation";
    private static final String CLIENT_ASSOCIATION_NAME = "clientAssociation";

    private static final String CLIENT_HOST = "127.0.0.1";
    private static final String CLIENT_HOST1 = "10.2.50.194"; // "10.0.2.15"

    private static final int CLIENT_PORT = 2351;

    private final String CLIENT_MESSAGE = "Client says Hi";
    private final String SERVER_MESSAGE = "Server says Hi";

    private NettySctpManagementImpl management = null;

    // private Management managementClient = null;
    private NettyServerImpl server = null;

    private NettyAssociationImpl serverAssociation = null;
    private NettyAssociationImpl clientAssociation = null;

    private volatile boolean clientAssocUp = false;
    private volatile boolean serverAssocUp = false;

    private volatile boolean clientAssocDown = false;
    private volatile boolean serverAssocDown = false;

    private FastTable<String> clientMessage = null;
    private FastTable<String> serverMessage = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public void setUp(IpChannelType ipChannelType) throws Exception {
        this.clientAssocUp = false;
        this.serverAssocUp = false;

        this.clientAssocDown = false;
        this.serverAssocDown = false;

        this.clientMessage = new FastTable<String>();
        this.serverMessage = new FastTable<String>();

        this.management = new NettySctpManagementImpl("server-management");
//        this.management.setSingleThread(true);
        this.management.start();
        this.management.setConnectDelay(10000);// Try connecting every 10 secs
        this.management.removeAllResourses();

        this.server = (NettyServerImpl) this.management.addServer(SERVER_NAME, SERVER_HOST, SERVER_PORT, ipChannelType, false,
                0, new String[] { SERVER_HOST1 });
        this.serverAssociation = (NettyAssociationImpl) this.management.addServerAssociation(CLIENT_HOST, CLIENT_PORT,
                SERVER_NAME, SERVER_ASSOCIATION_NAME, ipChannelType);
        this.clientAssociation = (NettyAssociationImpl) this.management.addAssociation(CLIENT_HOST, CLIENT_PORT, SERVER_HOST,
                SERVER_PORT, CLIENT_ASSOCIATION_NAME, ipChannelType, new String[] { CLIENT_HOST1 });
    }

    public void tearDown() throws Exception {

        this.management.removeAssociation(CLIENT_ASSOCIATION_NAME);
        this.management.removeAssociation(SERVER_ASSOCIATION_NAME);
        this.management.removeServer(SERVER_NAME);

        this.management.stop();
    }

    /**
     * Simple test that creates Client and Server Association, exchanges data
     * and brings down association. Finally removes the Associations and Server
     */
    @Test(groups = { "functional", "sctp-multihome" })
    public void testDataTransferSctp() throws Exception {

        // Testing only is sctp is enabled
        if (!SctpTransferTest.checkSctpEnabled())
            return;
        
        this.setUp(IpChannelType.SCTP);

        this.management.startServer(SERVER_NAME);

        this.serverAssociation.setAssociationListener(new ServerAssociationListener());
        this.management.startAssociation(SERVER_ASSOCIATION_NAME);

        this.clientAssociation.setAssociationListener(new ClientAssociationListener());
        this.management.startAssociation(CLIENT_ASSOCIATION_NAME);

        for (int i1 = 0; i1 < 40; i1++) {
            if (serverAssocUp)
                break;
            Thread.sleep(1000 * 5); // was: 40
        }
        Thread.sleep(1000 * 15000); // was: 40

        this.management.stopAssociation(CLIENT_ASSOCIATION_NAME);

        Thread.sleep(1000);

        this.management.stopAssociation(SERVER_ASSOCIATION_NAME);
        this.management.stopServer(SERVER_NAME);

        Thread.sleep(1000 * 2);

        // assertTrue(Arrays.equals(SERVER_MESSAGE, clientMessage));
        // assertTrue(Arrays.equals(CLIENT_MESSAGE, serverMessage));

        assertTrue(clientAssocUp);
        assertTrue(serverAssocUp);

        assertTrue(clientAssocDown);
        assertTrue(serverAssocDown);

        Runtime runtime = Runtime.getRuntime();

        this.tearDown();
    }

    private class ClientAssociationListener implements AssociationListener {
        private final Logger logger = Logger.getLogger(ClientAssociationListener.class);
        
        private LoadGenerator loadGenerator = null;

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationUp
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams) {
            logger.info(" onCommunicationUp");

            clientAssocUp = true;
            loadGenerator = new LoadGenerator(association, CLIENT_MESSAGE);
            (new Thread(loadGenerator)).start();

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationShutdown
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationShutdown(Association association) {
            logger.warn( " onCommunicationShutdown");
            clientAssocDown = true;
            loadGenerator.stop();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationLost
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationLost(Association association) {
            logger.warn(" onCommunicationLost");
            loadGenerator.stop();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationRestart
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationRestart(Association association) {
            logger.warn(" onCommunicationRestart");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onPayload(org.mobicents
         * .protocols.sctp.Association,
         * su.sres.sur.sctp.PayloadData)
         */
        @Override
        public void onPayload(Association association, PayloadData payloadData) {
            byte[] data = new byte[payloadData.getDataLength()];
            payloadData.getByteBuf().readBytes(data);
            String rxMssg = new String(data);
            logger.debug("CLIENT received " + rxMssg);
            clientMessage.add(rxMssg);

        }

        /* (non-Javadoc)
         * @see su.sres.sur.sctp.api.AssociationListener#inValidStreamId(su.sres.sur.sctp.api.PayloadData)
         */
        @Override
        public void inValidStreamId(PayloadData payloadData) {
            // TODO Auto-generated method stub
            
        }

    }

    private class LoadGenerator implements Runnable {

        private String message = null;
        private Association association;
        private volatile boolean started = true;

        LoadGenerator(Association association, String message) {
            this.association = association;
            this.message = message;
        }

        void stop() {
            this.started = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            for (int i = 0; i < 10000 && started; i++) {
                byte[] data = (this.message + i).getBytes();
                PayloadData payloadData = new PayloadData(data.length, Unpooled.copiedBuffer(data), true, false, 3, 1);

                try {
                    this.association.send(payloadData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class ServerAssociationListener implements AssociationListener {
        private final Logger logger = Logger.getLogger(ServerAssociationListener.class);
        private LoadGenerator loadGenerator = null;

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationUp
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams) {
            logger.info(" onCommunicationUp");

            serverAssocUp = true;

            loadGenerator = new LoadGenerator(association, SERVER_MESSAGE);
            (new Thread(loadGenerator)).start();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationShutdown
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationShutdown(Association association) {
            logger.warn(" onCommunicationShutdown");
            serverAssocDown = true;
            loadGenerator.stop();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationLost
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationLost(Association association) {
            logger.warn(" onCommunicationLost");
            loadGenerator.stop();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onCommunicationRestart
         * (su.sres.sur.sctp.Association)
         */
        @Override
        public void onCommunicationRestart(Association association) {
            logger.warn(" onCommunicationRestart");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * su.sres.sur.sctp.AssociationListener#onPayload(org.mobicents
         * .protocols.sctp.Association,
         * su.sres.sur.sctp.PayloadData)
         */
        @Override
        public void onPayload(Association association, PayloadData payloadData) {
            byte[] data = new byte[payloadData.getDataLength()];
            payloadData.getByteBuf().readBytes(data);
            String rxMssg = new String(data);
            logger.debug("SERVER received " + rxMssg);
            serverMessage.add(rxMssg);
        }

        /* (non-Javadoc)
         * @see su.sres.sur.sctp.api.AssociationListener#inValidStreamId(su.sres.sur.sctp.api.PayloadData)
         */
        @Override
        public void inValidStreamId(PayloadData payloadData) {
            // TODO Auto-generated method stub
            
        }

    }

}
