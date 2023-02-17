package com.example.capstone.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User  implements Serializable {
    @Id
    @Email(message = "Email  must be in Correct form")
    @NotBlank(message = "Email Should not be Blank")
    private String email;
    @Size(min = 3,max = 16,message = "First Name Length must be 3 to 16 Characters")
    @NotBlank(message = "First Name  Should not be Blank")
    private String firstname;
    @Size(min = 1,max = 10,message = "Last Name Should be atleast 1 to 10 Characters")
    private String lastname;

    @Size(min=8,max = 30,message = "Password length Must be upto 8 to 16 Characters")
    private String password;
    private String gender;

    @Pattern(regexp="(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])",message = "Date is not in correct form")
    private String dob;

    private String provider="LOCAL";
    private String role="USER";
    private Boolean enabled = false;
    private String verifyotp;
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<String> friendrequest = new ArrayList<>();

    private ArrayList<String> categories;

    public User(String email, String firstname, String lastname, String password, String gender, String dob) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.gender = gender;
        this.dob = dob;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
