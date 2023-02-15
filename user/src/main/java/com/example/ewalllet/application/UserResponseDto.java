package com.example.ewalllet.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class UserResponseDto {


    private String name;

    private String username;

    private String mobNo;

    private String email;

}
