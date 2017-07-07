/**
 * This file is part of PacketWrapper.
 * Copyright (C) 2012-2015 Kristian S. Strangeland
 * Copyright (C) 2015 dmulloy2
 *
 * PacketWrapper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PacketWrapper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PacketWrapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.volmit.wormholes.wrapper;

import java.util.UUID;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayClientSpectate18 extends AbstractPacket18 {
    public static final PacketType TYPE = PacketType.Play.Client.SPECTATE;
    
    public WrapperPlayClientSpectate18() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }
    
    public WrapperPlayClientSpectate18(PacketContainer packet) {
        super(packet, TYPE);
    }
    
    /**
     * Retrieve Target Player.
     * @return The current Target Player
     */
    public UUID getTargetPlayer() {
        return handle.getSpecificModifier(UUID.class).read(0);
    }
    
    /**
     * Set Target Player.
     * @param value - new value.
     */
    public void setTargetPlayer(UUID value) {
        handle.getSpecificModifier(UUID.class).write(0, value);
    }
    
}
