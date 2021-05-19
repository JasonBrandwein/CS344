import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NurseClient extends Thread implements moreActions {
  public int port = 1234;
  public String address = "127.0.0.1";
  public ObjectOutputStream oout;
  public Socket socket;
  public ListeningThread listen;

  public static void main(String[] args) {
	// only one nurse in the project.
	new NurseClient().start();
  }

  public void run() {
	try {
	  socket = new Socket(address, port);
	  OutputStream sout = socket.getOutputStream();
	  oout = new ObjectOutputStream(new BufferedOutputStream(sout));

	  InputStream istream = socket.getInputStream();
	  listen = new ListeningThread(istream, this);
	  listen.start();

	  Message message = new Message(Message.ClassType.Nurse, Message.Action.createNurse);
	  	//debug
	  //System.out.println("Sending create nurse message");
	  oout.writeObject(message);
	  oout.flush();

	  // moreActions();

	} catch (Exception e) {
	  System.err.println(e);
	}
  }

  public void moreActions(int i) throws IOException {
	// tell the nurse to do more stuff here.
	Socket socket = new Socket(address, port);
	oout = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	Message message;
	switch (i) {
	case 1:
	  message = new Message(Message.ClassType.Nurse, Message.Action.continueArrival);
	  // debug
	  // System.out.println("case 1 activated");
	  oout.writeObject(message);
	  oout.flush();
	  break;
	case 2:
	  // debug
	  // System.out.println("Case 2 actived");
	  message = new Message(Message.ClassType.Nurse, Message.Action.continueArrival2);
	  oout.writeObject(message);
	  oout.flush();
	  break;
	case 3:
	  message = new Message(Message.ClassType.Nurse, Message.Action.End);
	  // debug
	  //System.out.println("case 4, send home activated");
	  oout.writeObject(message);
	  oout.flush();
	  return;
	default:
	  break;
	}
  }

  public static void NurseClient() {
	// TODO Auto-generated method stub
	new NurseClient().start();

  }

  @Override
  public void moreActions(int i, int id) throws IOException {
	// TODO Auto-generated method stub

  }

}
