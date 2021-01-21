package com.herokuapp.projectideas.database.document.tag;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Tag {

    protected String id;
    protected String name;
    protected int usages;
    protected Type type;
    protected boolean standard;

    public Tag(String name, Type type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.usages = 1;
        this.type = type;
        this.standard = false;
    }

    public static enum Type {
        Project,
        Idea,
    }

    public static String[] STANDARD_IDEA_TAGS = {
        "simple",
        "complex",
        "website",
        "iot",
        "app",
    };

    public static String[] STANDARD_PROJECT_TAGS = {
        "website",
        "app",
        "python",
        "java",
        "c#",
    };
}
