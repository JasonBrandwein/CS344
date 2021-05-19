import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PrincipalClient extends Thread implements moreActions {
  public int port = 1234;
  public String address = "127.0.0.1";
  public ObjectOutputStream oout;
  public Socket socket = null;
  public ListeningThread listen = null;

  public PrincipalClient() {
	// TODO Auto-generated constructor stub
  }

  public static void main(String[] args) {
	// only one nurse in the project.
	new PrincipalClient().start();
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
	  Message message = new Message(Message.ClassType.Principal, Message.Action.createPrincipal);
	  oout.writeObject(message);
	  oout.flush();
	} catch (Exception e) {
	  System.err.println(e);
	}
  }

  public static void PrincipalClient() {
	// TODO Auto-generated method stub
	new PrincipalClient().start();

  }

  @Override
  public void moreActions(int i) throws IOException {
	// TODO Auto-generated method stub
	Socket socket = new Socket(address, port);
	oout = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	Message message;

	switch (i) {
	case 1:
	  message = new Message(Message.ClassType.Principal, Message.Action.wakeStep1);
		//debug
		//System.out.println("case 1 activated");
		oout.writeObject(message);
		oout.flush();
		break;
	case 2:
	  message = new Message(Message.ClassType.Principal, Message.Action.wakeStep2);
		//debug
		//System.out.println("case 1 activated");
		oout.writeObject(message);
		oout.flush();
		break;
	case 3:
	  message = new Message(Message.ClassType.Principal, Message.Action.wakeStep3);
		//debug
		//System.out.println("case 1 activated");
		oout.writeObject(message);
		oout.flush();
		break;		
	case 4:
	  message = new Message(Message.ClassType.Principal, Message.Action.End);
	  // debug
	  //System.out.println("case 4, send home activated");
	  oout.writeObject(message);
	  oout.flush();
	  return;
	default:
	  System.out.println("Defaulting - Principal");
	  break;
	}
  }

  @Override
  public void moreActions(int i, int id) throws IOException {
	// TODO Auto-generated method stub
	
  }

}
