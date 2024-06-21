/*
 * Copyright 2015 - 2019 Anton Tananaev (anton@traccar.org)
 * Copyright 2016 Gabor Somogyi (gabor.g.somogyi@gmail.com)
 * Copyright 2017 Andrey Kunitsyn (andrey@traccar.org)
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
package org.traccar.api.resource;

import org.traccar.Context;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.database.CommandsManager;
import org.traccar.model.Command;
import org.traccar.model.Typed;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandResource extends ExtendedObjectResource<Command> {

    public CommandResource() {
        super(Command.class);
    }

    @GET
    @Path("send")
    public Collection<Command> get(@QueryParam("deviceId") long deviceId) {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        CommandsManager commandsManager = Context.getCommandsManager();
        Set<Long> result = new HashSet<>(commandsManager.getUserItems(getUserId()));
        result.retainAll(commandsManager.getSupportedCommands(deviceId));
//                     System.out.println("================send================" + commandsManager.getItems(result));
                     System.out.println("================all command we fire list ================" + result);


        return commandsManager.getItems(result);
    }

    @POST
    @Path("send")
    public Response send(Command entity) throws Exception {
        Context.getPermissionsManager().checkReadonly(getUserId());
        long deviceId = entity.getDeviceId();
        long id = entity.getId();

        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        if (id != 0) {
            Context.getPermissionsManager().checkPermission(Command.class, getUserId(), id);
            Context.getPermissionsManager().checkUserDeviceCommand(getUserId(), deviceId, id);
                         System.out.println("perticular command we fire with id  " + id );

        } else {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        }
        if (!Context.getCommandsManager().sendCommand(entity)) {
            return Response.accepted(entity).build();
        }
                     System.out.println("command " + Response.ok(entity).build());

        return Response.ok(entity).build();
    }

    @GET
    @Path("types")
    public Collection<Typed> get(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("protocol") String protocol,
            @QueryParam("textChannel") boolean textChannel) {
        if (deviceId != 0) {
            Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
             System.out.println("helloooo this is device id " +"=="+ deviceId);
           System.out.println("helloooo this is protocol" +"=="+ protocol);
  System.out.println("helloooo this is textChannel" +   "==" + textChannel);

            return Context.getCommandsManager().getCommandTypes(deviceId, textChannel);
        } else if (protocol != null) {
            return Context.getCommandsManager().getCommandTypes(protocol, textChannel);
        } else {

            return Context.getCommandsManager().getAllCommandTypes();
        }
    }
}
