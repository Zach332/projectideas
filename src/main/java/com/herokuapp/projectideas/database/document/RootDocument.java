package com.herokuapp.projectideas.database.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RootDocument {
    public String getId();

    public String getType();

    @JsonIgnore
    public String getPartitionKey();
}
