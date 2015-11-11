package ru.beywer.mobi3.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meet {

    private String name;
    private String description;
    private User owner;
    private List<User> participants;
    private Date start;
    private Date end;

    public Meet(String name, User owner, Date start, Date end) {
        this.name = name;
        this.owner = owner;
        this.start = start;
        this.end = end;

        this.participants = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
