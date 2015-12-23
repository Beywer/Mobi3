package ru.beywer.home.mobi3.lib;

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
    private MeetPriority meetPriority;

    public Meet(){
        name = null;
        description = null;
        owner = null;
        start = null;
        end = null;

        meetPriority = MeetPriority.LOW;
        this.participants = new ArrayList<>();
    }

    public Meet(String name, User owner, Date start, Date end) {
        this.name = name;
        this.owner = owner;
        this.start = start;
        this.end = end;

        meetPriority = MeetPriority.LOW;
        this.participants = new ArrayList<>();
    }

    //GETTERS SETTERS BLAH BLAH BLAH

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

    public void setStart(long startMillis) {
        this.start = new Date(startMillis);
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setEnd(long endMillis) {
        this.end = new Date(endMillis);
    }

    public MeetPriority getMeetPriority() {
        return meetPriority;
    }

    public void setMeetPriority(MeetPriority meetPriority) {
        this.meetPriority = meetPriority;
    }
}
