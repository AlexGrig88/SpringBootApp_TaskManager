package com.alexgrig.education.springboottasks.business.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySearchValues {  //возможные значения по которым можно искать категории

    private String title;
    private String email;
}
