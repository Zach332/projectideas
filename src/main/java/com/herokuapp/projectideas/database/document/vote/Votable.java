package com.herokuapp.projectideas.database.document.vote;

public interface Votable {
    public void addUpvote();

    public void removeUpvote();

    public long getTimeCreated();

    public int getUpvoteCount();
}
