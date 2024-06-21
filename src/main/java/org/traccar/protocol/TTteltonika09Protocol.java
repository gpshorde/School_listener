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
public class TTteltonika09Protocol extends BaseProtocol{

    public TTteltonika09Protocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonika09FrameDecoder());
                pipeline.addLast(new TTteltonika09ProtocolEncoder());
                pipeline.addLast(new TTteltonika09ProtocolDecoder(TTteltonika09Protocol.this, false));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonika09ProtocolEncoder());
                pipeline.addLast(new TTteltonika09ProtocolDecoder(TTteltonika09Protocol.this, true));
            }
        });
    }
}
