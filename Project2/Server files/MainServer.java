
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class MainServer {
  public static Vector<SubServerThread> subVector = new Vector<>(30);
  int port = 1234;
  public static AtomicMonitor count = new AtomicMonitor(0);
  public ServerSocket serverSocket;
  public Socket socket;

  public void stop() {
	try {
	  if(serverSocket != null)
	  serverSocket.close();
	  if(socket != null)
	  socket.close();
	} catch (IOException ignored) {
	}
  }

  public MainServer() throws InterruptedException {
	try {
	  serverSocket = new ServerSocket(1234);
	  System.out.println("serverSocket is open.");
	  // times out in 15 seconds so we can check if the thread can go and exit.
	  serverSocket.setSoTimeout(30000);  
	  while (true) {
		socket = serverSocket.accept();
		
		if (count.get() == 23) {
		  socket.close();
		  serverSocket.close();
		  return;
		}
		
		// debug
		// System.out.println("socket = " + socket.toString());

		SubServerThread newThread = new SubServerThread(socket);
		// subVector.add(newThread);
	  } // while
	} // try
	catch(SocketTimeoutException e) {
	  try {
		//debug
		System.out.println("closing server socket");
		serverSocket.close();
		return;
	  } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	  }
	  //catch timeout when it does timeout
	}//catch
	catch (IOException e) {
	  System.out.println("Unable to listen to port.");
	  e.printStackTrace();
	} // catch
  }// constructor

  public static void main(String[] args) throws InterruptedException {
	new MainServer();
  }// main
}// class
