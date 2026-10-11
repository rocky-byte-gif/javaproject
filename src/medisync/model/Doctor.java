package medisync.model;

import medisync.exception.SlotNotAvailableException;
import medisync.service.Bookable;
import medisync.service.Reportable;

// [Practical 4] Multiple interface inheritance (implements Bookable, Reportable)
// [Practical 5] Packages: medisync.model
public class Doctor extends Person implements Bookable, Reportable {

    // [Practical 6] final static constant
    public static final int SLOTS_PER_DOCTOR = 6;

    private int id;
    private String specialization;

    // [Practical 2] Arrays for slot booking management
    private boolean[] booked;
    private String[] slotTimes = {"10:00", "10:30", "11:00", "11:30", "12:00", "12:30"};

    // [Practical 3] Parameterized constructor
    // [Practical 6] super() call
    public Doctor(int id, String name, String specialization) {
        super(name, 45, "9876500000");
        this.id = id;
        this.specialization = specialization != null ? specialization : "General";
        this.booked = new boolean[SLOTS_PER_DOCTOR];
    }

    // [Practical 4] Overriding getRole from Person
    @Override
    public String getRole() {
        return "Doctor";
    }

    // [Practical 4] Overloaded method 1: book first free slot
    // [Practical 8] Thread safety: synchronized method
    @Override
    public synchronized Appointment bookSlot(Patient p) throws SlotNotAvailableException {
        // [Practical 1] Loop over slots
        for (int i = 0; i < SLOTS_PER_DOCTOR; i++) {
            if (!booked[i]) {
                booked[i] = true;
                return new Appointment(0, p.getId(), this.id, slotTimes[i], "BOOKED");
            }
        }
        // [Practical 7] Throw custom exception if no slot is free
        throw new SlotNotAvailableException("Doctor " + getName() + " has all slots booked.");
    }

    // [Practical 4] Overloaded method 2: book specific slot index
    // [Practical 8] Thread safety: synchronized method
    public synchronized Appointment bookSlot(Patient p, int slotNo) throws SlotNotAvailableException {
        if (slotNo < 0 || slotNo >= SLOTS_PER_DOCTOR) {
            throw new SlotNotAvailableException("Invalid slot number requested: " + slotNo);
        }
        if (booked[slotNo]) {
            throw new SlotNotAvailableException("Slot " + slotTimes[slotNo] + " is already booked for " + getName());
        }
        booked[slotNo] = true;
        return new Appointment(0, p.getId(), this.id, slotTimes[slotNo], "BOOKED");
    }

    // [Practical 8] Free slot upon appointment cancellation
    public synchronized void freeSlot(int slotNo) {
        if (slotNo >= 0 && slotNo < SLOTS_PER_DOCTOR) {
            booked[slotNo] = false;
        }
    }

    // Free slot by slot time string
    public synchronized void freeSlotByTime(String time) {
        if (time == null) return;
        for (int i = 0; i < SLOTS_PER_DOCTOR; i++) {
            // [Practical 9] String method: equalsIgnoreCase
            if (slotTimes[i].equalsIgnoreCase(time.trim())) {
                booked[i] = false;
                break;
            }
        }
    }

    // Mark slot as booked (used when loading appointments from database)
    public synchronized void markSlotBookedByTime(String time) {
        if (time == null) return;
        for (int i = 0; i < SLOTS_PER_DOCTOR; i++) {
            if (slotTimes[i].equalsIgnoreCase(time.trim())) {
                booked[i] = true;
                break;
            }
        }
    }

    // Get count of free slots
    public synchronized int getFreeCount() {
        int count = 0;
        for (int i = 0; i < SLOTS_PER_DOCTOR; i++) {
            if (!booked[i]) {
                count++;
            }
        }
        return count;
    }

    // [Practical 4] Overriding getSummary from Reportable interface
    @Override
    public String getSummary() {
        return getName() + " (" + specialization + "): " + getFreeCount() + " of " + SLOTS_PER_DOCTOR + " slots free";
    }

    public int getId() {
        return id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public boolean[] getBooked() {
        return booked;
    }

    public String[] getSlotTimes() {
        return slotTimes;
    }
}
