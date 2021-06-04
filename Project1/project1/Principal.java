package project1;

import java.util.Random;
import java.util.Vector;
//Made my own AtomicMonitor class to satisfy the condition of monitor only no other synchronization tools.
//import java.util.concurrent.atomic.AtomicInteger;

public class Principal implements Runnable {
  public static Vector<Principal> isAlive = new Vector<>();
  private String name;
  public static long time = System.currentTimeMillis();
  public Thread thread;
  public int rand;; // will be used more than once

  public static AtomicMonitor period = new AtomicMonitor(0);

  public static Object schoolStarting = new Object();
  public static Object startClass = new Object();

  public static AtomicMonitor lateCount = new AtomicMonitor(0);
  public static Object exitMonitor = new Object();
  public boolean interruptFlag = false;

  public Principal(int id) {
	this.name = "Principal-" + Integer.toString(id);
	thread = new Thread(this, this.name);
	isAlive.add(this);

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

  @Override
  public void run() {
	synchronized (schoolStarting) {
	  msg("Waiting for school to Start");
	  while (true) {
		try {
		  schoolStarting.wait();
		  break;
		} catch (InterruptedException e) {
		  continue;
		}
	  }
	}

	msg("School started");

	// what if all the students aren't here by this time? This is our first problem.
	synchronized (Student.arrival) {
	  for (Student s : Student.arrival) {
		synchronized (s) {
		  if (!s.Questionare) {
			// we send the student home if they forgot the questionaire.
			s.sendStudentHome("was sent home by the principal for forgetting to do the questionare");

			s.notify();

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
	  msg("Is teaching class for 20 seconds");
	  Thread.sleep(20000);
	} catch (InterruptedException ie) {
	  System.out.println(ie);
	}
  }

  public void breakSleep() {
	// class is 20 seconds long.
	try {
	  // msg("has gone to sleep for 10 seconds");
	  Thread.sleep(10000);
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
	System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m + ".");
  }
}
