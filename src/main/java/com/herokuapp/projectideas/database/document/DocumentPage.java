package com.herokuapp.projectideas.database.document;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class DocumentPage<T extends RootDocument> {

    private List<T> documents;
    private boolean lastPage;

    public DocumentPage(List<T> documents, boolean lastPage) {
        this.documents = documents;
        this.lastPage = lastPage;
    }
}
