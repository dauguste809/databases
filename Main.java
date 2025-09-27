package studentDB;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;


public class Main extends JFrame{
	private Database db;
	private DefaultTableModel tableModel;
	
	
	public Main(Database db) {
		this.db = db;
		setTitle("Student Database");
		setSize(800,400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		//Table to display students
		tableModel = new DefaultTableModel(
			new String[] {"ID","First","Last","Major","GPA", 
					"BirthDate","Address", "SSN"},0);
		JTable table = new JTable(tableModel);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		//Control panel
		JPanel panel = new JPanel(new FlowLayout());
		JButton refreshBtn = new JButton("Refresh");
		JButton addBtn = new JButton("Add");
		JButton updateBtn = new JButton("Update");
		JButton deleteBtn = new JButton("Delete by ID");
		JButton searchBtn = new JButton("Search");
		
		panel.add(refreshBtn); //Implementend
		panel.add(addBtn);		//Implemented (But edit functionality to accept no SSN)
		panel.add(updateBtn);   //Implemented
		panel.add(searchBtn);   //Implemented
		panel.add(deleteBtn);   //Implemented
		add(panel, BorderLayout.NORTH);
		
		//Button actions
		refreshBtn.addActionListener(e -> refreshTable());
		addBtn.addActionListener(e -> addStudent());
		updateBtn.addActionListener(e -> updateStudent());
		searchBtn.addActionListener(e -> searchStudent());
		deleteBtn.addActionListener(e -> deleteStudent());
		
		/*Add INPUT VALIDATION 
		 * Add functionality that makes sure birthdate entered is valid date 
		 * Add functionality to allow blank input for SSN (international), for NO same SSN to be entered, and for SSN to fit format (***-**-***)
		 * Add error handler for if a number is not entered for id 
		 * Also if an id is entered that is already taken and for SSN (these should be unique)
		 * Also add functionality to order the table from least to greatest id 
		*/	
		
		refreshTable();	
	}
	
	//Refresh implementation
	private void refreshTable() {
		//Clear existing rows
		tableModel.setRowCount(0);
		
		//Get all students from database and sort by ID
		List<Student> students = db.getAllStudents().stream().sorted(Comparator.comparingInt
				(s->Integer.parseInt(s.getId()))).toList();
		
		//Add each student to the table
		for (Student s : students) {
			tableModel.addRow(new Object[] {
					s.getId(),
					s.getFirstName(),
					s.getLastName(),
					s.getMajor(),
					s.getGPA(),
					s.getBirthDate(),
					s.getAddress(),
					s.getSSN()	
			});
		}
	}

	//Add implementation
	private void addStudent() {
		
		//Add ALL student fields
		try {
		String id = validateId();
		if (id == null) return;
		String firstName = JOptionPane.showInputDialog(this, "Enter first name");
		String lastName = JOptionPane.showInputDialog(this, "Enter last name");
		String major = JOptionPane.showInputDialog(this, "Enter major");
		double gpa = validateGPA();
		if (gpa == -1) return;
		
		LocalDate birthDate = validateBirthdate();
		if (birthDate == null) return;
		
		String address = JOptionPane.showInputDialog(this, "Enter address");
		String ssn = validateSSN();
		if (ssn == null) return;
			
		//Create Student object
		Student newStudent = new Student(id, firstName, lastName, major,
				gpa, birthDate, address, ssn);
			
		//Add to database
		db.insertStudent(newStudent);
		refreshTable();
		JOptionPane.showMessageDialog(this, "Student successfully added!");
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(this,"Error adding student " + exc.getMessage());
		}		
	};
	
	//Validate birthday
	private LocalDate validateBirthdate() {
		while (true) {
			String input = JOptionPane.showInputDialog(this,"Enter birthdate (YYYY-MM-DD)");
			if (input == null || input.isBlank()) return null;      //cancelled by user
			
			try {
				LocalDate date = LocalDate.parse(input);
				if (date.isAfter(LocalDate.now())) {      //Checks date is not in the future 
					JOptionPane.showMessageDialog(this, "Birthdate is invalid");
					continue;
				} 
					return date;
				}
			 catch (Exception exc) {
				JOptionPane.showMessageDialog(this, "Invalid date. Must use format (YYYY-MM-DD) and valid date");
			}
		}
	}
	
	//Validate SSN
	private String validateSSN() {
		while(true) {
			String ssn = JOptionPane.showInputDialog(this, 
					"Enter SSN (XXX-XX-XXX) or N/A (if international)");
			if (ssn == null) return null;
				
				ssn = ssn.trim(); 
				
				if(ssn.equalsIgnoreCase("N/A") || ssn.isEmpty()) {
					return "";   //stored as a blank
				}
				
				//check format
				if (!ssn.matches("\\d{3}-\\d{2}-\\d{3}")) {
					JOptionPane.showMessageDialog(this, "SSN must match format (XXX-XX-XXX) or N/A");
					continue;
				}
				
				//check is SSN is unique
				if (db.ssnExists(ssn)){
					JOptionPane.showMessageDialog(this, "SSN is already in database.");
					continue;
				}
				return ssn;   //valid ssn
			}
		}

	
	//Validate GPA
	private double validateGPA() {
		while(true) {
			String gpaStr = JOptionPane.showInputDialog(this, "Enter GPA (0-4)");
			if (gpaStr == null) return -1;  //cancelled
			
			try {
				double gpa = Double.parseDouble(gpaStr);
				if (gpa < 0 || gpa > 4) {
					JOptionPane.showMessageDialog(this, "GPA must be between 0 and 4");
					continue;
				} 
				return gpa;
				
			} catch (NumberFormatException exc) {
				JOptionPane.showMessageDialog(this, "Invalid number format for GPA");
			}
		}
	}
	
	//Validate ID 
	private String validateId() {
		while(true) {
		String id = JOptionPane.showInputDialog(this, "Enter student ID");
		if (id == null || id.isEmpty()) return null;    //cancelled by user
			
		//make sure id is a #
			if (!id.matches("\\d+")){
				JOptionPane.showMessageDialog(this, "ID must be a number.");
				continue;
			}
		
		//make sure id is not taken
			if (db.idExists(id)) {
				JOptionPane.showMessageDialog(this, "ID is in database.");
				continue;
			}
		
			return id;
		}
	}
	
	//Update implementation
	private void updateStudent() {
		JDialog updateDialog = new JDialog(this, "Select Field to Update", true);
		updateDialog.setLayout(new GridLayout(5,1));
		updateDialog.setSize(250,150);
		
		String[] fields = {"First Name", "Last Name", "Major", "GPA", "Address"};
		for (String field : fields) {
			JButton btn = new JButton(field);
			btn.addActionListener(e -> {
				String id = JOptionPane.showInputDialog(this, "Enter student ID");
				String newValue = JOptionPane.showInputDialog(this, "Enter new " + field);
			if (id != null && newValue != null) {
				boolean updated = db.updateById(id, field, newValue);
				if (updated) {
					JOptionPane.showMessageDialog(this, "Updated successfully!");
					refreshTable();
				} else {
					JOptionPane.showMessageDialog(this, "ID not found or invalid field!");
				}
			}
			updateDialog.dispose();
		});
			updateDialog.add(btn);
		}
		
		updateDialog.setLocationRelativeTo(this);
		updateDialog.setVisible(true);
	}
	
	//Search implementation 
	private void searchStudent() {
		JDialog searchDialog = new JDialog (this, "Select Field to Search", true);
		searchDialog.setLayout(new GridLayout(7,1));
		searchDialog.setSize(250,200);
		
		String[] fields = {"First Name", "Last Name", "ID", "GPA", "Birthdate", "Major", "Address"};
		for (String field : fields) {
			JButton btn = new JButton(field);
			btn.addActionListener(e -> {
				
			if (field.equalsIgnoreCase("birthdate")) {
			//Before, After, or On Date
				JDialog dateDialog = new JDialog(this, "Birthdate Search", true);
				dateDialog.setLayout(new GridLayout(3,1));
				dateDialog.setSize(200,100);
				String[] modes = {"Before", "After", "On"};
					
			for (String mode : modes) {
				JButton modeBtn = new JButton(mode);
				modeBtn.addActionListener(ev -> {
				String dateInput = JOptionPane.showInputDialog(
					this, "Enter birthdate (YYYY-MM-DD)");
				if (dateInput != null) {
					try {
						LocalDate date = LocalDate.parse(dateInput);
						List<Student> results = db.queryByBirthdate(date, mode.toLowerCase());
						showStudents(results);
					} catch (Exception exc) {
						JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
					}
				}
				dateDialog.dispose();
				searchDialog.dispose();
				});
				dateDialog.add(modeBtn);
			}
				
				dateDialog.setLocationRelativeTo(this);
				dateDialog.setVisible(true);
				return;
				
			} else if (field.equalsIgnoreCase("gpa")) {
				String search = JOptionPane.showInputDialog(this, "Enter minimun GPA");
				if (search != null) {
					try {
						double gpa = Double.parseDouble(search);
						List<Student> results = db.queryByGPA(gpa);
						showStudents(results);
					} catch (NumberFormatException exc) {
						JOptionPane.showMessageDialog(this, "Invalid GPA.");
					}
				}
				searchDialog.dispose();
				return;
				
			} else if (field.equalsIgnoreCase("address")) {
				String search = JOptionPane.showInputDialog(this,
						"Enter a keyword to search in the address");
				if (search != null) {
					List<Student> results = db.queryByAddress(search);
					showStudents(results);
				}
				searchDialog.dispose();
				return;
		}
				
			String search = JOptionPane.showInputDialog(this,"Enter " + field);
			if (search != null) {
				List<Student> results = null;
				switch (field.toLowerCase()) {
				case "first name": results = db.queryByFirstName(search);
					break;
				case "last name": results = db.queryByLastName(search);
					break;
				case "id": 
					Student s = db.queryById(search);
					results = s != null ? List.of(s) : List.of();
					break;
				case "gpa": results = db.queryByGPA(Double.parseDouble(search));
					break;
				case "birthdate": results = db.queryByBirthdate(LocalDate.parse(search),"on");
					break;
				case "major": results = db.queryByMajor(search);
					break;
					
				}
				showStudents(results);
			}
				searchDialog.dispose();
		});
			searchDialog.add(btn);
		}
		
		searchDialog.setLocationRelativeTo(this);
		searchDialog.setVisible(true);
	}
	
	//To show a table of the searched entries 
	private void showStudents(List<Student> students) {
		//Very simialr to refreshTable()
		tableModel.setRowCount(0);
		
		//(Difference) Doesn't get all students from db
		//Add each student to the table
		for (Student s : students) {
			tableModel.addRow(new Object[] {
				s.getId(),
				s.getFirstName(),
				s.getLastName(),
				s.getMajor(),
				s.getGPA(),
				s.getBirthDate(),
				s.getAddress(),
				s.getSSN()	
			});
		
		}
	}
	
	//Delete implementation
	private void deleteStudent() {
		String id = JOptionPane.showInputDialog(this, 
				"Enter the ID of the student to be deleted");
		if (id != null) {
			boolean removed = db.deleteById(id);
			if (removed) {
				JOptionPane.showMessageDialog(this,  "Student succesfully deleted.");
				refreshTable();
			} else {
				JOptionPane.showMessageDialog(this, "No student with ID " + id + " found.");
			}
		}
	}
	

    public static void main(String[] args) {
        String fileName = "/Users/donellauguste/Desktop/students.txt"; // change path as needed
        Database db = new Database(fileName);
    
        SwingUtilities.invokeLater(() -> {
        	Main gui = new Main(db);
        	gui.setVisible(true);
        });
        
  /*
        // --- Test insert ---
        Student newStudent = new Student(
            "Bob", "Smith", "Math", "102", 3.6,
            LocalDate.of(2004, 5, 10),
            "456 Oak St", "222-33-4444"
        );
        db.insertStudent(newStudent);
        System.out.println("Inserted Bob Smith.");

        // --- Test query by first name ---
        List<Student> firstNameQuery = db.queryByFirstName("Bob");
        for (Student s : firstNameQuery) {
            System.out.println("Found by first name: " + s.getFirstName() + " " + s.getLastName());
        }

        // --- Test query by birthdate ---
        List<Student> bornBefore = db.queryByBirthdate(LocalDate.of(2004,1,1), "before");
        for (Student s : bornBefore) {
            System.out.println("Born before 2004: " + s.getFirstName() + " " + s.getBirthDate());
        }

        // --- Test update ---
        boolean updated = db.updateStudentById("102", "gpa", "3.9");
        System.out.println("Updated GPA? " + updated);

        // --- Test delete ---
        boolean deleted = db.deleteStudentById("102");
       System.out.println("Deleted student? " + deleted);
       */
    }
}

