package com.herokuapp.projectideas.database.document.tag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class IdeaTag extends Tag {

    public IdeaTag(String name) {
        super(name);
        this.type = "IdeaTag";
    }

    public static String[] STANDARD_TAGS = {
        "simple",
        "complex",
        "website",
        "iot",
        "app",
    };
}
