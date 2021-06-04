package project1;

public class AtomicMonitor {
  // a way to keep track of values without using the synchronized atomic class.
  public static Object atomicMonitor = new Object();
  public int value;

  // setter
  public AtomicMonitor(int val) {
	this.value = val;
  }

  public void set(int newval) {
	synchronized (atomicMonitor) {
	  this.value = newval;
	}
  }

  public int getAndIncrement() {
	synchronized (atomicMonitor) {
	  int oldvalue = value;
	  this.value++;
	  return oldvalue;
	}
  }

  public int incrementAndGet() {
	synchronized (atomicMonitor) {
	  return ++this.value;
	}
  }

  public int get() {
	synchronized (atomicMonitor) {
	  return value;
	}
  }
}
