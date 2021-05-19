
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
  public InputStream sin;
  public ObjectInputStream oin;
  public OutputStream oStream;
  public PrintWriter pw;
  private Object monitor = new Object();

  public Instructor(int id) {
	this.name = "Instructor-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	isAlive.add(this);
	this.id = id;
  }

  public void setStuff(Socket socket, ObjectInputStream oin) {

	this.oin = oin;
	try {
	  oStream = socket.getOutputStream();
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
	this.pw = new PrintWriter(new BufferedOutputStream(oStream));
  }

  public void start() {
	thread.start();
  }

  public void notifyMonitor() {
	synchronized (monitor) {
	  msg("Notifying the monitor");
	  monitor.notify();
	}
  }

  public void step1() {
	synchronized (monitor) {
	  try {
		randSleep();
		msg("has arrived to school and is ready to teach");
		monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
  }

  public void step2() {
	synchronized (monitor) {
	  try {
		msg("ready to teach class");
		monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}

	// step 2
	while (Principal.period.get() != 3) {
	  msg("period was " + Principal.period.get());
	  if (this.id == 0) {
		teachELA();
	  } else {
		teachMath();
	  }
	  randSleep();
	  if (interruptCheck()) {
		return;
	  }
	}
  }

  public void step3() {
	synchronized (monitor) {
	  try {
		msg("is ready to go home");
		monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	// step 3
	sendInstructorHome();
  }

  @Override
  public void run() {
	// TODO Auto-generated method stub
	// step 1
	step1();
	step2();
	step3();
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
	m = "[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m + ".";
	System.out.println(m);
	this.pw.println(m);
	this.pw.flush();
  }
}
