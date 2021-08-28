package Assignment2;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;



/**
* This class generates a transcript for each student, whose information is in the text file.
* 
*
*/

public class Transcript {
	private ArrayList<Object> grade = new ArrayList<Object>();
	private File inputFile;
	private String outputFile;
	
	/**
	 * This the the constructor for Transcript class that 
	 * initializes its instance variables and call readFie private
	 * method to read the file and construct this.grade.
	 * @param inFile is the name of the input file.
	 * @param outFile is the name of the output file.
	 */
	public Transcript(String inFile, String outFile) {
		inputFile = new File(inFile);	
		outputFile = outFile;	
		grade = new ArrayList<Object>();
		this.readFile();
	}// end of Transcript constructor

	/** 
	 * This method reads a text file and add each line as 
	 * an entry of grade ArrayList.
	 * @exception It throws FileNotFoundException if the file is not found.
	 */
	private void readFile() {
		Scanner sc = null; 
		try {
			sc = new Scanner(inputFile);	
			while(sc.hasNextLine()){
				grade.add(sc.nextLine());
	        }      
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			sc.close();
		}		
	} // end of readFile
	

	/**
	 * This method builds a Student ArrayList containing all the students from the input file
	 * @return a Student ArrayList which contains all the students and their respective
	 * information from the input file
	 * @exception It throws InvalidTotalException if the total weight of assessments is not 100 or if the total grade
	 * of a student is more than 100. The course will not be added to the list of courses taken by that student. 
	 */
	public ArrayList<Student> buildStudentArray() {
		ArrayList<Student> students = new ArrayList<>();
		
		for(Object obj: grade) {
			String line = (String) obj;
			
			// Split each line at '.'
			String[] tokens = line.split(",");
			
			// First index gives the student name, then credit, student id and so on
			String courseName = tokens[0];
			double credit = Double.parseDouble(tokens[1]);
			String studentId = tokens[2];
			ArrayList<Assessment> assessments = new ArrayList<>();
			
			// Last index in each line is the student name 
			String studentName = tokens[tokens.length-1];
			
			ArrayList<Double> grades = new ArrayList<Double>();
			ArrayList <Integer> weights = new ArrayList<Integer>();
		
			for(int i = 3; i < tokens.length-1; i++) {
				char type = tokens[i].charAt(0);
				// Split string again to separate assessment weight and grade for each assessment
				String [] assessmentTokens = tokens[i].substring(1).split("\\(");
				int weight = Integer.parseInt(assessmentTokens[0]);
				double mark = Double.parseDouble(assessmentTokens[1].substring(0, assessmentTokens[1].length()-1));
				Assessment assessment = Assessment.getInstance(type, weight);
				assessments.add(assessment);
				// Add grades and their weights to the respective ArrayList
				grades.add(mark);
				weights.add(weight);
			}
			
			// Create a Course object from each line of input file
			Course course = new Course(courseName, assessments, credit);
			
			// Since each student has multiple courses, here we check if we have already added this student
			// to the students ArrayList. If the student does not exists in the students ArrayList, then
			// we add this student to it
			Student student = findStudent(studentId, students);
			if(student == null) {
				student = new Student(studentId, studentName, new ArrayList<Course>());
				students.add(student);
			}
			// Add the final grade and course to this student using the respective methods
			// If the weights do not add to 100 or if the grades add to more than 100, this course
			// will not be added 
			try {
				student.addGrade(grades, weights);
				student.addCourse(course);
			} catch (InvalidTotalException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}

		}
		return students;
		
	}
	
	/**
	 * Helper method to check if a student if a particular student is already in the 
	 * ArrayList of students
	 * @param studentId student id
	 * @param students ArrayList of students
	 * @return returns the student if there is already a student with that id in the ArrayList of students
	 * or returns null if a student with that studentId does not exist in the ArrayList of students
	 */
	private Student findStudent(String studentId, ArrayList<Student> students) {
		for(Student s: students) {
			if(s.getStudentId().equals(studentId)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Method writes the transcript of students to an output file
	 * Prints student name, student id, the list of courses for the student and their final grades,
	 * and their GPA.
	 * @param st an ArrayList of students
	 */
	public void printTranscript(ArrayList<Student> st) {
		File file = new File(this.outputFile);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			for(Student s: st) {
				// write to a new output file
				writer.write(s.getName() + "\t" + s.getStudentId());
				writer.write("\n--------------------\n");
				ArrayList<Course> courseTaken = s.getCourse();
				ArrayList<Double> finalGrade = s.getFinalGrades();
				for(int i = 0; i < courseTaken.size(); i++) {
					writer.write(courseTaken.get(i).getCode() + "\t" + String.format("%.1f", finalGrade.get(i)) + "\n");
				}
				writer.write("--------------------\n");
				writer.write(String.format("GPA: %.1f", s.weightedGPA()) + "\n\n");
				
			}
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//main method to test the output
	public static void main(String args[]) {
		Transcript transcript = new Transcript("input.txt", "outputFile.txt");
		ArrayList <Student> st = transcript.buildStudentArray();
		transcript.printTranscript(st);
		System.out.println(transcript);
		
	}
	
	
} // end of Transcript

class Student {
	private String studentId;
	private String name;
	private ArrayList<Course> courseTaken; 
	private ArrayList<Double> finalGrade; 
	
	/**
	 * Default constructor for Student class
	 * Initializes this student to have default values for its fields
	 */
	public Student() {
		this.studentId = "";
		this.name = "";
		this.courseTaken = new ArrayList<Course>();
		this.finalGrade = new ArrayList<Double>();	
	}
	/**
	 * Custom constructor for the Student class
	 * Initializes this student to have specific values for its fields 
	 * @param sId is the student id
	 * @param n is the student name
	 * @param obj an ArrayList of courses
	 */
	public Student(String sId, String n, ArrayList<Course> obj) {
		this.studentId = sId;
		this.name = n;
		this.courseTaken = new ArrayList<>(obj);
		this.finalGrade = new ArrayList<Double>();
	}
	
	/**
	 * Getter method for student id
	 * @return studentId
	 */
	public String getStudentId() {
		return this.studentId;
	}
	
	/**
	 * getter method for student name
	 * @return student name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * getter method for courses taken by a student
	 * @return a defensive copy of an  ArrayList of courses
	 */
	public ArrayList<Course> getCourse() {
		return new ArrayList<Course>(this.courseTaken);
	}
	
	/**
	 * getter method for final grades of a student
	 * @return a defensive copy of an ArrayList of final grades
	 */
	public ArrayList<Double> getFinalGrades() {
		return new ArrayList<Double>(this.finalGrade);
	}
	
	/**
	 * setter method for id of this student 
	 * sets student id to the specified value
	 * @param sId student id
	 */
	public void setStudentId(String sId) {
		this.studentId = sId;
	}
	
	/**
	 * setter method for the name of this student
	 * sets the student name to the specified value
	 * @param n student name
	 */
	public void setName(String n) {
		this.name = n;
	}
	
	/**
	 * setter method for courses taken by this student
	 * 
	 * @param other ArrayList of courses taken by a student
	 */
	public void setCourse(ArrayList<Course> other) {
		this.courseTaken = new ArrayList<Course>(other);
	}
	
	/**
	 * setter method for final grades of this student 
	 * @param other ArrayList of final grades
	 */
	public void setFinalGrades(ArrayList<Double> other) {
		this.finalGrade = new ArrayList<Double>(other);
	}
	/**
	 * Calculates the final grade for each course a student has taken and
	 * adds it to finalGrade
	 * @param grades ArrayList of grades
	 * @param weights ArrayList of weights
	 * @throws InvalidTotalException if the total weight of assessments is not 100 or if the total grade
	 * of a student is more than 100
	 */
	public void addGrade(ArrayList<Double> grades, ArrayList<Integer> weights) throws InvalidTotalException{
		int totalWeight = 0;
		double sumGrades = 0.0;
		for(int i = 0; i < grades.size(); i++) {
			totalWeight += weights.get(i);
			sumGrades += (grades.get(i) * weights.get(i))/100;
		}
		
		if(totalWeight != 100 || sumGrades > 100) {
			throw new InvalidTotalException("Total weight is not 100 or Sum of grades is greater than 100");
		}
		
		finalGrade.add(sumGrades);	
	}
	/**
	 * method calculates the weighted GPA for this student using all the
	 * courses taken by this student and their final grades
	 * @return the GPA of this student 
	 */
	public double weightedGPA() {
		double totGrades = 0;
		int totCredits = 0;
		for(int i = 0; i < finalGrade.size(); i++) {
			totGrades += courseTaken.get(i).getCredit() * getGradePoint((finalGrade.get(i)));
			totCredits += courseTaken.get(i).getCredit();
		}
		return totGrades/totCredits;
		
	}
	
	/**
	 * Helper method to convert grade to Grade Point
	 * @param grade
	 * @return grade point 
	 */
	private int getGradePoint(double grade) {
		if(grade >= 90) return 9;
		if(grade >= 80) return 8;
		if(grade >= 75) return 7;
		if(grade >= 70) return 6;
		if(grade >= 65) return 5;
		if(grade >= 60) return 4;
		if(grade >= 55) return 3;
		if(grade >= 50) return 2;
		if(grade >= 47) return 1;
		return 0;
	}
	
	/**
	 * Adds a Course object to the the courseTaken ArrayList
	 * @param obj is an object of Course type
	 */
	public void addCourse(Course obj) {
		courseTaken.add(obj);
	}
	
	
}


class Course {
	private String code;
	private ArrayList<Assessment> assignment; 
	private double credit;
	
	/**
	 * default constructor for the Course class
	 * Initializes this course to have default values for its fields
	 */
	public Course() {
		this.code = "";
		this.assignment = new ArrayList<Assessment>();
		this.credit = 0.0;	
	}
	
	/**
	 * custom constructor for the Course class
	 * Initializes this course to have specific values for its fields
	 * @param cd course code
	 * @param a ArrayList of assignments for this course
	 * @param cr credit for this course
	 */
	public Course(String cd, ArrayList<Assessment> a, double cr) {
		this.code = cd;
		this.assignment = new ArrayList<Assessment>(a);
		this.credit = cr;	
	}
	
	/**
	 * copy constructor for the Course class
	 * Initializes this course to have the values of the other course for its fields
	 * @param other course object
	 */
	public Course(Course other) {
		this(other.code, new ArrayList<Assessment>(other.assignment), other.credit);
	}
	
	/**
	 * getter method for the course credit
	 * @return credit for this course
	 */
	public double getCredit() {
		return this.credit;
	}
	
	/**
	 * getter method for the course code
	 * @return code for this course
	 */
	public String getCode() {
		return this.code;
	}
	
	/**
	 * getter method for the list of assignments for this course
	 * @return a defensive copy of assignments for this course
	 */
	public ArrayList<Assessment> getAssignment() {
		return new ArrayList<Assessment>(assignment);
	}
	
	/**
	 * setter method for course credit
	 * sets this curse credit to the specified value
	 * @param cr credit
	 */
	public void setCredit(double cr) {
		this.credit = cr;
	}
	
	/**
	 * setter method for course code
	 * sets this course code to the specified value
	 * @param c code
	 */
	public void setCode(String c) {
		this.code = c;
	}
	
	/**
	 * setter method for course assignments
	 * sets this course to have assignments of the specified ArrayList
	 * @param x ArrayList of assignments
	 */
	public void setAssignment(ArrayList<Assessment> x) {
		this.assignment = new ArrayList<Assessment>(x);
	}
	
	/**
	 * Override the equals method. Two courses are equal if:
	 * they have the same code, assignments, and credit
	 * @return whether or not the two courses are equal
	 */
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(obj.getClass() == this.getClass()) {
			Course other = (Course) obj;
			return this.code.equals(other.code) && this.assignment.equals(other.assignment) 
					&& this.credit == other.credit;
		}
		return false;
	}
	
}


 class Assessment {
	private char type;
	private int weight;
	
	/**
	 * default constructor for the Assessment class
	 * Initializes this Assessment to have default values for its field 
	 */
	private Assessment() {
		this.type = '\0';
		this.weight = 0;
	}
	
	/**
	 * custom constructor for the Assessment class
	 * initializes this assessment to have specific values for its fields
	 * @param t assessment type
	 * @param w assessment weight
	 */
	private Assessment(char t, int w) {
		this.type = t;
		this.weight = w;
	}
	
	/**
	 * creates an instance of an Assessment with the passed values
	 * @param t assessment type
	 * @param w assessment weight
	 * @return an instance of Assessment with the specified values for its fields
	 */
	public static Assessment getInstance(char t, int w) {
		return new Assessment(t, w);
	}
	
	/**
	 * getter method for type
	 * @return type for this assessment
	 */
	public char getType() {
		return this.type;
	}
	
	/**
	 * getter method for weight
	 * @return weight of this assessment
	 */
	public int getWeight() {
		return this.weight;
	}
	
	/**
	 * setter method for type
	 * @param t type of assessment
	 */
	public void setType(char t) {
		this.type = t;
	}
	
	/**
	 * setter method for weight
	 * @param w weight of assessment
	 */
	public void setWeight(int w) {
		this.weight = w;
	}
	
	
	/**
	 * Override the equals method. Two assessments are equal if:
	 * they have their weights and type are the same
	 * @return whether or not the two assessments are equal
	 */
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(obj.getClass() == this.getClass()) {
			Assessment other = (Assessment) obj;
			if(this.type == other.type && this.weight == other.weight) {
				return true;
			}
		}
		return false;
	}
	
}
 

 class InvalidTotalException extends Exception {
	 public InvalidTotalException(String message) {
		 super(message);
	 }
 }

