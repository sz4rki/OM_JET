package pl.edu.vistula.fullstacktest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import pl.edu.vistula.dataobject.ExpDateValidator;
import pl.edu.vistula.dataobject.TestDataGenerator;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class FullstackTestApplicationTests {

    private WebDriver driver;
    private static GenericContainer<?> webAppContainer;
    private static String containerUrl;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    @BeforeEach
    public void setupTest() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        webAppContainer = new GenericContainer<>(DockerImageName.parse("fullstacktest:latest"))
                .withExposedPorts(8080);
        webAppContainer.start();
        containerUrl = "http://" + webAppContainer.getHost() + ":" + webAppContainer.getMappedPort(8080);


        email = TestDataGenerator.generateRandomEmail();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomName();
        lastName = TestDataGenerator.generateRandomName();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get(containerUrl);
            WebElement goRegisterButton = driver.findElement(By.id("goregister"));
            goRegisterButton.click();

            assertTrue(driver.getCurrentUrl().contains("/rejestracja"));

            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("password")).sendKeys(password);
            driver.findElement(By.id("confirm-password")).sendKeys(password);
            driver.findElement(By.id("first-name")).sendKeys(firstName);
            driver.findElement(By.id("last-name")).sendKeys(lastName);

            WebElement registerButton = driver.findElement(By.id("register"));
            registerButton.click();

            wait.until(ExpectedConditions.titleIs("Strona Logowania"));

            driver.findElement(By.id("email")).sendKeys(email);
            driver.findElement(By.id("password")).sendKeys(password);

            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Zaloguj się']"));
            loginButton.click();

            wait.until(ExpectedConditions.titleIs("Aktywne Bilety"));

            String pageTitle = driver.getTitle();
            assertEquals("Aktywne Bilety", pageTitle);

        } catch (Exception e) {
            fail("Test rejestracji i logowania nie powiódł się: " + e.getMessage());
        }
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
        if (webAppContainer != null) {
            webAppContainer.stop();
        }
    }

    @Test
    public void testBuyingTickets() {
        driver.get(containerUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String[] ticketTypes = {"ulgowy", "normalny", "dzienny", "miesieczny"};
        Random random = new Random();

        String randomTicketType = ticketTypes[random.nextInt(ticketTypes.length)];
        int randomQuantity = random.nextInt(5) + 1;

        WebElement shopButton = driver.findElement(By.id("goshop"));
        shopButton.click();

        WebElement ticketTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[contains(., 'Bilet godzinny ulgowy')]")));
        ticketTypeDropdown.click();

        WebElement ticketTypeOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//option[contains(text(), '" + randomTicketType + "')]")));
        ticketTypeOption.click();

        WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='number']")));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(randomQuantity));

        WebElement buyButton = driver.findElement(By.id("buybutton"));
        buyButton.click();

        wait.until(ExpectedConditions.titleIs("Aktywne Bilety"));

        for (int i = 1; i <= randomQuantity; i++) {
            WebElement ticketDiv = driver.findElement(By.xpath("(//div[contains(@class, 'slot')])[" + i + "]"));
            assertNotNull(ticketDiv);
        }
    }

    @Test
    public void testBuyingAndValidatingTickets() {
        driver.get(containerUrl);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        String[] ticketTypes = {"ulgowy", "normalny", "dzienny", "miesieczny"};
        Random random = new Random();

        String randomTicketType = ticketTypes[random.nextInt(ticketTypes.length)];
        int randomQuantity = random.nextInt(5) + 1;

        WebElement shopButton = driver.findElement(By.id("goshop"));
        shopButton.click();

        WebElement ticketTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[contains(., 'Bilet godzinny ulgowy')]")));
        ticketTypeDropdown.click();

        WebElement ticketTypeOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//option[contains(text(), '" + randomTicketType + "')]")));
        ticketTypeOption.click();

        WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='number']")));
        quantityInput.clear();
        quantityInput.sendKeys(String.valueOf(randomQuantity));

        WebElement buyButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Kup bilet')]")));
        buyButton.click();

        wait.until(ExpectedConditions.titleIs("Aktywne Bilety"));

        for (int i = 1; i <= randomQuantity; i++) {
            WebElement ticketDiv = driver.findElement(By.xpath("(//div[contains(@class, 'slot')])[" + i + "]"));
            assertNotNull(ticketDiv);
        }

        WebElement firstTicket = driver.findElement(By.xpath("(//div[contains(@class, 'slot')])[1]"));
        assertNotNull(firstTicket);

        WebElement firstTicketExpiration = firstTicket.findElement(By.xpath(".//div[contains(text(), 'Data wygaśnięcia biletu')]//following-sibling::span"));
        String expirationText = firstTicketExpiration.getText();
        assertNotNull(expirationText);

        try {
            ExpDateValidator.validateTicketExpiration(expirationText, randomTicketType);
        } catch (Exception e) {
            fail("Ticket expiration validation failed: " + e.getMessage());
        }
    }

    @Test
    public void testUserProfile() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement profileButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, '/profilUżytkownika')]")));
        profileButton.click();
        wait.until(ExpectedConditions.titleIs("Profil Użytkownika"));

        WebElement firstNameInput = driver.findElement(By.id("first-name"));
        WebElement lastNameInput = driver.findElement(By.id("last-name"));
        WebElement emailInput = driver.findElement(By.id("email"));

        assertEquals(firstName, firstNameInput.getAttribute("value"));
        assertEquals(lastName, lastNameInput.getAttribute("value"));
        assertEquals(email, emailInput.getAttribute("value"));
    }

    @Test
    public void testPasswordChange() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(containerUrl);

        WebElement profileButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, '/profilUżytkownika')]")));
        profileButton.click();

        wait.until(ExpectedConditions.titleIs("Profil Użytkownika"));

        String newPassword = "NoweHaslo123";

        driver.findElement(By.id("new-password")).sendKeys(newPassword);
        driver.findElement(By.id("repeat-password")).sendKeys(newPassword);

        WebElement passwordButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Zresetuj hasło')]")));
        passwordButton.click();

        wait.until(ExpectedConditions.titleIs("Strona Logowania"));

        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(newPassword);

        WebElement loginButton = driver.findElement(By.xpath("//button[text()='Zaloguj się']"));
        loginButton.click();

        wait.until(ExpectedConditions.titleIs("Aktywne Bilety"));

        String pageTitle = driver.getTitle();
        assertEquals("Aktywne Bilety", pageTitle);
    }

    @Test
    public void testRegistrationWithExistingEmail() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        driver.get(containerUrl);

        WebElement profileButton = wait.until(ExpectedConditions.visibilityOfElementLocated
                (By.xpath("//a[contains(@href, '/profilUżytkownika')]")));
        profileButton.click();
        wait.until(ExpectedConditions.titleIs("Profil Użytkownika"));

        WebElement passwordButton = wait.until(ExpectedConditions.elementToBeClickable
                (By.xpath("//button[contains(text(), 'Wyloguj się')]")));
        passwordButton.click();

    try {
        wait.until(ExpectedConditions.titleIs("Strona Logowania"));

        WebElement goRegisterButton = driver.findElement(By.id("goregister"));
        goRegisterButton.click();

        assertTrue(driver.getCurrentUrl().contains("/rejestracja"));

        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("confirm-password")).sendKeys(password);
        driver.findElement(By.id("first-name")).sendKeys(firstName);
        driver.findElement(By.id("last-name")).sendKeys(lastName);

        WebElement registerButton = driver.findElement(By.id("register"));
        registerButton.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Podany użytkownik już istnieje!", alert.getText());
        alert.accept();
    } catch (Exception e) {
        fail("Test rejestracji z istniejącym emailem nie powiódł się: " + e.getMessage());
    }
}
    @Test
    public void testVerifyLoginCookie() {
        Cookie loginCookie = driver.manage().getCookieNamed("login");
        assertNotNull(loginCookie, "Nie znaleziono ciasteczka 'login'");
        assertEquals(email, loginCookie.getValue(), "Ciasteczko 'login' nie nazywa się tak samo jak email użytkownika");
    }

}

