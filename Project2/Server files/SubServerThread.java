
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class SubServerThread extends Thread {
  private Socket socket;
  private ObjectInputStream oin;
  private String name;
  public int id;
  public static long time = System.currentTimeMillis();
  public static AtomicMonitor subCount = new AtomicMonitor(0);
  public static AtomicMonitor subServerThreadId = new AtomicMonitor(0);
  public static AtomicMonitor nurseCount = new AtomicMonitor(0);
  public static AtomicMonitor studentCount = new AtomicMonitor(0);
  public static AtomicMonitor principalCount = new AtomicMonitor(0);
  public static AtomicMonitor instructorCount = new AtomicMonitor(0);

//  // vector for each thread being made of each supertype.
  public static Vector<Principal> principalVector = new Vector<>();
  public static Vector<Student> studentVector = new Vector<>(20);
  public static Vector<Instructor> instructorVector = new Vector<>();
  public static Vector<Nurse> nurseVector = new Vector<>();
  public Nurse nurse;
  public InputStream inStream;
  public Principal principal;

  // constructor

  public SubServerThread() {
	// default constructor dgetThreadTypeo nothing
  }

  public SubServerThread(Socket socket) {
	this.id = subCount.getAndIncrement();
	this.socket = socket;
	try {
	  this.inStream = socket.getInputStream();
	  this.oin = new ObjectInputStream(new BufferedInputStream(this.inStream));
	  this.name = "subserver-thread-" + subServerThreadId.getAndIncrement();
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}

	this.start();
  }

  public void run() {
	// find out what the message that was sent is.
	Message msg;
	Boolean runtime = true;
	try {
	  while (runtime) {
		// System.out.println("oin is the problem? We're not able to read new objects
		// because we never get the in the end somehow");
		Object obj = this.oin.readObject();
		msg = (Message) obj;

		// debug
//		msg("id = " + this.name);
//		msg("msg = " + msg.toString());
//		msg("msg class = " + msg.getClassType());
//		msg("msg action = " + msg.getAction());

		// way to end the threads if needed.
		if (msg.getAction() == Message.Action.End) {
		  MainServer.count.getAndIncrement();
		  // debug
		  // System.out.println("count = " + MainServer.count.get());
		  this.oin.close();
		  this.socket.close();
		  break;
		} else
		  doAction(msg);
		// System.out.println("end of while loop looking to read next object");

	  } // end of while

	} catch (ClassNotFoundException e) {
	  // TODO Auto-generated catch block
	  System.out.println(this.name + " threw the error.");
	  e.printStackTrace();
	} catch (IOException e) {
	  runtime = false;
	  return;
	  // debug
	  // ystem.out.println("weird error shit happened here");
	}
	/*
	 * finally { try { this.oin.close(); this.socket.close(); } }
	 */

	catch (Exception e) {
	  e.printStackTrace();
	}

  }

  public void doAction(Message msg) {
	Message.ClassType threadType = msg.getClassType();
	Message.Action threadAction = msg.getAction();

	switch (threadType) {
	case Nurse:
	  // to find out what action to do on that thread.
	  switch (threadAction) {
	  case createNurse:

		nurse = new Nurse(nurseCount.getAndIncrement());
		nurse.setStuff(socket, oin);
		nurseVector.add(nurse);
		// System.out.println("setting nurse shit");
		nurse.start();
		break;

	  case continueArrival:
		// System.out.println("Continue nurse arrived at school - start");
		nurseVector.get(0).notifyMonitor();
		// this.nurse.notifyMonitor();
		break;

	  case continueArrival2:
		// System.out.println("Continue arrival nurse sent to office - start");
		nurseVector.get(0).wakeOffice();
		break;
	  default:
		System.out.println("No valid nurse action input");
		break;
	  } // end switch thread action
	  break;
	case Instructor:
	  // to find out what action to do on that thread.
	  switch (threadAction) {
	  case createInstructor:
		Instructor instructor = new Instructor(instructorCount.getAndIncrement());
		instructorVector.add(instructor);
		instructor.setStuff(socket, oin);
		instructor.start();
		break;
	  case wakeStep1:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 1");
		instructorVector.get(id).notifyMonitor();
		break;
	  case wakeStep2:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 2");
		instructorVector.get(id).notifyMonitor();
		break;
	  case wakeStep3:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 3");
		instructorVector.get(id).notifyMonitor();
		break;
	  default:
		break;
	  }

	  break;
	case Principal:
	  // to find out what action to do on that thread.
	  switch (threadAction) {
	  case createPrincipal:
		principal = new Principal(principalCount.getAndIncrement());
		principalVector.add(principal);
		principal.setStuff(socket, oin);
		// debug
		// System.out.println("setting principal ");
		principal.start();
		break;
	  case wakeStep1:
		// debug
		// System.out.println("Wake step 1 - principal");
		principalVector.get(0).wakeStep1();
		break;
	  case wakeStep2:
		// debug
		// System.out.println("Wake step 2 - principal");
		principalVector.get(0).wakeStep2();
		break;
	  case wakeStep3:
		// debug
		// System.out.println("Wake step 3 - principal");
		principalVector.get(0).wakeStep3();
		break;
	  default:
		// debug
		System.out.println("Defaulted - principal");
		break;
	  }

	  break;
	case Student:
	  // to find out what action to do on that thread.
	  switch (threadAction) {
	  case createStudent:
		Student student = new Student(studentCount.getAndIncrement());
		student.setStuff(socket, oin);
		studentVector.add(student);
		student.start();
		break;
	  case wakeStep1:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 1");
		studentVector.get(id).notifyMonitor();
		break;
	  case wakeStep2:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 2");
		studentVector.get(id).notifyMonitor();
		break;
	  case wakeStep3:
		id = msg.getId();
		// debug
		// System.out.println("Should notify the monitor - step 3");
		studentVector.get(id).notifyMonitor();
		break;
	  default:
		System.out.println("Student has defaulted");
		break;
	  }
	  break;

	default:
	  System.out.println("Invalid class-type");
	} // end switch threadType

  }

  public void msg(String msg) {
	msg = "[" + (System.currentTimeMillis() - time) + "] " + this.name + ": " + msg;
	System.out.println(msg);
  }
}
