
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
//Made my own AtomicMonitor class to satisfy the condition of monitor only no other synchronization tools.
//import java.util.concurrent.atomic.AtomicInteger;

public class Nurse implements Runnable {
  public static long time = System.currentTimeMillis();
  private String name;
  public Thread thread;
  public int rand;
  public static Object nurseWaiting = new Object();
  public static Vector<Nurse> isAlive = new Vector<>();
  public static int testingCount = 0;
  public static Boolean goHome = false;
  public static AtomicMonitor sickCount = new AtomicMonitor(0);
  // for interaction
  public InputStream sin;
  public ObjectInputStream oin;
  public OutputStream oStream;
  public PrintWriter pw;
  public Object monitor1 = new Object();

  public Nurse(int id) {
	this.name = "Nurse-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	isAlive.add(this);
  }

  public void start() {
	thread.start();
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

  @Override
  public void run() {
	randSleep();
	nurseArrive();
  }

  public void nurseArrive() {
	randSleep();
	synchronized (monitor1) {
	  try {
		// wait for the next command to be sent by the client to continue the process.
		// msg("Waiting for next command");
		msg("has arrived to school and is waiting on students to be tested");
		monitor1.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	// debug
	msg("has escaped the monitor and is goign to the office");
  }

  public void notifyMonitor() {
	synchronized (monitor1) {
	  // notify the monitor to continue with its process
	  monitor1.notify();
	}
	msg("is going to the office to test students");
	goToOffice();
  }

  public void nurseWait() {
	synchronized (nurseWaiting) {
	  msg("Waiting for students to be tested");
	  while (true) {
		try {
		  nurseWaiting.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}
  }

  public Boolean testForRona() {
	rand = new Random().nextInt(100);
	if (rand < 3) {
	  return true;
	}

	return false;
  }

  public Boolean singleTest() {
	msg("doing single test");
	if (testingCount < 2) {
	  testingCount++;
	  if (testForRona()) {
		sickCount.getAndIncrement();
		testingCount--;
		return true;
	  } else {
		return false;
	  }
	}
	return false;
  }

  public void doubleTest() {
	msg("doing double test");
	// check to see if there's more than one student being tested. Otherwise we
	if (testingCount >= 0 && testingCount < 2) {
	  testingCount += 2;
	  synchronized (Student.pairs.get(1)) {

		if (testForRona()) {
		  sickCount.getAndIncrement();
		  // send that student home.
		  Student.pairs.get(1).sendStudentHome("Has tested positive for Covid and is being sent home");
		  killSwitchCheck();
		} else {
		  Student.pairs.get(1).msg("Has tested negative for Covid and is being sent back to class");
		}
		Student.pairs.get(1).notify();

	  }

	  Student.pairs.remove(1);

	  synchronized (Student.pairs.get(0)) {

		if (testForRona()) {
		  // send that student home.
		  Student.pairs.get(0).sendStudentHome("Has tested positive for Covid and is being sent home");
		  // check if 3 or more are sent home for covid.
		  killSwitchCheck();
		} else {
		  Student.pairs.get(0).msg("Has tested negative for Covid and is being sent back to class");
		}
		Student.pairs.get(0).notify();

	  }

	  Student.pairs.remove(0);

	  testingCount -= 2;
	}

  }

  public void testStudents() {
	while (!Nurse.goHome) {

	  if (Student.pairs.isEmpty()) {
		nurseWait();
	  }
	  if(Nurse.goHome) {
		sendHome("has tested everyone, and is going home");
	  }

	  boolean flag = false;
	  if (Student.pairs.size() > 1) {
		doubleTest();
	  }
	  // single test
	  else if (Student.pairs.size() > 0) {
		flag = true;
		synchronized (Student.pairs.get(0)) {

		  if (singleTest()) {
			Student.pairs.get(0).sendStudentHome("Has tested positive for Covid and is being sent home");
			// sickCount.getAndIncrement();
			killSwitchCheck();

			// remove that one student and send him home
		  } else {
			Student.pairs.get(0).msg("Has tested negative for Covid and is being sent back to class");
		  }
		  Student.pairs.get(0).notify();
		}

		if (flag) {
		  // msg("flag proper triggered");
		  Student.pairs.remove(0);
		}
	  }
	}
  }

  public void sendHome(String message) {
	msg(message);
	Thread.currentThread().interrupt();
  }

  public void killSwitchCheck() {

	msg("sickcount = " + sickCount.get());
	if (sickCount.get() == 3) {
	  // 3 or more people have covid and must shut down everything.
	  msg("is sending all the students home");
	  for (Student s : Student.isAlive) {
		if (!s.interrupted)
		  s.sendStudentHome("was sent home because 3 or more people had rona");
	  }
	  msg("is sending all the Instructors home");

	  int teachercount = 0;
	  for (Instructor i : Instructor.isAlive) {

		i.sendInstructorHome("was sent home because 3 or more people had rona");

		if (teachercount++ == 0) {
		  msg("instruct0");
		  synchronized (Instructor.instructorElaMonitor) {
			Instructor.instructorElaMonitor.notify();
		  }
		} else {
		  msg("instruct1");
		  synchronized (Instructor.instructorMathMonitor) {
			Instructor.instructorMathMonitor.notify();
		  }
		}
	  }
	  // we need to notify the principal to go home. Just doing sendhome isn't enough.
	  msg("is sending the principal home");
	  for (Principal p : Principal.isAlive) {
		p.sendPrincipalHome("was sent home because 3 or more people had rona");

		synchronized (Principal.startClass) {
		  Principal.startClass.notify();
		}
	  }

	  msg("Has finished sending everyone home and is going home");
	  sendHome("has gone home because 3 or more people had Covid.");
	  goHome = true;
	} else {
	  msg("was elsed");
	}
  }

  public void goToOffice() {
	msg("Nurse went to the office");
	synchronized(monitor1) {
		  try {
			monitor1.wait();
		  } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	}
	msg("Starting to test students");
	testStudents();
  }
  public void wakeOffice() {
	synchronized (monitor1) {
	  // notify the monitor to continue with its process
	  monitor1.notify();
	}
  }
  private void randSleep() {
	try {
	  Thread.sleep(rand = new Random().nextInt(100) + 100);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public String getName() {
	return this.name;
  }

  // will have to set the other classes to do this.
  public void msg(String m) {
	m = "[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m + ".";
	System.out.println(m);
	this.pw.println(m);
	this.pw.flush();
  }
}