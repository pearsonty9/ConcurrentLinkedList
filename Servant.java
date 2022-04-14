import java.util.Random;

public class Servant extends Thread {
  Party party;
  int counter = 0;
  Servant(Party party) {
    this.party = party;
  }

  public void run() {
    while(!party.done) {
      if (counter % 3 == 0) {
        int index = party.counter.getAndIncrement();
        if (index < party.size) {
          if (!party.list.add(index)) {
            party.counter.decrementAndGet();
          }
        }
      }
      else if (counter % 3 == 1){
        if (party.removed.get() < party.size && party.list.removeFirst());
          party.removed.getAndIncrement();
      }
      else {
        Random rand = new Random();
        int check = rand.nextInt(500000);
        party.list.contains(check);
      }
      counter++;
    }
  }
}
