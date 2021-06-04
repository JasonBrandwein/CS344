package project1;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

//Made my own AtomicMonitor class to satisfy the condition of monitor only no other synchronization tools.
//import java.util.concurrent.AtomicMonitor.AtomicInteger;

public class Student implements Runnable {
  private String name;
  public static long time = System.currentTimeMillis();
  public Thread thread;
  public int studentID;
  public int rand; // will be used more than once
  public static Vector<Student> pairs = new Vector<>();
  public static Vector<Student> isAlive = new Vector<>();
  public static Vector<Student> arrival = new Vector<>();
  public boolean interrupted = false;
  public static Vector<Student> ELA = new Vector<>(3);
  public static Object elaMonitor = new Object();
  public static AtomicMonitor ELACount = new AtomicMonitor(0);
  public static Vector<Student> math = new Vector<>(3);
  public static Object mathMonitor = new Object();
  public static AtomicMonitor mathCount = new AtomicMonitor(0);
  public static Vector<Student> physEd = new Vector<>();
  public static Object physEdMonitor = new Object();
  public static AtomicMonitor classCount = new AtomicMonitor(0);
  public static AtomicMonitor studentCount = new AtomicMonitor(0);
  public int lastTook = 0;
  // class Attendance. when and where when doing cleanup.
  public Vector<String> attendance = new Vector<>();
  // after class students need a place to take a break, this gives us time to
  public static Vector<Student> breakArea = new Vector<>();
  public static Object breakAreaMonitor = new Object();
  public boolean Questionare;
  public boolean ronaTest;
  public boolean hasTakenClass = false;
  public static AtomicMonitor count = new AtomicMonitor(0);

  public Student(int id) {
	studentID = id;
	this.name = "Student-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	rand = new Random().nextInt(100);
	// if rand > 15, then they filled out the questionare.
	Questionare = rand % 100 > 15;
	isAlive.add(this);
  }

  public void start() {
	thread.start();
  }

  @Override
  public void run() {

	randSleep();
	msg("woke up");
	randSleep();

	if (!schoolOpen()) {
	  msg("adding to count");
	  count.getAndIncrement();
	  arrival.add(this);
	  if (schoolOpen()) {
		synchronized (Principal.schoolStarting) {
		  msg("principal was notified");
		  Principal.schoolStarting.notify();
		}
	  }
	  msg("entering schoolyard");
	  enterSchoolyard();
	  if (interrupted) {
		return;
	  } else {
		msg("was let in by the principal");
	  }
	} else {
	  sendStudentHome("was late to school and was sent home early");
	  return;
	}
	randSleep();

	if (ronaTest && !interrupted) {
	  msg("is being sent to the nurse to be tested for rona");
	  waitForPair();
	} else if (ronaTest && !interrupted) {
	  // not sure what this part means anymore double check later.
	  msg("Ready to go inside");
	}
	randSleep();

	//the student could be send home at this point so we check.
	if (interrupted) {
	  //if the case is true, and the student should be sent home. You can get the thread out of the run method here.
	  return;
	} else {
	  msg("Was here");
	}

	while (Principal.period.get() != 3) {
	  goToClass();
	  // take a small break
	  randSleep();
	}

	if (Principal.period.get() == 3) {

	  sendStudentHome("went home at the end of the school day");
	}
	return;
  }

  public void wakePrincipalCheck() {

	if (classCount.get() == Main.numStudents) {
	  // we wake the principal to start teaching class.
	  msg("notifying principal to start class");
	  synchronized (Principal.startClass) {
		Principal.startClass.notify();
	  }
	}
  }

  public void goToClass() {
	classCount.incrementAndGet();
	hasTakenClass = true;
	// ela can't take ela again the next period
	// Okay to always increment. The first 4 spots should always fill up no matter
	// what.
	if (ELACount.getAndIncrement() < 4 && lastTook != 1) {
	  msg("is going to ELA");
	  lastTook = 1;
	  ELA.add(this);
	  wakePrincipalCheck();
	  elaSleep();
	  breakRoom();
	  // math can't take math again the next period It's okay if the incramenting
	  // always go because the first 4 slots should fill up. Any values past that
	  // we're okay with.
	} else if (mathCount.getAndIncrement() < 4 && lastTook != 2) {
	  lastTook = 2;
	  msg("is going to math");
	  math.add(this);
	  wakePrincipalCheck();
	  mathSleep();
	  breakRoom();
	} else {
	  lastTook = 3;
	  msg("is going to physed");
	  physEd.add(this);
	  wakePrincipalCheck();
	  physEdSleep();
	  breakRoom();
	}
  }

  public void breakRoom() {
	msg("is on break");
	breakArea.add(this);
	randSleep();
	if (Principal.period.get() != 3) {
	  synchronized (breakAreaMonitor) {
		while (true) {
		  try {
			breakAreaMonitor.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }
	}

	if (Principal.period.get() != 3)
	  msg("finished break and is going back to class");
  }

  public void elaSleep() {
	// the ELA teacher and students in the ELA classroom will wait on one object
	synchronized (elaMonitor) {
	  while (true) {
		try {
		  msg("is waiting for ELA to start");
		  elaMonitor.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}
	msg("woke up from ELA");
  }

  public void mathSleep() {
	// the MATH teacher and students in the MATH classroom will wait on a different

	synchronized (mathMonitor) {
	  while (true) {
		try {
		  msg("is waiting for math to start");
		  mathMonitor.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}
	msg("woke up from math");

  }

  public void physEdSleep() {
	// the students in the backyard will wait on another object

	synchronized (physEdMonitor) {
	  while (true) {
		try {
		  msg("is waiting for physEd to start");
		  physEdMonitor.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}
	msg("woke up from physED");

  }

  public void waitForPair() {
	// DEBUG printout
	// msg("is waiting for it's pair");
	pairs.add(this);

	synchronized (Nurse.nurseWaiting) {
	  msg("notified nurse");
	  Nurse.nurseWaiting.notify();
	  // student waits here.
	}

	synchronized (this) {
	  while (true) {
		try {
		  msg("is waiting for test results");
		  this.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}
  }

  public void printAttendance() {
	Iterator<String> it = attendance.iterator();
	int count = 1;
	while (it.hasNext()) {
	  msg("Attended " + it.next() + " at period " + count++);
	}
  }

  public void sendStudentHome(String message) {
	// we kill the thread if they forgot the questionare.
	msg("classCount = " + classCount.get());
	classCount.getAndIncrement();
	// a way to balance out.
	studentCount.getAndIncrement();
	msg(message);
	if (hasTakenClass) {
	  printAttendance();
	}

	this.interrupted = true;
	Thread.currentThread().interrupt();
	return;
  }

  private void enterSchoolyard() {
	synchronized (this) {
	  if (!schoolOpen()) {
		msg("is waiting for the principal");
		while (true) {
		  try {
			this.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }

	}
  }

  private static boolean schoolOpen() {
	// when most of the students arrive we let the students in. Get the students in
	// one
	// most is at around 85% of students arriving. So if... Count is >
	// Main.numStudents * .85?
	// debug printout
	// System.out.println("count = " + count + " main.num = " + Main.numStudents);
	return count.get() > Main.numStudents * .85;
  }

  public void gotoSleep(int time) {
	try {
	  Thread.sleep(time);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
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

  public void msg(String m) {
	System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m + ".");
  }
}
