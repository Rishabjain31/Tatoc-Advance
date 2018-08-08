package tatoc_adv;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class Tatoc_AdvanceCourse {
	static final String DB_URL = "jdbc:mysql://10.0.1.86/tatoc";

	//Database credentials
	static final String USER = "tatocuser";
	static final String PASS = "tatoc01";

	public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException, IOException{             

		System.setProperty("webdriver.chrome.driver", "C:\\Users\\rishabh.jain1\\Documents\\chromedriver.exe");
		WebDriver driver=new ChromeDriver();
		driver.get("http://10.0.1.86/tatoc/advanced/hover/menu");

	//1st part

		Actions action = new Actions(driver);
		WebElement menu2 = driver.findElement(By.xpath("/html/body/div/div[2]/div[2]"));
		action.moveToElement(menu2);
		WebElement gonext=driver.findElement(By.xpath("/html/body/div/div[2]/div[2]/span[5]"));
		action.moveToElement(gonext);
		action.click().build().perform();

	//2nd part

		String symbol=driver.findElement(By.cssSelector("#symbol")).getAttribute("value");
		System.out.println(symbol);
		Connection con = null;
		Statement stmt = null;

		Class.forName("com.mysql.jdbc.Driver");
		System.out.println("Connecting to a selected database...");
		con = DriverManager.getConnection(DB_URL, USER, PASS);
		System.out.println("Connected database successfully...");

		System.out.println("Creating statement...");
		stmt = con.createStatement();

		ResultSet rs=stmt.executeQuery("select id,symbol from identity where symbol= '"+symbol+"'");
		int id=0;
		while(rs.next())
		{
			id=rs.getInt("id");
			System.out.print("id="+id);
			System.out.println(", symbol="+rs.getString("symbol"));
		}
		rs.close();
		System.out.println("Corresponding name and password : ");
		ResultSet rs1= stmt.executeQuery("select name,passkey from credentials where id='"+id+"'");
		while(rs1.next())
		{
			String name=rs1.getString("name");
			driver.findElement(By.cssSelector("#name")).sendKeys(name);
			System.out.println("Name : "+name);
			String pwd=rs1.getString("passkey");
			driver.findElement(By.cssSelector("#passkey")).sendKeys(pwd);
			System.out.println("Passkey : "+pwd);

		}
		rs1.close();
		con.close();
		driver.findElement(By.cssSelector("#submit")).click();        

	//3rd part

//		JavascriptExecutor js = (JavascriptExecutor) driver;  
//		Thread.sleep(4000);
//		js.executeScript("document.getElementsByClassName('video')[0].getElementsByTagName('object')[0].playMovie();");
//		Thread.sleep(36000);
//		driver.findElement(By.linkText("Proceed")).click(); 
		((JavascriptExecutor)driver).executeScript("window.played = true;");
        driver.findElement(By.linkText("Proceed")).click();
	

	//4th part

		String session=driver.findElement(By.cssSelector("#session_id")).getText();
		String[] session_id=session.split(": ");
		System.out.println("Session id = "+ session_id[1]);

		//Rest Service to generate token	    	
		URL geturl = new URL("http://10.0.1.86/tatoc/advanced/rest/service/token/" + session_id[1]);
		HttpURLConnection getconn = (HttpURLConnection) geturl.openConnection();
		getconn.setRequestMethod("GET");
		getconn.setRequestProperty("Accept", "application/json");

		if (getconn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ getconn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((getconn.getInputStream())));
		System.out.println(br);
		String output;
		String restful = new String();
		while ((output = br.readLine()) != null) {
			restful=restful.concat(output);
		}
		br.close();
		System.out.println(restful);
		String response[]= restful.split(":\"");
		String token[]= response[1].split("\"");
		String jsonToken= token[0];
		System.out.println("jsontoken: "+ jsonToken);

		//POST BODY: id=[Session ID], signature=[Token], allow_access=1
		//Rest Service to register for access		     
		try {

			URL posturl = new URL("http://10.0.1.86/tatoc/advanced/rest/service/register");
			HttpURLConnection postconn = (HttpURLConnection) posturl.openConnection();
			postconn.setDoOutput(true);

			System.out.println("\nSending 'POST' request to URL : " + posturl);
			postconn.setRequestMethod("POST");
			postconn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String input = "id="+session_id[1]+"& signature="+jsonToken+"&allow_access=1";
			System.out.println("Post parameters : " + input);

			DataOutputStream wr = new DataOutputStream(postconn.getOutputStream());
			wr.writeBytes(input);
			wr.flush();
			wr.close();

			int responseCode = postconn.getResponseCode();
			System.out.println("Response Code : " + responseCode); 
			System.out.println(postconn.getResponseMessage());

			postconn.disconnect();
			Thread.sleep(5000);
			driver.findElement(By.partialLinkText("Proceed")).click();     
		}
		catch(Exception e) {
			System.out.println("exception: "+ e);
		}

	//5th part

		driver.findElement(By.linkText("Download File")).click();
		Thread.sleep(5000);
		File file = new File("C:\\Users\\rishabh.jain1\\Downloads\\file_handle_test.dat");

		FileInputStream fileInput = null;
		try {
			fileInput = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Properties prop = new Properties();

		//load properties file
		try {
			prop.load(fileInput);
		} catch (IOException e) {
			e.printStackTrace();
		}

		driver.findElement(By.id("signature")).sendKeys(prop.getProperty("Signature"));
		driver.findElement(By.className("submit")).click();
		System.out.println("property: "+prop.getProperty("Signature"));

		driver.close();
   }
}



