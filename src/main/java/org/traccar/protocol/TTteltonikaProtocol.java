/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.TrackerServer;
import org.traccar.model.Command;

/**
 *
 * @author addon10
 */
public class TTteltonikaProtocol extends BaseProtocol{

    public TTteltonikaProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonikaFrameDecoder());
                pipeline.addLast(new TTteltonikaProtocolEncoder());
                pipeline.addLast(new TTteltonikaProtocolDecoder(TTteltonikaProtocol.this, false));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonikaProtocolEncoder());
                pipeline.addLast(new TTteltonikaProtocolDecoder(TTteltonikaProtocol.this, true));
            }
        });
    }
}
