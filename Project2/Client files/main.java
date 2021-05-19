import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Receiver;

public class main extends Thread {
  public static int numStudents = 20;
  public static final int numInstructors = 2;
  public static final int numPrincipal = 1;
  public static final int numNurse = 1;
  public static int shortTime = 50;

  public static void main(String[] args) throws InterruptedException {
	// only one nurse in the project.
	// run each client the amount of times necassary.
	// Create Principals
	
	System.out.println("Creating principal");
	for (int i = 0; i < numPrincipal; i++) {
	  
	  PrincipalClient.PrincipalClient();
	  // Can't create the sockets too quickly otherwise we'll get a socket reset error
	  sleepTime();
	}
	// create Instructors

	System.out.println("Creating instructors");
	for (int i = 0; i < numInstructors; i++) {
	  InstructorClient.InstructorClient();
	  // Can't create the sockets too quickly otherwise we'll get a socket reset error
	  sleepTime();
	}

	// Create students
	System.out.println("Creating students");
	for (int i = 0; i < numStudents; i++) {
	  StudentClient.StudentClient();
	  // Can't create the sockets too quickly otherwise we'll get a socket reset error
	  sleepTime();
	}

	// Create Nurse
	System.out.println("Creating nurse");

	for (int i = 0; i < numNurse; i++) {
	  NurseClient.NurseClient();
	  // Can't create the sockets too quickly otherwise we'll get a socket reset error
	  sleepTime();
	  }
  }
  public static void sleepTime() {
	  try {
		TimeUnit.MILLISECONDS.sleep(shortTime);
	  } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
}
