package ch.mno.copper.collect;

import it.sauronsoftware.cron4j.Predictor;

/**
 * Usage:
 * call shouldRun, then getRunnable, then markAsRun.
 * Created by dutoitc on 02.02.2016.
 */
public class CollectorTask {

    private Runnable runnable;
    private String cronExpression;
    private long lastRun=-1;
    private long nextRun;
    private static long nextId=1;
    private long taskId = nextId++;

    /**
     * pattern minutes(0-59) hours(0-23) day_of_month(1-31), month(1-12 or jan,feb...), days of week(0-6=sunday-saturday or sun mon tue...) e.g. 0 3 * jan-jun,sep-dec mon-fri
     *                @see {http://www.sauronsoftware.it/projects/cron4j/manual.php}
     * @param runnable
     * @param cronExpression
     */
    public CollectorTask(Runnable runnable, String cronExpression) {
        this.runnable = runnable;
        this.cronExpression = cronExpression;
        computeNextRun();
    }

    private void computeNextRun() {
        Predictor p = new Predictor(cronExpression);
        nextRun = p.nextMatchingTime();
        System.out.println("Task " + taskId + ": scheduled next run in " + computeTime(nextRun-System.currentTimeMillis()));
    }

    public long getTaskId() {
        return taskId;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public boolean shouldRun() {
        return System.currentTimeMillis()>=nextRun;
    }

    public void markAsRun() {
        lastRun = System.currentTimeMillis();
        computeNextRun();
    }

    private String computeTime(long millis) {
        if (millis<=0) return "(now)";

        StringBuilder sb = new StringBuilder();
        if (millis >= 1000*3600*24) {
            int days = (int)(millis/1000/3600/24);
            sb.append(days).append(" days");
            millis-=days*1000*3600*24;
        }
        if (millis>=1000*3600) {
            int hours = (int)(millis/1000/3600);
            if (sb.length()>0) sb.append(", ");
            sb.append(hours).append(" hours");
            millis-=hours*1000*3600;
        }
        if (millis>=1000*60) {
            int min = (int)(millis/1000/60);
            if (sb.length()>0) sb.append(", ");
            sb.append(min).append(" minutes");
            millis-=min*1000*3600;
        }
        if (millis>=1000) {
            int sec = (int)(millis/1000);
            if (sb.length()>0) sb.append(", ");
            sb.append(sec).append(" seconds");
            millis-=sec*1000;
        }
        return sb.toString();
    }

}
