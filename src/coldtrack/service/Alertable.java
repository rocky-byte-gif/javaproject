package coldtrack.service;

import coldtrack.model.Truck;

// [Practical 4] Interface definition for raising alerts
// [Practical 5] Packages: coldtrack.service
public interface Alertable {
    void raiseAlert(Truck t, String msg);
}
