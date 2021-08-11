/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package su.sres.sur.sctp.api;

import java.util.List;

/**
 * A wrapper over actual server side Socket
 * 
 * @author amit bhayani
 * 
 */
public interface Server {

	/**
	 * Get the Server channel type - TCP or SCTP
	 * 
	 * @return
	 */
	public IpChannelType getIpChannelType();

	/**
	 * Return if this Server accepts Anonymous connections 
	 * 
	 * @return
	 */
	public boolean isAcceptAnonymousConnections();

	/**
	 * Return the count of concurrent connections that can accept a Server. 0 means an unlimited count.  
	 * 
	 * @return
	 */
	public int getMaxConcurrentConnectionsCount();

	/**
	 * Set the count of concurrent connections that can accept a Server. 0 means an unlimited count.  
	 * 
	 * @return
	 */
	public void setMaxConcurrentConnectionsCount(int val);

	/**
	 * Get name of this Server. Should be unique in a management instance
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * The host address that this server socket is bound to
	 * 
	 * @return
	 */
	public String getHostAddress();

	/**
	 * The host port that this server socket is bound to
	 * 
	 * @return
	 */
	public int getHostport();
	
	/**
	 * When SCTP multi-homing configuration extra IP addresses are here
	 * 
	 * @return
	 */
	public String[] getExtraHostAddresses();

	/**
	 * If the server is started
	 * 
	 * @return
	 */
	public boolean isStarted();

	/**
	 * {@link Association} configured for this Server
	 * Anonymous associations are not present in this list
	 * 
	 * @return
	 */
	public List<String> getAssociations();

	/**
	 * Returns an unmodifiable list of anonymous associations that are connected at the moment 
	 * @return
	 */
	public List<Association> getAnonymAssociations();

}
