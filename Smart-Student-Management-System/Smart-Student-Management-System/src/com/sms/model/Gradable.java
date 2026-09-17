package com.sms.model;

/**
 * Interface implemented by anything that can be graded (Unit 2: Java Interfaces).
 * Contains an abstract method and a default method.
 */
public interface Gradable {

    double getPercentage();

    LetterGrade getLetterGrade();

    /** Default method: a record passes when the letter grade is not F. */
    default boolean isPass() {
        return getLetterGrade() != LetterGrade.F;
    }
}
