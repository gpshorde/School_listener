/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.traccar.model.Alert;
import org.traccar.model.Event;
import org.traccar.model.Position;

public abstract class BaseDataHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      //  System.out.println("org.traccar.BaseDataHandler.channelRead()");
        if (msg instanceof Position) {
            Position position = handlePosition((Position) msg);

        //  System.out.println("BaseDataHandler position-------------->"+position);
            if (position != null) {
           //    System.out.println("BaseDataHandler ---------------->if2");
                ctx.fireChannelRead(position);
            }
        } else {
//            System.out.println("BaseDataHandler ---------------->else");
            super.channelRead(ctx, msg);
        }
    }

   protected abstract Position handlePosition(Position position) throws JsonProcessingException;

//    protected abstract Position handlePosition(Position position, Event event);

    protected abstract Position handlePosition(Position position, Alert event) throws JsonProcessingException;
}
