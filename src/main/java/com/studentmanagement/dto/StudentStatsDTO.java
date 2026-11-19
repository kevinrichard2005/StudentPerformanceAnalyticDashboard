package com.studentmanagement.dto;

import java.util.Map;

public class StudentStatsDTO {

    private long totalStudents;
    private long maleStudents;
    private long femaleStudents;
    private double averageMarks;
    private long passedStudents;
    private long failedStudents;
    private double passPercentage;
    private Map<String, Long> gradeDistribution;
    private Map<String, Long> departmentDistribution;
    private Map<String, Long> genderDistribution;
    private Map<String, Long> enrollmentTrend;

    public Map<String, Long> getEnrollmentTrend() { return enrollmentTrend; }
    public void setEnrollmentTrend(Map<String, Long> enrollmentTrend) { this.enrollmentTrend = enrollmentTrend; }

    // Getters and Setters
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getMaleStudents() { return maleStudents; }
    public void setMaleStudents(long maleStudents) { this.maleStudents = maleStudents; }
    public long getFemaleStudents() { return femaleStudents; }
    public void setFemaleStudents(long femaleStudents) { this.femaleStudents = femaleStudents; }
    public double getAverageMarks() { return averageMarks; }
    public void setAverageMarks(double averageMarks) { this.averageMarks = averageMarks; }
    public long getPassedStudents() { return passedStudents; }
    public void setPassedStudents(long passedStudents) { this.passedStudents = passedStudents; }
    public long getFailedStudents() { return failedStudents; }
    public void setFailedStudents(long failedStudents) { this.failedStudents = failedStudents; }
    public double getPassPercentage() { return passPercentage; }
    public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }
    public Map<String, Long> getGradeDistribution() { return gradeDistribution; }
    public void setGradeDistribution(Map<String, Long> gradeDistribution) { this.gradeDistribution = gradeDistribution; }
    public Map<String, Long> getDepartmentDistribution() { return departmentDistribution; }
    public void setDepartmentDistribution(Map<String, Long> departmentDistribution) { this.departmentDistribution = departmentDistribution; }
    public Map<String, Long> getGenderDistribution() { return genderDistribution; }
    public void setGenderDistribution(Map<String, Long> genderDistribution) { this.genderDistribution = genderDistribution; }
}
