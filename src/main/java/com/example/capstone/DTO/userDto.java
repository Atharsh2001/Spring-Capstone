package com.example.capstone.DTO;


import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class userDto {
    @Size(min = 3,max = 16,message = "First Name Length must be 3 to 16 Characters")
    @NotBlank(message = "First Name  Should not be Blank")
    private String firstname;
    @Size(min = 1,max = 10,message = "Last Name Should be atleast 1 to 10 Characters")
    @NotBlank(message = "Last Name Should not be Blank")
    private String lastname;
    @NotBlank(message = "Gender Should not be Blank")
    private String gender;
    @Pattern(regexp="(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])",message = "Date is not in correct form")
    private String dob;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}
