package pl.edu.vistula.dataobject;

public record RegisterRequest(String email, String password, String firstName, String lastName) {
}
