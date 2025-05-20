package it.uniroma3.cashlytics.cashlytics;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;

@SpringBootApplication
public class CashlyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashlyticsApplication.class, args);
	}
 @Bean
public ApplicationRunner logResolvers(List<ViewResolver> resolvers) {
    return args -> {
        System.out.println("===== ViewResolvers in ordine =====");
        resolvers.forEach(r -> System.out.println("  " + r.getClass().getName()));
    };
}

}
