package it.uniroma3.cashlytics.Config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		// Log dell'accesso negato
		String username = request.getRemoteUser();
		String uri = request.getRequestURI();

		System.out.println("Access denied for user: " + username + " on URI: " + uri);

		// Redirect alla pagina di errore 403
		response.sendRedirect(request.getContextPath() + "/error/403");
	}

}
