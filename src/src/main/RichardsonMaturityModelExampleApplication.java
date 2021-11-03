package src.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@EnableAutoConfiguration
public class RichardsonMaturityModelExampleApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(RichardsonMaturityModelExampleApplication.class, args);
	}
}