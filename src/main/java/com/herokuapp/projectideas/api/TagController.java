package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.tag.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagController {

    @Autowired
    Database database;

    @GetMapping("/api/tags/standard/idea")
    public List<String> getStandardIdeaTags() {
        return Arrays.asList(Tag.STANDARD_IDEA_TAGS);
    }

    @PostMapping("/api/tags/new/idea")
    public void addIdeaTag(@RequestBody String tagName) {
        Optional<Tag> existingTag = database.getTag(tagName, Tag.Type.Idea);
        if (existingTag.isPresent()) {
            database.incrementTagUsages(tagName, Tag.Type.Idea);
        } else {
            database.createTag(new Tag(tagName, Tag.Type.Idea));
        }
    }
}
