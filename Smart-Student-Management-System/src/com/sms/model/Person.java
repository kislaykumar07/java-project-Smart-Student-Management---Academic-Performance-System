package com.sms.model;

/**
 * Abstract base class (Unit 2: Abstract Class & Method, Encapsulation).
 * All fields are private and exposed through getters/setters.
 */
public abstract class Person {

    private final String id;
    private String name;
    private int age;
    private String email;

    protected Person(String id, String name, int age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    /** Every concrete Person must describe its role. */
    public abstract String getRole();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getRole() + " [" + id + "] " + name + ", age " + age + ", " + email;
    }
}
