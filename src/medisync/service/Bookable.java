package medisync.service;

import medisync.exception.SlotNotAvailableException;
import medisync.model.Appointment;
import medisync.model.Patient;

// [Practical 4] Interface for booking slots
// [Practical 5] Packages: medisync.service
public interface Bookable {
    Appointment bookSlot(Patient p) throws SlotNotAvailableException;
}
