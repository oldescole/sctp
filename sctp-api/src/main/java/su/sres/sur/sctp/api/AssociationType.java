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
 * 
 */
public enum AssociationType {
	CLIENT("CLIENT"), SERVER("SERVER"), ANONYMOUS_SERVER("ANONYMOUS_SERVER");

	private final String type;

	private AssociationType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public static AssociationType getAssociationType(String type) {
		if (type == null) {
			return null;
		} else if (type.equalsIgnoreCase(CLIENT.getType())) {
			return CLIENT;
		} else if (type.equalsIgnoreCase(SERVER.getType())) {
			return SERVER;
		} else if (type.equalsIgnoreCase(ANONYMOUS_SERVER.getType())) {
			return ANONYMOUS_SERVER;
		} else {
			return null;
		}
	}
	
}
