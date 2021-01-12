package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagController {

    @Autowired
    Database database;
}
