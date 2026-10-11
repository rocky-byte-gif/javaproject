package grievance.service;

import grievance.exception.ComplaintNotFoundException;
import grievance.model.Complaint;

// [Practical 4] Interface definition
// [Practical 5] Packages: grievance.service
public interface Trackable {
    Complaint find(int id) throws ComplaintNotFoundException;
}
