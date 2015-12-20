package ru.beywer.home.mobi3.lib;

import java.util.UUID;

public class User {

    private String login;
    private String password;
    private String name;
    private String surname;
    private String fatherName;

    public User(){
        this.login = UUID.randomUUID().toString();
    }

    public User(String name, String surname, String fatherName, String login, String password){
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.fatherName = fatherName;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }
}
