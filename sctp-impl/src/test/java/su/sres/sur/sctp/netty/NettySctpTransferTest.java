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

package su.sres.sur.sctp.netty;

import static org.junit.Assert.assertTrue;
import io.netty.buffer.Unpooled;
import su.sres.sur.sctp.api.Association;
import su.sres.sur.sctp.api.AssociationListener;
import su.sres.sur.sctp.api.IpChannelType;
import su.sres.sur.sctp.api.PayloadData;
import su.sres.sur.sctp.netty.NettyAssociationImpl;
import su.sres.sur.sctp.netty.NettySctpManagementImpl;
import su.sres.sur.sctp.netty.NettyServerImpl;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.testng.annotations.*;

import com.sun.nio.sctp.SctpChannel;

/**
 * @author amit bhayani
 * 
 */
public class NettySctpTransferTest {
	private static final String SERVER_NAME = "testserver";
	private static final String SERVER_HOST = "127.0.0.1";
	private static final int SERVER_PORT = 22347;

	private static final String SERVER_ASSOCIATION_NAME = "serverAssociation";
	private static final String CLIENT_ASSOCIATION_NAME = "clientAssociation";

	private static final String CLIENT_HOST = "127.0.0.1";
	private static final int CLIENT_PORT = 22348;

	private final byte[] CLIENT_MESSAGE = "Client says Hi".getBytes();
	private final byte[] SERVER_MESSAGE = "Server says Hi".getBytes();

	private NettySctpManagementImpl management = null;

	// private Management managementClient = null;
	private NettyServerImpl server = null;

	private NettyAssociationImpl serverAssociation = null;
	private NettyAssociationImpl clientAssociation = null;

	private volatile boolean clientAssocUp = false;
	private volatile boolean serverAssocUp = false;

	private volatile boolean clientAssocDown = false;
	private volatile boolean serverAssocDown = false;

	private byte[] clientMessage;
	private byte[] serverMessage;
	
	private volatile int clientMaxInboundStreams = 0;
	private volatile int clientMaxOutboundStreams = 0;
	
	private volatile int serverMaxInboundStreams = 0;
	private volatile int serverMaxOutboundStreams = 0;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	public void setUp(IpChannelType ipChannelType) throws Exception {
	    this.clientMaxInboundStreams = 0;
		this.serverMaxOutboundStreams = 0;
		
		this.clientAssocUp = false;
		this.serverAssocUp = false;

		this.clientAssocDown = false;
		this.serverAssocDown = false;

		this.clientMessage = null;
		this.serverMessage = null;

		this.management = new NettySctpManagementImpl("netty-server-management");
//		this.management.setSingleThread(true);
		this.management.start();
        this.management.setConnectDelay(10000);// Try connecting every 10 secs
		this.management.removeAllResourses();

		this.server = (NettyServerImpl)this.management.addServer(SERVER_NAME, SERVER_HOST, SERVER_PORT, ipChannelType, false, 0, null);
		this.serverAssociation = (NettyAssociationImpl)this.management.addServerAssociation(CLIENT_HOST, CLIENT_PORT, SERVER_NAME, SERVER_ASSOCIATION_NAME, ipChannelType);
		this.clientAssociation = (NettyAssociationImpl)this.management.addAssociation(CLIENT_HOST, CLIENT_PORT, SERVER_HOST, SERVER_PORT, CLIENT_ASSOCIATION_NAME, ipChannelType,
				null);
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
	@Test(groups = { "functional", "sctp" })
	public void testDataTransferSctp() throws Exception {
		if (NettySctpTransferTest.checkSctpEnabled())
			this.testDataTransferByProtocol(IpChannelType.SCTP);
	}

	/**
	 * Simple test that creates Client and Server Association, exchanges data
	 * and brings down association. Finally removes the Associations and Server
	 */
	//TODO
    @Test(groups = { "functional", "tcp" })
    public void testDataTransferTcp() throws Exception {
        this.testDataTransferByProtocol(IpChannelType.TCP);
    }

	private void testDataTransferByProtocol(IpChannelType ipChannelType) throws Exception {

		this.setUp(ipChannelType);

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
		Thread.sleep(1000 * 1); // was: 40

		this.management.stopAssociation(SERVER_ASSOCIATION_NAME);

		Thread.sleep(1000);

		this.management.stopAssociation(CLIENT_ASSOCIATION_NAME);
		
		this.management.stopServer(SERVER_NAME);

		Thread.sleep(1000 * 2);

		assertTrue(Arrays.equals(SERVER_MESSAGE, clientMessage));
		assertTrue(Arrays.equals(CLIENT_MESSAGE, serverMessage));

		assertTrue(clientAssocUp);
		assertTrue(serverAssocUp);

		assertTrue(clientAssocDown);
		assertTrue(serverAssocDown);
		
		assertTrue(this.clientMaxInboundStreams> 0 );
		assertTrue(this.clientMaxOutboundStreams > 0);
		
		assertTrue(this.serverMaxInboundStreams> 0 );
		assertTrue(this.serverMaxOutboundStreams > 0);

		this.tearDown();
	}

	/**
	 * @return true if sctp is supported by this OS and false in not
	 */
	public static boolean checkSctpEnabled() {
		try {
			SctpChannel socketChannel = SctpChannel.open();
			socketChannel.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private class ClientAssociationListener implements AssociationListener {
		
		private final Logger logger = Logger.getLogger(ClientAssociationListener.class);

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * su.sres.sur.sctp.AssociationListener#onCommunicationUp
		 * (su.sres.sur.sctp.Association)
		 */
		@Override
		public void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams) {
			System.out.println(this + " onCommunicationUp");
			
			clientMaxInboundStreams = maxInboundStreams;
			clientMaxOutboundStreams = maxOutboundStreams;
			clientAssocUp = true;

			PayloadData payloadData = new PayloadData(CLIENT_MESSAGE.length, Unpooled.copiedBuffer(CLIENT_MESSAGE), true, false, 3, 1);

			try {
				association.send(payloadData);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			System.out.println(this + " onCommunicationShutdown");
			clientAssocDown = true;
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
			System.out.println(this + " onCommunicationLost");
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
			System.out.println(this + " onCommunicationRestart");
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
			clientMessage = new byte[payloadData.getDataLength()];
			payloadData.getByteBuf().readBytes(clientMessage);
			logger.debug("CLIENT received " + new String(clientMessage));
		}

		/* (non-Javadoc)
		 * @see su.sres.sur.sctp.api.AssociationListener#inValidStreamId(su.sres.sur.sctp.api.PayloadData)
		 */
		@Override
		public void inValidStreamId(PayloadData payloadData) {
			// TODO Auto-generated method stub
			
		}

	}

	private class ServerAssociationListener implements AssociationListener {

		private final Logger logger = Logger.getLogger(ServerAssociationListener.class);

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * su.sres.sur.sctp.AssociationListener#onCommunicationUp
		 * (su.sres.sur.sctp.Association)
		 */
		@Override
		public void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams) {
			System.out.println(this + " onCommunicationUp");

			serverAssocUp = true;
			serverMaxInboundStreams = maxInboundStreams;
			serverMaxOutboundStreams = maxOutboundStreams;
					

			PayloadData payloadData = new PayloadData(SERVER_MESSAGE.length, Unpooled.copiedBuffer(SERVER_MESSAGE), true, false, 3, 1);

			try {
				association.send(payloadData);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			System.out.println(this + " onCommunicationShutdown");
			serverAssocDown = true;
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
			System.out.println(this + " onCommunicationLost");
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
			System.out.println(this + " onCommunicationRestart");
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
			serverMessage = new byte[payloadData.getDataLength()];
			payloadData.getByteBuf().readBytes(serverMessage);
			logger.debug("SERVER received " + new String(serverMessage));
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
