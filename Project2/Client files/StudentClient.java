import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class StudentClient extends Thread implements moreActions{
  public int port = 1234;
  public String address = "127.0.0.1";
  public ObjectOutputStream oout;
  public ListeningThread listen;
  public Socket socket;
  public Message message;
  public StudentClient() {
	// TODO Auto-generated constructor stub

  }
  public static void main(String[] args) {
	//only one nurse in the project.
  }
  
  public void run() {
	Socket socket = null;
	try {
	  socket = new Socket(address, port);
	  OutputStream sout = socket.getOutputStream();
	  oout = new ObjectOutputStream(new BufferedOutputStream(sout));

	  InputStream istream = socket.getInputStream();
	  listen = new ListeningThread(istream, this);
	  listen.start();

	  message = new Message(Message.ClassType.Student, Message.Action.createStudent);
	  oout.writeObject(message);
	  oout.flush();
      
      //moreActions();      
	}
	catch(Exception e) {
	  System.err.println(e);
	}
  }
  
  public static void StudentClient() {
	// TODO Auto-generated method stub
	new StudentClient().start();
	
  }
  @Override
  public void moreActions(int i) throws IOException {
	// TODO Auto-generated method stub
	
  }
  @Override
  public void moreActions(int i, int id) throws IOException {
 	// TODO Auto-generated method stub
 	switch(i) {
 	case 1:
 	  //debug
 	  //System.out.println("Instructor with id " + id + " is in case 1");
 		message = new Message(Message.ClassType.Student, Message.Action.wakeStep1, id);
 		//debug
 		//System.out.println("case 1 activated");
 		oout.writeObject(message);
 		oout.flush();
 		break;
 	case 2:
 	  //debug
 	  //System.out.println("Instructor with id " + id + " is in case 2");
 		message = new Message(Message.ClassType.Student, Message.Action.wakeStep2, id);
 		//debug
 		//System.out.println("case 2 activated");
 		oout.writeObject(message);
 		oout.flush();
 		break;
 	case 3:
 	  //debug
 	  //System.out.println("Instructor with id " + id + " is in case 3");
 		message = new Message(Message.ClassType.Student, Message.Action.wakeStep3, id);
 		//debug
 		//System.out.println("case 3 activated");
 		oout.writeObject(message);
 		oout.flush();
 		break;
 	case 4:
 	 message = new Message(Message.ClassType.Student, Message.Action.End, id);
		//debug
		//System.out.println("case 4, send home activated");
		oout.writeObject(message);
		oout.flush();
		return;
 	default:
 	  //debug
 	  System.out.println("Instructor with id " + id + " has defaulted");
 	  break;
 	}
   }

}
