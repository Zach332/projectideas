package com.herokuapp.projectideas.database.document.tag;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.herokuapp.projectideas.database.document.RootDocument;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({ @Type(IdeaTag.class), @Type(ProjectTag.class) })
public abstract class Tag implements RootDocument {

    protected String id;
    protected String type;
    protected String name;
    protected int usages;
    protected boolean standard;

    public Tag(String name) {
        this.id = UUID.randomUUID().toString();
        this.type = "Tag";
        this.name = name;
        this.usages = 1;
        this.standard = false;
    }

    public String getPartitionKey() {
        return name;
    }
}
