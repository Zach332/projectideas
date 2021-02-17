package com.herokuapp.projectideas.database.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RootDocument {
    @JsonIgnore
    public String getPartitionKey();
}
