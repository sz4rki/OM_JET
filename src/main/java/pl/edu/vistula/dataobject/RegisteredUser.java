package pl.edu.vistula.dataobject;

import lombok.Data;

@Data
public class RegisteredUser {
    String email;
    String password;
    String firstName;
    String lastName;
}
