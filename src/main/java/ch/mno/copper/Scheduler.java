package ch.mno.copper;

import it.sauronsoftware.cron4j.Predictor;

import java.util.Date;

/**
 * Created by dutoitc on 31.01.2016.
 */
public class Scheduler {

    /**
     *
     * @param pattern minutes(0-59) hours(0-23) day_of_month(1-31), month(1-12 or jan,feb...), days of week(0-6=sunday-saturday or sun mon tue...) e.g. 0 3 * jan-jun,sep-dec mon-fri
     *                @see {http://www.sauronsoftware.it/projects/cron4j/manual.php?PHPSESSID=ercrm156a8fd38ok2fe98mpmu0}
     */
    public Date nextMatchingDate(String pattern) {
        Predictor p = new Predictor(pattern);
        return p.nextMatchingDate();
    }

    /*
    Scheduler s = new Scheduler();
		// Schedule a once-a-minute task.
		s.schedule("* * * * *", new Runnable() {
			public void run() {
				System.out.println("Another minute ticked away...");
			}
		});
		// Starts the scheduler.
		s.start();

		String pattern = "0 3 * jan-jun,sep-dec mon-fri";
Predictor p = new Predictor(pattern);
for (int i = 0; i < n; i++) {
	System.out.println(p.nextMatchingDate());
}
     */
}
