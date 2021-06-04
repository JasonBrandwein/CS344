package project1;

import java.util.Random;
import java.util.Vector;

public class Instructor implements Runnable {
  private String name;
  public static long time = System.currentTimeMillis();
  public Thread thread;
  public int rand;
  public static Vector<Instructor> isAlive = new Vector<>();
  public int id;
  public static Object instructorMathMonitor = new Object();
  public static Object instructorElaMonitor = new Object();
  public boolean interruptFlag = false;

  public Instructor(int id) {
	this.name = "Instructor-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	isAlive.add(this);
	this.id = id;
  }

  public void start() {
	thread.start();
  }

  @Override
  public void run() {
	// TODO Auto-generated method stub
	randSleep();
	msg("has arrived to school");
	// msg("my id = " + this.id);

	// period is double because there's two teachers.
	while (Principal.period.get() != 3) {
	  msg("period was " + Principal.period.get());
	  if (this.id == 0) {
		teachELA();
	  } else {
		teachMath();
	  }
	  randSleep();
	  if (interruptCheck()) {
		//Since thread.join only updates a variable and doesn't kill the thread. An easy way to get a thread to end is by getting it out/to the end of the run method.
		return;
	  }
	}
	sendInstructorHome();
	return;
  }

  public boolean interruptCheck() {
	if (interruptFlag)
	  return true;
	return false;
  }

  public void sendInstructorHome() {
	Thread.currentThread().interrupt();
	interruptFlag = true;
	msg("has gone home");
  }

  private void waitOnEla() {
	synchronized (instructorElaMonitor) {
	  synchronized (Student.elaMonitor) {
		while (true) {
		  try {
			instructorElaMonitor.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }
	}
	// msg("woke up");
  }

  private void waitOnMath() {
	synchronized (instructorMathMonitor) {
	  synchronized (Student.mathMonitor) {
		while (true) {
		  try {
			instructorMathMonitor.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }
	}
	// msg("woke up");
  }

  private void teachELA() {
	msg("is waiting for class to start");
	waitOnEla();
	// Thread.currentThread().interrupt()
	// we might have to do isInterrupted(
	if (interruptCheck()) {
	  return;
	}

	msg("class has started");
	waitOnEla();
	msg("class has ended");
	msg("waking ela students");
	wakeEla();
  }

  private void teachMath() {
	msg("is waiting for class to start");
	waitOnMath();
	if (interruptCheck()) {
	  return;
	}
	msg("class has started");
	waitOnMath();
	msg("class has ended");
	msg("waking math students");
	wakeMath();
  }

  private void randSleep() {
	try {
	  Thread.sleep(rand = new Random().nextInt(100) + 100);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public void wakeEla() {
	for (int i = 0; i < 4; i++) {
	  synchronized (Student.elaMonitor) {
		Student.elaMonitor.notify();
	  }
	}

  }

  public void wakeMath() {
	for (int i = 0; i < 4; i++) {
	  synchronized (Student.mathMonitor) {
		Student.mathMonitor.notify();
	  }
	}
  }

  public void sendInstructorHome(String message) {
	msg(message);
	Thread.currentThread().interrupt();
	interruptFlag = true;
	// we can make a flag incase the interrupt doesn't work.
	return;
  }

  public String getName() {
	return this.name;
  }

  public void msg(String m) {
	System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m + ".");
  }
}
