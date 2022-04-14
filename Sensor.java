import java.util.Random;

public class Sensor extends Thread {
  Report report;
  int time = 0;
  Sensor(Report report) {
    this.report = report;
  }

  public void run(){
    while(!report.done) {
      while(report.time < report.readingsPerReport) {
        while (!report.lock.get()) {
          addReading(report.time);
          report.lock.set(true);
        }
      }
    }
  }

  private boolean addReading(int start) {
    Random rand = new Random();
    int low = -100;
    int high = 71;
    int reading = rand.nextInt(high-low) + low;
    if (report.readings.add(reading, start)) {
      if (reading > report.max.get())
        report.max.getAndSet(reading);
      if (reading < report.min.get())
        report.min.getAndSet(reading);
      return true;
    }
    return false;
  }
}
