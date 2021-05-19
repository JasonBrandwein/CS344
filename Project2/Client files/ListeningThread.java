import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ListeningThread extends Thread {
  private moreActions stub;
  private InputStream sin;

  public ListeningThread(InputStream sin, moreActions stub) {
	this.sin = sin;
	this.stub = stub;
  }

  public void msg(String msg) {
	System.out.println("From server: " + msg);
  }

  public int getId(String msg) {
	String id = "";
	Boolean flag = false;
	Boolean flag2 = false;
	// i need to get the id when dealing with vectors >1 of size.
	for (char c : msg.toCharArray()) {
	  if (!flag2 && c == '-') {
		flag2 = true;
	  }
	  if (flag2 && c >= '0' && c <= '9') {
		id += c;
		flag = true;
	  } else if (flag) {
		break;
	  }
	}
	// debug
	// System.out.println("id = " + id);
	return Integer.parseInt(id);

  }

  @Override
  public void run() {
	BufferedReader reader = new BufferedReader(new InputStreamReader(this.sin));
	String line;

	try {
	  while (true) {
		if (null != (line = reader.readLine())) {
		  msg(line);
		  // nurse case
		  if (line.contains("has arrived to school and is waiting on students to be tested")) {
			this.stub.moreActions(1);
		  } else if (line.contains("Nurse went to the office")) {
			// System.out.println("Sending more commands");
			this.stub.moreActions(2);
		  } else if (line.contains("has tested everyone, and is going home")) {
			this.stub.moreActions(3);
			return;
		  }
		  // principal case
		  else if (line.contains("Waiting for school to Start")) {
			// System.out.println("wakeStep1- principal");
			this.stub.moreActions(1);
		  } else if (line.contains("School started going next step")) {
			// System.out.println("wakeStep2- principal");
			this.stub.moreActions(2);
		  } else if (line.contains("Starting to teach classes")) {
			// System.out.println("wakeStep3- principal");
			this.stub.moreActions(3);
		  } else if (line.contains("Went home.")) {
			return;
		  }
		  // Instructor case
		  else if (line.contains("has arrived to school and is ready to teach")) {
			// do instructor stuff here
			int id = getId(line);
			// debug
			// System.out.println("Instructor with id " + id + " is being sent a release
			// command");

			this.stub.moreActions(1, id);
		  } else if (line.contains("ready to teach class")) {
			// do stuff.
			int id = getId(line);

			this.stub.moreActions(2, id);

		  } else if (line.contains("is ready to go home")) {
			// do stuff.
			int id = getId(line);

			this.stub.moreActions(3, id);
		  } else if (line.contains("has gone home.")) {
			int id = getId(line);
			this.stub.moreActions(4, id);
			return;
		  }

		  // student case
		  else if (line.contains("woke up and is heading to school")) {
			int id = getId(line);

			this.stub.moreActions(1, id);
		  } else if (line.contains("is ready to go to the schoolyard")) {
			int id = getId(line);

			this.stub.moreActions(2, id);
		  } else if (line.contains("ready to go to class")) {
			int id = getId(line);
			this.stub.moreActions(3, id);
		  } else if (line.contains("at period 3") || line.contains("was late to school and was sent home early.")
			  || line.contains("was sent home by the principal for forgetting to do the questionare")) {
			int id = getId(line);
			this.stub.moreActions(4, id);
			return;
		  } else {
			// debug
			// System.out.println("Was elsed and line was " + line);
		  }
		}
	  }
	} catch (IOException e) {
	  e.printStackTrace();
	} finally {
	  try {
		reader.close();
	  } catch (IOException e) {
		e.printStackTrace();
	  }
	  Thread.currentThread().interrupt();
	}
  }
}
