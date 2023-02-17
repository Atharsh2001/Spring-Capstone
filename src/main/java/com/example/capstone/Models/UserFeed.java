package com.example.capstone.Models;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeed  implements Serializable {
    @Id
    private String id;
    @CsvBindByPosition(position = 0)
    private String email;
    @CsvBindByPosition(position = 1)
    private String title;
    @CsvBindByPosition(position = 2)
    private String description;
    @CsvBindByPosition(position = 3)
    private String image;
    @CsvBindByPosition(position = 4)
    private ArrayList<String> tags;
    @CsvBindByPosition(position = 5)
    private String category;
    private String visiblity;
    private boolean available = true;

    public UserFeed(String email, String title, String description, String image, ArrayList<String> tags, String category, String visiblity) {
        this.email = email;
        this.title = title;
        this.description = description;
        this.image = image;
        this.tags = tags;
        this.category = category;
        this.visiblity = visiblity;
    }
}
