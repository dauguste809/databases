package studentDB;

import java.time.*;

public class Student {
	private final String id;
	private String firstName;
	private String lastName;
	private String major;
	private double gpa;
	private LocalDate birthDate;
	private String address;
	private String ssn;
	
	//Constructor
	public Student(String id, String firstName, String lastName, String major, 
			double gpa, LocalDate birthDate, 
			String address, String ssn ){
	this.id = id;
	this.firstName = firstName;
	this.lastName = lastName;
	this.major = major;
	this.gpa = gpa;
	this.birthDate = birthDate;
	this.address = address;
	this.ssn = ssn;
	}
	
	//Writing to file 
	public String toFileString() {
		return String.join("|", id, firstName, lastName, major, 
				String.valueOf(gpa), //Converting GPA double to String
				birthDate.toString(), //Converting LocalDate to String
				address, ssn);
	}
	
	
	//Reading from file 
	public static Student fromFileString(String line) {
		String[] s = line.split("\\|");
		return new Student(s[0], s[1], s[2], s[3], 
				Double.parseDouble(s[4]),   //Converting to double for easier query
				LocalDate.parse(s[5]), 		//Converting to LocalDate for easier query
				s[6], s[7] );
	}
	
	
	//Getters
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getMajor() {
		return major;
	}
	
	public String getId() {
		return id;	
	}
	
	public double getGPA() {
		return gpa;
	}
	
	public LocalDate getBirthDate() {
		return birthDate;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getSSN() {
		return ssn;
	}
	
	//Setters
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setMajor(String major) {
		this.major = major;
	}
	
	public void setGPA(double gpa) {
		this.gpa = gpa;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

}
