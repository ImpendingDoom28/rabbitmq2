package ru.itis.rabbitmq2.models;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PassportData {
        private String name;
        private String surname;
        private String passportId;
        private Integer age;
        private Date dateOfIssue;
        private String scan;
        private String citizenship;
}
