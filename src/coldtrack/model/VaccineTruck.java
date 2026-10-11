package coldtrack.model;

// [Practical 4] Subclass overriding checkLimit()
// [Practical 5] Packages: coldtrack.model
public class VaccineTruck extends Truck {

    // [Practical 6] super() constructor call
    public VaccineTruck(int id, String name, double minTemp, double maxTemp) {
        super(id, name, "vaccine", minTemp, maxTemp);
    }

    // [Practical 4] Overriding checkLimit with tolerance = 1.0 degree
    @Override
    public String checkLimit(double temp) {
        double min = getMinTemp();
        double max = getMaxTemp();
        double tol = 1.0;

        if (temp >= min && temp <= max) {
            return "OK";
        } else if ((temp >= min - tol && temp < min) || (temp > max && temp <= max + tol)) {
            return "WARNING";
        } else {
            return "DANGER";
        }
    }
}
