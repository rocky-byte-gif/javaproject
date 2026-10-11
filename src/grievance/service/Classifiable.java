package grievance.service;

// [Practical 4] Interface definition
// [Practical 5] Packages: grievance.service
public interface Classifiable {
    String classify(String text);
    int scorePriority(String text);
}
