
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

public class Principal implements Runnable {
  public static Vector<Principal> isAlive = new Vector<>();
  private String name;
  public static long time = System.currentTimeMillis();
  public Thread thread;
  public int rand; // will be used more than once

  public static AtomicMonitor period = new AtomicMonitor(0);

  public static Object schoolStarting = new Object();
  public static Object startClass = new Object();
  public static Object monitor = new Object();
  public static Object step3Monitor = new Object();
  public static Object step2Monitor = new Object();
  public static AtomicMonitor lateCount = new AtomicMonitor(0);
  public static Object exitMonitor = new Object();
  public boolean interruptFlag = false;
  public InputStream sin;
  public ObjectInputStream oin;
  public OutputStream oStream;
  public PrintWriter pw;

  public Principal(int id) {
	this.name = "Principal-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	isAlive.add(this);

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
	rand = new Random().nextInt(100);
	thread.start();
  }

  private void randSleep() {
	try {
	  Thread.sleep(rand = new Random().nextInt(100) + 100);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public void wakeStep1() {
	synchronized (monitor) {
	  // let the principal go to step 1 and continue its process.
	  // debug
	  msg("notifying the wakestep 1 monitor");
	  monitor.notify();
	}
  }

  public void wakeStep2() {
	synchronized (step2Monitor) {
	  // let the principal go to step 1 and continue its process.
	  // debug
	  msg("notifying the wakestep 2 monitor");
	  step2Monitor.notify();
	}
  }

  public void wakeStep3() {
	synchronized (step3Monitor) {
	  // let the principal go to step 1 and continue its process.
	  // debug
	  msg("notifying the wakestep 3 monitor");
	  step3Monitor.notify();
	}
  }

  public void step1() {

	synchronized (monitor) {
	  try {
		msg("Waiting for school to Start");
		monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	// debug
	// System.out.println("debug printing out of monitor");
	if (!Student.schoolOpenFlag) {
	  synchronized (schoolStarting) {
		while (true) {
		  try {
			msg("school waiting");
			schoolStarting.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }
	}

//	if(Thread.interrupted()) {
//	  msg("was already interrupted at end of step 1");
//	}
//	else {
//	  msg("wasn't interrupted and shouldn't be a problem");
//	}

	// debug
	msg("Was notified by a student, and is out of the schoolstarting monitor");
  }

  public void step2() {

	synchronized (step2Monitor) {
	  try {
		msg("School started going next step");
		step2Monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	// debug
//	msg("debug printing out of monitor");


	// what if all the students aren't here by this time? This is our first problem.
	synchronized (Student.arrival) {
	  for (Student s : Student.arrival) {
		synchronized (s) {
		  if (!s.Questionare) {
			// we send the student home if they forgot the questionaire.
			s.sendStudentHome("was sent home by the principal for forgetting to do the questionare");
			s.notify();
			//this is triggering my notify?

		  } else {
			s.msg("notified by principal");
			// 1 in 3 odds
			s.ronaTest = rand % 3 > 1;
			rand = new Random().nextInt(100);
			s.notify();
		  }
		}
	  }
	}

  }

  public void step3() {
	synchronized (step3Monitor) {
	  try {
		msg("Starting to teach classes");
		step3Monitor.wait();
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
	}
	// debug
	msg("debug printing out of monitor");

	// step 3
	while (period.get() != 3) {
	  waitToTeachClass();
	  // a way to send the nurse home in a timely fashion without the nurse leaving
	  // early.
	  if (interruptFlag) {
		return;
	  }

	  sendNurseHome();
	  // notify to tell teacher that class has started
	  notifyTeachers();
	  teachClass();
	  // we need to balance out the students against a hardcoded variable.
	  Student.classCount.set(Student.studentCount.get());
	  period.getAndIncrement();
	  msg("class has ended, taking attendance");
	  takeAttendance();
	  msg("class has ended, notifying teachers");
	  clearCount();
	  // notify to tell teachers that class has ended.
	  notifyTeachers();
	  randSleep();
	  notifyPhysEdStudents();
	  notifyBreakRoom();
	}
	sendPrincipalHome();
  }

  @Override
  public void run() {

	// step 1
	msg("goign to step 1");
	step1();
	msg("going to step 2");
	step2();
	msg("going to step 3");
	step3();
	return;
  }

  public void sendPrincipalHome() {
	Thread.currentThread().interrupt();
	msg("Went home");
  }

  public void sendNurseHome() {
	if (!Nurse.goHome) {
	  msg("notified the nurse to go home");
	  synchronized (Nurse.nurseWaiting) {
		Nurse.goHome = true;
		Nurse.nurseWaiting.notify();
	  }
	}
	// we have to wake the nurse for the nurse to go home.
  }

  public void clearCount() {
	Student.mathCount.set(0);
	Student.ELACount.set(0);

  }

  public void notifyPhysEdStudents() {
	synchronized (Student.physEdMonitor) {
	  Student.physEdMonitor.notifyAll();
	}
  }

  public void notifyTeachers() {
	synchronized (Instructor.instructorElaMonitor) {
	  msg("notifying ela teacher");
	  Instructor.instructorElaMonitor.notify();
	}

	synchronized (Instructor.instructorMathMonitor) {
	  msg("notifying math teacher");
	  Instructor.instructorMathMonitor.notify();
	}
  }

  public void takeAttendance() {
	// ela takes attendance
	for (Student s : Student.physEd) {
	  s.attendance.add("physEd");
	}
	Student.physEd.clear();
	for (Student s : Student.ELA) {
	  // s.msg("attended ela");
	  s.attendance.add("ELA");
	}
	Student.ELA.clear();
	for (Student s : Student.math) {
	  // s.msg("attended math");
	  s.attendance.add("math");
	}
	// phys ed takes attendance
	Student.math.clear();
  }

  public void notifyBreakRoom() {
	// notifies all students that are on break
	// 10 second break.
	if (period.get() != 3) {
	  breakSleep();
	  synchronized (Student.breakAreaMonitor) {
		Student.breakAreaMonitor.notifyAll();
	  }
	}
  }

  public void notifyNurse() {
	synchronized (Nurse.nurseWaiting) {
	  Nurse.nurseWaiting.notify();
	}
  }

  public void teachClass() {
	// we can send the nurse home becuase all the students arrived to class and that
	// means that all students have been

	if (!Nurse.goHome) {
	  // we can send the nurse home.
	  Nurse.goHome = true;
	  notifyNurse();
	}
	gotoSleep();
  }

  public void gotoSleep() {
	// class is 20 seconds long.
	try {
	  msg("Is teaching class for 10 seconds");
	  Thread.sleep(10000);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public void breakSleep() {
	// class is 20 seconds long.
	try {
	  // msg("has gone to sleep for 5 seconds");
	  Thread.sleep(5000);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public void waitToTeachClass() {
	if (Student.classCount.get() != 20) {
	  synchronized (startClass) {
		while (true) {
		  try {
			// how is this printing twice?
			msg("is waiting for students");
			startClass.wait();
			break;
		  } catch (InterruptedException e) {
			continue;
		  }
		}
	  }
	  // msg("has awoken");
	}
  }

  public void sendPrincipalHome(String message) {
	// we kill the thread if they forgot the questionare.
	msg(message);
	Thread.currentThread().interrupt();
	interruptFlag = true;
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
