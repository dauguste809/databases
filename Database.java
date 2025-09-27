package studentDB;

import java.io.*;
import javax.swing.JOptionPane;
import java.util.*;
import java.time.LocalDate;

public class Database {
	
	private List<Student> students;
	private String fileName;
	
	//Constructor 
	public Database(String fileName) {
		this.fileName = fileName;
		this.students = new ArrayList<>();
		loadStudents();
	}
	
	//Loading students from file
	private void loadStudents() {
		students.clear();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;
			while ((line = br.readLine()) != null) {
				students.add(Student.fromFileString(line));
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + e.getMessage());
		}
	}
	
	//Saving students to file
	public void saveStudents() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))){
			for (Student s: students) {
				bw.write(s.toFileString());
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println("Error writing file" + e.getMessage());
		}
	}
	
	//For GUI to access student list and display any changes
	public List<Student> getAllStudents(){
		return new ArrayList<>(students);
	}
	
	//Insert student
	public void insertStudent(Student s) {
		students.add(s);
		saveStudents();    //Writing back to file
	}
	
	//Ensure unique ID
	public boolean idExists(String id) {
		return students.stream().anyMatch(s -> s.getId().equals(id));
	}
	
	//Ensure unique SSN
	public boolean ssnExists(String ssn) {
		return students.stream().filter(s->s.getSSN() != null && !s.getSSN().isBlank())
				.anyMatch(s->s.getSSN().equals(ssn));
	}
	
	//Delete by ID
	public boolean deleteById(String id) {
		Iterator<Student> iter = students.iterator();
		while (iter.hasNext()) {
			Student s = iter.next();
			if (s.getId().equals(id)) {
				iter.remove();
				saveStudents();
				return true;   //deleted
			}
		}
		return false;       //ID not found
	}
	
	//Update by ID
	public boolean updateById(String id, String category, String newValue) {
		for (Student s : students) {
			if (s.getId().equals(id)) {
				switch (category.toLowerCase()) {
				case "first name": s.setFirstName(newValue);
				break;
				case "last name": s.setLastName(newValue);
				break;
				case "major": s.setMajor(newValue);
				break;
				case "gpa": 
					try {
						double gpa = Double.parseDouble(newValue);
						if ( gpa < 0 || gpa> 4) throw new NumberFormatException();
						s.setGPA(gpa);			
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "Invalid GPA entered");
						return false;
					}
				break;
				case "address" : s.setAddress(newValue);
				break;
				}
				saveStudents();
				return true;
			}
		}
		return false;
	}
	
	//Query by First Name 
	public List<Student> queryByFirstName(String firstName) {
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			if (s.getFirstName().equalsIgnoreCase(firstName)) {
				result.add(s);
			}
		}
		return result;
	}
	
	//Query by Last Name 
	public List<Student> queryByLastName(String lastName) {
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			if (s.getLastName().equalsIgnoreCase(lastName)) {
				result.add(s);
			}
		}
		return result;
	}
	
	//Query by ID 
	public Student queryById(String id) {
		for (Student s : students) {
			if (s.getId().equals(id))
				return s;
		}
		return null;
	}
	
	//Query by GPA >=
	public List<Student> queryByGPA(double minGPA){
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			if (s.getGPA() >= minGPA) {
				result.add(s);
			}
		}
		return result;
	}

	
	//Query by Birthdate
	public List<Student> queryByBirthdate(LocalDate date, String condition) {
		//condition: "before", "after", "on"
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			switch (condition.toLowerCase()) {
			case "before": if (s.getBirthDate().isBefore(date)) result.add(s);
			break;
			case "after": if (s.getBirthDate().isAfter(date)) result.add(s);
			break;
			case "on": if (s.getBirthDate().isEqual(date)) result.add(s);
			break;
			}
		}
		return result;
	}
	
	//Query by Major
	public List<Student> queryByMajor(String major){
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			if (s.getMajor().equalsIgnoreCase(major)) {
				result.add(s);
			}
		}
		return result;
	}
	
	//Query by Address (Key: Street, City, State (if initially entered with address))
	public List<Student> queryByAddress(String keyword){
		List<Student> result = new ArrayList<>();
		for (Student s : students) {
			if (s.getAddress().toLowerCase().contains(keyword.toLowerCase())) {
				result.add(s);
			}
		}
		return result;
	}

}
