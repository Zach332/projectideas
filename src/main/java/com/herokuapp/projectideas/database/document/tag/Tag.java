package com.herokuapp.projectideas.database.document.tag;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.herokuapp.projectideas.database.document.RootDocument;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
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

    @Getter(AccessLevel.NONE)
    protected String id;

    protected String type;
    protected int usages;
    protected boolean standard;

    public Tag(String name) {
        // The tag id is url encoded to deal with special characters (e.g. #)
        this.id = URLEncoder.encode(name, StandardCharsets.UTF_8);
        this.usages = 1;
        this.standard = false;
    }

    public String getId() {
        return URLDecoder.decode(id, StandardCharsets.UTF_8);
    }

    public String getPartitionKey() {
        return type;
    }

    public void urlDecodeId() {
        id = URLDecoder.decode(id, StandardCharsets.UTF_8);
    }
}
