package com.herokuapp.projectideas.database.document;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class Tag {

    protected String name;
    protected int usages;
    protected Type type;
}

enum Type {
    Project,
    Idea,
}
