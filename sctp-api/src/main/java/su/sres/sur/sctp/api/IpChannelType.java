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

/**
 * @author amit bhayani
 * @author sergey vetyutnev
 * 
 */
public enum IpChannelType {
	SCTP(0, "SCTP"), TCP(1, "TCP");

	int code;
	String type;

	private IpChannelType(int code, String type) {
		this.code = code;
		this.type = type;

	}

	public int getCode() {
		return this.code;
	}

	public String getType() {
		return type;
	}

	public static IpChannelType getInstance(int code) {
		switch (code) {
		case 0:
			return IpChannelType.SCTP;
		case 1:
			return IpChannelType.TCP;
		}

		return null;
	}

	public static IpChannelType getInstance(String type) {
		if (type.equalsIgnoreCase("SCTP")) {
			return SCTP;
		} else if (type.equalsIgnoreCase("TCP")) {
			return TCP;
		}
		
		return null;
	}
}
