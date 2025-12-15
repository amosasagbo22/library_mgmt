package com.example.library;

public class Member {
    private String id;
    private String name;
    private String membershipNumber;
    private String password;

    public Member() {}

    public Member(String id, String name, String membershipNumber, String password) {
        this.id = id;
        this.name = name;
        this.membershipNumber = membershipNumber;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMembershipNumber() { return membershipNumber; }
    public void setMembershipNumber(String membershipNumber) { this.membershipNumber = membershipNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}