package project1;

public class Main {

  public static int numStudents = 20;
  public static final int numInstructors = 2;
  public static final int numPrincipal = 1;
  public static final int numNurse = 1;
  public static Student[] students;

  public static void main(String[] args) {
	Student[] students = new Student[numStudents];
	Principal principal = new Principal(numPrincipal);
	Instructor[] instructor = new Instructor[numInstructors];
	Nurse nurse = new Nurse(numNurse);

	principal.start();
	for (int i = 0; i < numInstructors; i++) {
	  instructor[i] = new Instructor(i);
	  instructor[i].start();
	}
	for (int i = 0; i < numStudents; i++) {
	  students[i] = new Student(i);
	  students[i].start();
	}

	nurse.start();

  }

}
