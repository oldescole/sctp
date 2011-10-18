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
package org.mobicents.protocols.sctp;

import java.util.Arrays;

/**
 * @author amit bhayani
 * 
 */
public class PayloadData {
	private final int dataLength;
	private final byte[] data;
	private final boolean complete;
	private final boolean unordered;
	private final int payloadProtocolId;
	private final int streamNumber;

	/**
	 * @param dataLength
	 * @param data
	 * @param complete
	 * @param unordered
	 * @param payloadProtocolId
	 * @param streamNumber
	 */
	public PayloadData(int dataLength, byte[] data, boolean complete, boolean unordered, int payloadProtocolId, int streamNumber) {
		super();
		this.dataLength = dataLength;
		this.data = data;
		this.complete = complete;
		this.unordered = unordered;
		this.payloadProtocolId = payloadProtocolId;
		this.streamNumber = streamNumber;
	}

	/**
	 * @return the dataLength
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return the unordered
	 */
	public boolean isUnordered() {
		return unordered;
	}

	/**
	 * @return the payloadProtocolId
	 */
	public int getPayloadProtocolId() {
		return payloadProtocolId;
	}

	/**
	 * @return the streamNumber
	 */
	public int getStreamNumber() {
		return streamNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PayloadData [dataLength=").append(dataLength).append(", data=").append(Arrays.toString(data)).append(", complete=").append(complete)
				.append(", unordered=").append(unordered).append(", payloadProtocolId=").append(payloadProtocolId).append(", streamNumber=")
				.append(streamNumber).append("]");
		return sb.toString();
	}

}
