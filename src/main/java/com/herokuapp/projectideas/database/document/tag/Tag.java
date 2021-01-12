package com.herokuapp.projectideas.database.document.tag;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Tag {

    protected String id;
    protected String name;
    protected int usages;
    protected Type type;
    protected boolean standard;

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
}
