package grievance.service;

import grievance.model.Complaint;

// [Practical 4] Interface definition
// [Practical 5] Packages: grievance.service
public interface Routable {
    String route(Complaint c);
}
