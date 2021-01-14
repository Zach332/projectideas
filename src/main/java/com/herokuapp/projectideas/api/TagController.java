package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.tag.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagController {

    @Autowired
    Database database;

    @GetMapping("/api/tags/standard/idea")
    public List<String> getStandardIdeaTags() {
        return Arrays.asList(Tag.STANDARD_IDEA_TAGS);
    }
}
