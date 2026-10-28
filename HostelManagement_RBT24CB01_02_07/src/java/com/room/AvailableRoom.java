package com.room;

import com.entity.Room;

public final class AvailableRoom {

    private final Room room;
    private final long activeAllocations;

    public AvailableRoom(Room room, long activeAllocations) {
        this.room = room;
        this.activeAllocations = activeAllocations;
    }

    public Room getRoom() {
        return room;
    }

    public long getActiveAllocations() {
        return activeAllocations;
    }

    public int getTrackedFreeCapacity() {
        if (room == null || room.getCapacity() < 1) {
            return 0;
        }
        long free = room.getCapacity() - activeAllocations;
        return free < 0 ? 0 : (int) free;
    }

    public boolean isAvailable() {
        return room != null
                && "AVAILABLE".equalsIgnoreCase(room.getStatus())
                && room.getCapacity() > 0
                && activeAllocations < room.getCapacity();
    }
}
