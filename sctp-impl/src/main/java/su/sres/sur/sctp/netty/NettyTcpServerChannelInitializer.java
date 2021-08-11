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
import io.netty.channel.socket.SocketChannel;

/**
 * @author <a href="mailto:amit.bhayani@telestax.com">Amit Bhayani</a>
 * @author Sergey Vetyutnev</a>
 * 
 */
public class NettyTcpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyServerImpl nettyServerImpl;
    private final NettySctpManagementImpl sctpManagementImpl;

    protected NettyTcpServerChannelInitializer(NettyServerImpl nettyServerImpl, NettySctpManagementImpl sctpManagementImpl) {
        super();
        this.nettyServerImpl = nettyServerImpl;
        this.sctpManagementImpl = sctpManagementImpl;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new NettySctpServerHandler(this.nettyServerImpl, this.sctpManagementImpl));
    }
}
