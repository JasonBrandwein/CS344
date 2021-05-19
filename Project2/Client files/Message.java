import java.io.Serializable;

public class Message implements Serializable {

  private static final long serialVersionUID = 7304482163498965389L;
  private ClassType classType;
  private Action action;
  private int id;
  public Message() {
  }

  public Message(ClassType classType, Action action) {
	this.classType = classType;
	this.action = action;
  }
  public Message(ClassType classType, Action action,int id) {
 	this.classType = classType;
 	this.action = action;
 	this.id = id;
   }
  
  public int getId() {
	return this.id;
  }
  
  public ClassType getClassType() {
	return classType;
  }

  public void setThreadType(ClassType classType) {
	this.classType = classType;
  }

  public Action getAction() {
	return action;
  }

  public void setAction(Action action) {
	this.action = action;
  }

  public enum ClassType {
	Nurse, Principal, Instructor, Student;
  }

  public enum Action {
	createNurse, createPrincipal, createInstructor, createStudent, continueArrival, continueArrival2, wakeStep1,
	wakeStep2, wakeStep3, End;
  }
}
