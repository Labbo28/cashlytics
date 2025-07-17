package it.uniroma3.cashlytics.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UnauthorizedAccessException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleUnauthorizedAccessException(UnauthorizedAccessException ex, Model model,
			HttpServletRequest request) {
		logger.warn("Unauthorized access attempt: {}", ex.getMessage());
		model.addAttribute("errorMessage", "Non sei autorizzato ad accedere a questa risorsa.");
		model.addAttribute("errorCode", "403");
		model.addAttribute("requestUrl", request.getRequestURI());
		return "error/403";
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleAccessDeniedException(AccessDeniedException ex, Model model, HttpServletRequest request) {
		logger.warn("Access denied: {}", ex.getMessage());
		model.addAttribute("errorMessage", "Accesso negato. Non hai i permessi necessari.");
		model.addAttribute("errorCode", "403");
		model.addAttribute("requestUrl", request.getRequestURI());
		return "error/403";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGenericException(Exception ex, Model model, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

		// For AJAX requests or API calls, return error page
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader != null && acceptHeader.contains("application/json")) {
			model.addAttribute("errorMessage", "Si è verificato un errore interno del server.");
			model.addAttribute("errorCode", "500");
			return "error/500";
		}

		// For regular requests, redirect with flash message
		redirectAttributes.addFlashAttribute("errorMessage",
				"Si è verificato un errore imprevisto. Riprova più tardi.");

		// Try to redirect to a safe page
		String referer = request.getHeader("Referer");
		if (referer != null && referer.contains("/dashboard")) {
			return "redirect:" + extractUserDashboard(referer);
		}

		return "redirect:/login";
	}

	private String extractUserDashboard(String referer) {
		// Extract username from referer URL to redirect to user dashboard
		try {
			String[] parts = referer.split("/");
			for (int i = 0; i < parts.length - 1; i++) {
				if ("dashboard".equals(parts[i + 1])) {
					return "/" + parts[i] + "/dashboard";
				}
			}
		} catch (Exception e) {
			logger.debug("Could not extract username from referer: {}", referer);
		}
		return "/login";
	}
}