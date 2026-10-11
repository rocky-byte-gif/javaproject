package grievance.service;

import java.util.Vector;
import grievance.exception.ComplaintNotFoundException;
import grievance.exception.InvalidComplaintException;
import grievance.model.Complaint;
import grievance.model.Department;
import grievance.model.ElectricityDept;
import grievance.model.GeneralDept;
import grievance.model.RoadDept;
import grievance.model.SanitationDept;
import grievance.model.WaterDept;

// [Practical 4] Multiple inheritance via interfaces (implements Classifiable, Routable, Trackable)
// [Practical 5] Packages: grievance.service
public class ComplaintEngine implements Classifiable, Routable, Trackable {

    // [Practical 2] 2D Array: KEYWORDS for category matching (one row per category)
    private static final String[][] KEYWORDS = {
        {"WATER", "water", "pipe", "leak", "drain", "sewage", "drinking", "tap", "pipeline"},
        {"ROAD", "road", "pothole", "tar", "street", "traffic", "accident", "divider", "footpath"},
        {"SANITATION", "garbage", "trash", "waste", "bin", "dump", "stench", "cleaning", "sweep"},
        {"ELECTRICITY", "power", "electric", "current", "wire", "pole", "light", "transformer", "blackout"}
    };

    // [Practical 2] Vector<Complaint> as the in-memory queue
    private Vector<Complaint> queue;

    public ComplaintEngine() {
        this.queue = new Vector<Complaint>();
    }

    public Vector<Complaint> getQueue() {
        return queue;
    }

    // [Practical 4] Overloaded method 1: register with single text argument
    public Complaint register(String text) throws InvalidComplaintException {
        return register("Anonymous", text, "Ward 1");
    }

    // [Practical 4] Overloaded method 2: register with name, text, and ward
    public Complaint register(String name, String text, String ward) throws InvalidComplaintException {
        // [Practical 9] String method: trim and length
        if (text == null || text.trim().length() < 15) {
            // [Practical 7] Throw custom exception if text is empty or fewer than 15 characters
            throw new InvalidComplaintException("Complaint text must be at least 15 characters long.");
        }

        // [Practical 6] static counter increment
        Complaint.counter++;
        int newId = Complaint.counter;

        String category = classify(text);
        int priority = scorePriority(text);

        Complaint c = new Complaint(newId, name, text, ward, category, priority, "OPEN");

        // [Practical 2] Urgent complaints (priority >= 7) inserted at the front
        synchronized (queue) {
            if (c.getPriority() >= 7) {
                queue.insertElementAt(c, 0);
            } else {
                queue.addElement(c);
            }
        }

        return c;
    }

    // [Practical 4] Overriding classify from Classifiable interface
    @Override
    public String classify(String text) {
        if (text == null) {
            return "GENERAL";
        }

        // [Practical 9] String method: toLowerCase
        String lower = text.toLowerCase();

        // [Practical 1] Control flow: nested loops and if-else
        for (int i = 0; i < KEYWORDS.length; i++) {
            String cat = KEYWORDS[i][0];
            for (int j = 1; j < KEYWORDS[i].length; j++) {
                String kw = KEYWORDS[i][j];
                // [Practical 9] String methods: contains, indexOf
                if (lower.contains(kw) || lower.indexOf(kw) != -1) {
                    return cat;
                }
            }
        }
        return "GENERAL";
    }

    // [Practical 4] Overriding scorePriority from Classifiable interface
    @Override
    public int scorePriority(String text) {
        if (text == null) {
            return 3;
        }

        String lower = text.toLowerCase();
        double baseScore = 3.0;
        double multiplier = 1.0;

        // Check for urgency keywords
        if (lower.contains("urgent") || lower.contains("danger") || lower.contains("emergency") || lower.contains("spark") || lower.contains("hazard")) {
            multiplier = 1.8;
            baseScore += 3.0;
        } else if (lower.contains("broken") || lower.contains("burst") || lower.contains("severe") || lower.contains("overflow")) {
            multiplier = 1.4;
            baseScore += 2.0;
        }

        // Length-based consideration
        if (text.length() > 80) {
            baseScore += 1.0;
        }

        // [Practical 1] Type casting: double to int
        int score = (int) (baseScore * multiplier);

        // Clamp priority between 1 and 10
        if (score > 10) {
            score = 10;
        }
        if (score < 1) {
            score = 1;
        }
        return score;
    }

    // [Practical 4] Overriding route from Routable interface
    @Override
    public String route(Complaint c) {
        Department dept;
        String cat = c.getCategory();

        // [Practical 1] Switch statement: category to department
        switch (cat) {
            case "WATER":
                dept = new WaterDept();
                break;
            case "ROAD":
                dept = new RoadDept();
                break;
            case "SANITATION":
                dept = new SanitationDept();
                break;
            case "ELECTRICITY":
                dept = new ElectricityDept();
                break;
            default:
                dept = new GeneralDept();
                break;
        }

        // Polymorphic call to overridden handle() method
        return dept.handle(c);
    }

    // [Practical 4] Overriding find from Trackable interface
    @Override
    public Complaint find(int id) throws ComplaintNotFoundException {
        synchronized (queue) {
            // [Practical 1] Loop over queue elements
            for (int i = 0; i < queue.size(); i++) {
                Complaint c = queue.elementAt(i);
                if (c.getId() == id) {
                    return c;
                }
            }
        }
        // [Practical 7] Throw custom exception if ticket not found
        throw new ComplaintNotFoundException("Ticket ID #" + id + " does not exist in the active queue.");
    }
}
