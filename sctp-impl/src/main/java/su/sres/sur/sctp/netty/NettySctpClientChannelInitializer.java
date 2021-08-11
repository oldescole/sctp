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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.sctp.SctpChannel;
import io.netty.handler.codec.sctp.SctpMessageCompletionHandler;

/**
 * @author <a href="mailto:amit.bhayani@telestax.com">Amit Bhayani</a>
 * 
 */
public class NettySctpClientChannelInitializer extends ChannelInitializer<SctpChannel> {

    private final NettyAssociationImpl nettyAssociationImpl;

    /**
     * @param nettyAssociationImpl
     */
    public NettySctpClientChannelInitializer(NettyAssociationImpl nettyAssociationImpl) {
        super();
        this.nettyAssociationImpl = nettyAssociationImpl;
    }

    @Override
    protected void initChannel(SctpChannel ch) throws Exception {
        ch.pipeline().addLast(new SctpMessageCompletionHandler(), new NettySctpClientHandler(this.nettyAssociationImpl));

    }
}
