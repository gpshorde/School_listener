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
public class TTteltonika920sProtocol extends BaseProtocol{

    public TTteltonika920sProtocol() {
        setSupportedDataCommands(
                Command.TYPE_CUSTOM);
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonika920sFrameDecoder());
                pipeline.addLast(new TTteltonika920sProtocolEncoder());
                pipeline.addLast(new TTteltonika920sProtocolDecoder(TTteltonika920sProtocol.this, false));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast(new TTteltonika920sProtocolEncoder());
                pipeline.addLast(new TTteltonika920sProtocolDecoder(TTteltonika920sProtocol.this, true));
            }
        });
    }
}
