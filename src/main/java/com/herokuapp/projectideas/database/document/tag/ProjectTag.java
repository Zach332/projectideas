package com.herokuapp.projectideas.database.document.tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProjectTag extends Tag {

    public ProjectTag(String name) {
        super(name);
        this.type = "ProjectTag";
    }

    public static String[] STANDARD_TAGS = {
        "website",
        "app",
        "python",
        "java",
        "c#",
    };
}
