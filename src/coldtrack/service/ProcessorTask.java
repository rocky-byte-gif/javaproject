package coldtrack.service;

import java.util.Vector;
import coldtrack.exception.InvalidReadingException;

// [Practical 5] Packages: coldtrack.service
// [Practical 8] Multithreading: implements Runnable
public class ProcessorTask implements Runnable {

    private ColdChainService service;
    private Vector<String> inbox;
    private boolean active;

    public ProcessorTask(ColdChainService service, Vector<String> inbox) {
        this.service = service;
        this.inbox = inbox;
        this.active = true;
    }

    public void stopTask() {
        this.active = false;
    }

    // [Practical 8] Runnable execution logic
    @Override
    public void run() {
        while (active) {
            try {
                // Process inbox messages every 1 second
                Thread.sleep(1000);

                Vector<String> batch = new Vector<String>();

                // [Practical 8] Synchronized extraction from inbox Vector
                synchronized (inbox) {
                    while (!inbox.isEmpty()) {
                        batch.addElement(inbox.remove(0));
                    }
                }

                // [Practical 1] Loop over messages
                for (int i = 0; i < batch.size(); i++) {
                    String msg = batch.elementAt(i);
                    try {
                        service.processMessage(msg);
                    } catch (InvalidReadingException e) {
                        // [Practical 7] Catch custom exception and increment bad reading counter
                        service.recordBadReading();
                        System.out.println("[Processor] Rejected invalid reading payload: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("[Processor] Error: " + e.getMessage());
                    }
                }

            } catch (InterruptedException e) {
                System.out.println("[Processor] Interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[Processor] Unexpected error: " + e.getMessage());
            }
        }
    }
}
