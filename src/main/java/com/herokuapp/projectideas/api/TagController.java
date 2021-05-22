package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.tag.IdeaTag;
import com.herokuapp.projectideas.database.document.tag.ProjectTag;
import com.herokuapp.projectideas.search.SearchController;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class TagController {

    @Autowired
    Database database;

    @Autowired
    SearchController searchController;

    @GetMapping("/api/tags/standard/idea")
    public List<String> getStandardIdeaTags() {
        return Arrays.asList(IdeaTag.STANDARD_TAGS);
    }

    @GetMapping("/api/tags/standard/project")
    public List<String> getStandardProjectTags() {
        return Arrays.asList(ProjectTag.STANDARD_TAGS);
    }

    @GetMapping("/api/tags/suggested/idea")
    public List<String> getSuggestedIdeaTags(
        @RequestParam("search") String search
    ) {
        return searchController.searchForIdeaTags(search);
    }

    @GetMapping("/api/tags/suggested/project")
    public List<String> getSuggestedProjectTags(
        @RequestParam("search") String search
    ) {
        return searchController.searchForProjectTags(search);
    }
}
