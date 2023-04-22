package mmo.youtube.changepassemail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChangePassEmailApplication {
	public static final String filePath = "gmail.txt";
	public static final int numberThreads = 5;

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ChangePassEmailApplication.class, args);
		// Khởi tạo ChromeDriver
		System.setProperty("webdriver.chrome.driver", "chromedriver_win32\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--disable-notifications");

		// Đọc file chứa email và mật khẩu
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;

		List<String> data = new ArrayList<String>();
		// Tạo ThreadPool với kích thước 5
		ExecutorService executorService = Executors.newFixedThreadPool(numberThreads);

		while ((line = br.readLine()) != null) {
			line = line.replaceAll("\\s+", " "); // xóa các khoảng trắng dư thừa và giữ lại một dấu cách
			String[] parts = line.split(" "); // tách email và password ra từ chuỗi dữ liệu
			String email = parts[0];
			String password = parts[1];
			String emailRecovery = parts[2];

			// Tạo một Runnable mới với email và mật khẩu tương ứng
			Runnable worker = new WorkerThread(email, password, emailRecovery, options, data);
			executorService.execute(worker);
		}
		br.close();
	}

}

class WorkerThread implements Runnable {
	private final String email;
	private final String password;
	private final String emailRecovery;
	private final ChromeOptions options;
	private final List<String> data;
	public final int fiveSeconds = 7000;
	public final int tenSeconds = 15000;
	public final int threeseconds = 5000;
	public final int twoseconds = 2000;
	private final String newPassword = "Vule1234@";

	public WorkerThread(String email, String password, String emailRecovery, ChromeOptions options, List<String> data) {
		this.email = email;
		this.password = password;
		this.emailRecovery = emailRecovery;
		this.options = options;
		this.data = data;
	}

	@Override
	public void run() {
		WebDriver driver = new ChromeDriver(options);
		Duration duration = Duration.ofSeconds(120);
		WebDriverWait wait = new WebDriverWait(driver, duration);
		try {
			driver.get("https://accounts.google.com/");

			// Điền email và mật khẩu vào các phần tử input tương ứng
			try {
				WebElement emailInput = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
				emailInput.sendKeys(email);
				WebElement nextButton = driver.findElement(By.xpath("//div[@id='identifierNext']"));
				nextButton.click();
				Thread.sleep(fiveSeconds); // đợi 5 giây
				WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
				passwordInput.sendKeys(password);
				WebElement signInButton = driver.findElement(By.xpath("//div[@id='passwordNext']"));
				signInButton.click();
				try {
					Thread.sleep(twoseconds); // đợi5 giây
					WebElement alert_error_password = driver.findElement(By.xpath(
							"//span[contains(text(), 'Mật khẩu của bạn đã thay đổi') or contains(text(), 'Your password was changed')]"));
					System.out.println(alert_error_password.getText());
					if (alert_error_password.isDisplayed()) {
						String st = email + " " + password + " " + emailRecovery;
						data.add(st);
						driver.quit();
					}
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
				String st = email + " " + password + " " + emailRecovery;
				data.add(st);
				driver.quit();
			}

			try {
				Thread.sleep(fiveSeconds); // đợi 5 giây
				WebElement confirm = driver.findElement((By.xpath("//input[@id='confirm']")));
				confirm.click();
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
			}

			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//a[@aria-label='Các ứng dụng của Google' or @aria-label='Google apps']")));
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
				String st = email + " " + password + " " + emailRecovery;
				data.add(st);
				driver.quit();
			}

			try {
				List<WebElement> Security = driver.findElements(By.xpath(
						"//a[@href='security']//div[contains(text(), 'Bảo mật') or contains(text(), 'Security')]"));
				Security.get(1).click();
				WebElement passBtn = wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//a[@aria-label='Mật khẩu' or @aria-label='Password']")));

				// Tạo đối tượng Actions
				Actions actions = new Actions(driver);
				actions.moveToElement(passBtn).perform();
				passBtn.click();

				try {
					Thread.sleep(tenSeconds);
					WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
					passwordInput.sendKeys(password);
					WebElement signInButton = driver.findElement(By.xpath("//div[@id='passwordNext']"));
					signInButton.click();
				} catch (Exception e) {
					WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
					passwordInput.clear();
					System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
				}
				WebElement newPasswordInput = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='password']")));
				newPasswordInput.sendKeys(newPassword);
				WebElement confirmation_password_input = driver
						.findElement(By.xpath("//input[@name='confirmation_password']"));
				confirmation_password_input.sendKeys(newPassword);
				WebElement change_password_btn = driver.findElement(By.xpath("//button[@type='submit']"));
				change_password_btn.click();
				Thread.sleep(fiveSeconds); // đợi 5 giây
				System.out.println("Change password successfully with Email: " + email);
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage() + ". " + e.getCause());
				String st = email + " " + password + " " + emailRecovery;
				data.add(st);
				driver.quit();
			}
			driver.quit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Set<String> setWithoutDuplicates = new HashSet<>(data);
			List<String> masterData = new ArrayList<>(setWithoutDuplicates);
			String fileName = "gmail_error.txt";

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
				for (String entry : masterData) {
					bw.write(entry);
					bw.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
