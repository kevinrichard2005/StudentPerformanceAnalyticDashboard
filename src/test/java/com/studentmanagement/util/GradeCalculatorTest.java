package com.studentmanagement.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GradeCalculatorTest {

    @ParameterizedTest(name = "{0} marks -> {1} ({2})")
    @CsvSource({
            "100, O, Excellent",
            "90, O, Excellent",
            "89.9, A+, Outstanding",
            "80, A+, Outstanding",
            "79, A, Very Good",
            "70, A, Very Good",
            "69, B+, Good",
            "60, B+, Good",
            "59, B, Average",
            "50, B, Average",
            "49, C, Pass",
            "40, C, Pass",
            "39.9, F, Fail",
            "0, F, Fail"
    })
    void evaluatesGradeBoundaries(double marks, String grade, String result) {
        GradeCalculator.GradeResult r = GradeCalculator.evaluate(marks);
        assertThat(r.grade()).isEqualTo(grade);
        assertThat(r.result()).isEqualTo(result);
    }

    @Test
    void rejectsOutOfRangeMarks() {
        assertThatThrownBy(() -> GradeCalculator.evaluate(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GradeCalculator.evaluate(100.1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void passThresholdIsForty() {
        assertThat(GradeCalculator.isPass(40)).isTrue();
        assertThat(GradeCalculator.isPass(39.99)).isFalse();
    }
}
