package model;

public class UserData {

    // --- Core (from registration) ---
    private int    user_id;
    private String username;
    private String email;
    private String password;
    private String confirm_password;

    // --- Extended student details ---
    private String fullName;
    private String phone;
    private String dateOfBirth;
    private String nationality;
    private String program;
    private String yearOfStudy;
    private String semester;
    private String address;
    private String ecName;
    private String ecRelation;
    private String ecNumber;

    // --- Profile picture (stores file path as String) ---
    private String userImage;

    public UserData() {}

    public UserData(String username, String email, String password, String confirm_password) {
        this.username         = username;
        this.email            = email;
        this.password         = password;
        this.confirm_password = confirm_password;
    }

    // --- Core getters / setters ---
    public int    getId()                          { return user_id; }
    public void   setId(int user_id)               { this.user_id = user_id; }
    public String getUsername()                    { return username; }
    public void   setUsername(String username)     { this.username = username; }
    public String getEmail()                       { return email; }
    public void   setEmail(String email)           { this.email = email; }
    public String getPassword()                    { return password; }
    public void   setPassword(String password)     { this.password = password; }
    public String getConfirmPassword()             { return confirm_password; }
    public void   setConfirmPassword(String cp)    { this.confirm_password = cp; }

    // --- Extended getters / setters ---
    public String getFullName()                    { return fullName; }
    public void   setFullName(String fullName)     { this.fullName = fullName; }
    public String getPhone()                       { return phone; }
    public void   setPhone(String phone)           { this.phone = phone; }
    public String getDateOfBirth()                 { return dateOfBirth; }
    public void   setDateOfBirth(String dob)       { this.dateOfBirth = dob; }
    public String getNationality()                 { return nationality; }
    public void   setNationality(String n)         { this.nationality = n; }
    public String getProgram()                     { return program; }
    public void   setProgram(String program)       { this.program = program; }
    public String getYearOfStudy()                 { return yearOfStudy; }
    public void   setYearOfStudy(String y)         { this.yearOfStudy = y; }
    public String getSemester()                    { return semester; }
    public void   setSemester(String semester)     { this.semester = semester; }
    public String getAddress()                     { return address; }
    public void   setAddress(String address)       { this.address = address; }
    public String getEcName()                      { return ecName; }
    public void   setEcName(String ecName)         { this.ecName = ecName; }
    public String getEcRelation()                  { return ecRelation; }
    public void   setEcRelation(String ecRelation) { this.ecRelation = ecRelation; }
    public String getEcNumber()                    { return ecNumber; }
    public void   setEcNumber(String ecNumber)     { this.ecNumber = ecNumber; }

    // --- Profile picture ---
    public String getUserImage()                   { return userImage; }
    public void   setUserImage(String userImage)   { this.userImage = userImage; }
}