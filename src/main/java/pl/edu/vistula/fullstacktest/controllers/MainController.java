package pl.edu.vistula.fullstacktest.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import pl.edu.vistula.dataobject.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class MainController {
    private final Set<RegisteredUser> users = new HashSet<>();
    private final List<Ticket> tickets = new LinkedList<>();

    private void simulateLoad() {
        var latency = new Random().nextInt(1000);

        try {
            TimeUnit.MILLISECONDS.sleep(200 + latency);
        } catch (Exception ignored) {}
    }

    @GetMapping("/rejestracja")
    public String register() {
        simulateLoad();
        return "register";
    }

    @GetMapping("/logowanie")
    public String login() {
        simulateLoad();
        return "login";
    }

    @GetMapping("/biletomat")
    public String shop() {
        simulateLoad();
        return "shop";
    }

    @GetMapping("/profilUżytkownika")
    public String profile(@CookieValue(value = "login", defaultValue="") String cookieValue, Model model)  {
        simulateLoad();
        RegisteredUser currentUser = users.stream().filter(user -> user.getEmail().equals(cookieValue)).findFirst().orElse(null);
        model.addAttribute("firstName", currentUser.getFirstName());
        model.addAttribute("lastName", currentUser.getLastName());
        model.addAttribute("email", currentUser.getEmail());
        return "profile";
    }

    @GetMapping("/")
    public String mainPage(@CookieValue(value = "login", defaultValue="") String cookieValue, Model model) {
        simulateLoad();
        boolean isUserLoggedIn = users.stream().anyMatch(user -> user.getEmail().equals(cookieValue));

        if(!isUserLoggedIn) {
            return "redirect:/logowanie";
        }
        List<String> remainingTimes = tickets.stream()
                .map(Ticket::remainingTime)
                .collect(Collectors.toList());

        model.addAttribute("tickets", tickets);
        model.addAttribute("remainingTimes", remainingTimes);

        return "index";
    }

    @GetMapping("/wylogowanie")
    public String wylogowanie(HttpServletResponse httpServletResponse) {
        simulateLoad();
        Cookie cookie = new Cookie("login", "");
        httpServletResponse.addCookie(cookie);
        return "redirect:/logowanie";
    }

    @PostMapping("/logowanie")
    public String loginPost(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        simulateLoad();
        Optional<RegisteredUser> registeredUser = users.stream()
                .filter(user -> user.getEmail().equals(loginRequest.email()))
                .findFirst();

        if (registeredUser.isPresent()) {
            RegisteredUser user = registeredUser.get();
            if (user.getPassword().equals(loginRequest.password())) {
                Cookie cookie = new Cookie("login", loginRequest.email());
                httpServletResponse.addCookie(cookie);
                return "redirect:/";
            }
        }

        return "redirect:/logowanie";
    }

    @PostMapping("/rejestracja")
    public String rejestracja(@RequestBody RegisterRequest registerRequest) {
        simulateLoad();

        boolean isRegistered = users.stream().anyMatch(user -> user.getEmail().equals(registerRequest.email()));
        if(isRegistered) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "Użytkownik już istnieje!");
        }

        RegisteredUser user = new RegisteredUser();
        user.setEmail(registerRequest.email());
        user.setPassword(registerRequest.password());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());
        users.add(user);
        return "redirect:/logowanie";
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleResourceNotFoundException(HttpClientErrorException ex) {
        return new ResponseEntity<>(ex.getMessage(), ex.getStatusCode());
    }

    @PostMapping("/biletomat")
        public String shopPost(@RequestBody ShopRequest shopRequest) {
        simulateLoad();
        Ticket ticket = new Ticket(shopRequest.ticketType(), Instant.now().toEpochMilli(), Instant.now().plusSeconds(shopRequest.ticketType().getExpirationTime()).toEpochMilli(), shopRequest.quantity());
        for(int i = 0; i < shopRequest.quantity(); i++) {
            tickets.add(ticket);
        }
            return "redirect:/";
        }

    @PostMapping("/profilUżytkownika")
    public String profilePost(@RequestBody NewPassword newPassword, @CookieValue(value = "login", defaultValue="") String cookieValue, HttpServletResponse httpServletResponse) {
        simulateLoad();
        RegisteredUser currentUser = users.stream().filter(user -> user.getEmail().equals(cookieValue)).findFirst().orElse(null);
        currentUser.setPassword(newPassword.newPassword());

        return "redirect:/logowanie";
    }
}
