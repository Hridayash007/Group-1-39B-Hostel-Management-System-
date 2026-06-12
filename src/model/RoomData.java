package model;

public class RoomData {
    private int    roomId;
    private String roomNumber;
    private String block;
    private String floor;
    private String type;
    private int    capacity;
    private int    occupied;
    private String facilities;
    private String status;
    private double fee;

    // Allocated student info (populated via JOIN in allocation queries)
    private int    allocatedUserId;
    private String allocatedUsername;

    public RoomData() {}

    // Getters / setters
    public int    getRoomId()                        { return roomId; }
    public void   setRoomId(int id)                  { this.roomId = id; }

    public String getRoomNumber()                    { return roomNumber; }
    public void   setRoomNumber(String r)            { this.roomNumber = r; }

    public String getBlock()                         { return block; }
    public void   setBlock(String b)                 { this.block = b; }

    public String getFloor()                         { return floor; }
    public void   setFloor(String f)                 { this.floor = f; }

    public String getType()                          { return type; }
    public void   setType(String t)                  { this.type = t; }

    public int    getCapacity()                      { return capacity; }
    public void   setCapacity(int c)                 { this.capacity = c; }

    public int    getOccupied()                      { return occupied; }
    public void   setOccupied(int o)                 { this.occupied = o; }

    public String getFacilities()                    { return facilities; }
    public void   setFacilities(String f)            { this.facilities = f; }

    public String getStatus()                        { return status; }
    public void   setStatus(String s)                { this.status = s; }

    public int    getAllocatedUserId()               { return allocatedUserId; }
    public void   setAllocatedUserId(int id)        { this.allocatedUserId = id; }

    public String getAllocatedUsername()             { return allocatedUsername; }
    public void   setAllocatedUsername(String u)    { this.allocatedUsername = u; }
    
    public double getFee() {return fee;}
    public void setFee(double fee) {this.fee = fee;}
        /** Returns occupancy string like "1/2" */
    public String getOccupancyString()               { return occupied + "/" + capacity; }
}
