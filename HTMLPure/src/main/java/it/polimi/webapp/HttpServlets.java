package it.polimi.webapp;

import it.polimi.webapp.beans.User;
import it.polimi.webapp.pages.SellPage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class HttpServlets {

    private HttpServlets() {
    }

    public static <T extends HttpServlet> RequestDispatcher getDispatcherFor(ServletContext ctx, Class<? extends T> clazz) {
        var registration = ctx.getServletRegistrations().values().stream()
                .filter(s -> s.getClassName().equals(SellPage.class.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Servlet " + clazz + " is not configured"));
        return Objects.requireNonNull(
                ctx.getNamedDispatcher(registration.getName()),
                "Couldn't find dispatcher for class " + clazz + " named " + registration.getName());
    }

    public static String getParameterOr(HttpServletRequest req, String name, String fallback) {
        var v = req.getParameter(name);
        if(v == null)
            return fallback;
        return v;
    }

    @Contract("_, _, !null -> !null; _, _, _ -> _")
    public static @Nullable Integer getParameterOr(HttpServletRequest req, String name, @Nullable Integer fallback) {
        var v = req.getParameter(name);
        if(v == null)
            return fallback;

        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Contract("_, _, !null -> !null; _, _, _ -> _")
    public static @Nullable Double getParameterOr(HttpServletRequest req, String name, @Nullable Double fallback) {
        var v = req.getParameter(name);
        if(v == null)
            return fallback;

        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @Contract("_, _, !null -> !null; _, _, _ -> _")
    public static @Nullable LocalDateTime getParameterOr(HttpServletRequest req, String name, @Nullable LocalDateTime fallback) {
        var v = req.getParameter(name);
        if(v == null)
            return fallback;

        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }

    public static @Nullable InputStream getImageOrNull(HttpServletRequest req, String name) throws ServletException, IOException {
        Part articleImage = req.getPart(name);
        if(articleImage.getSize() == 0)
            return null;

        String imageName = articleImage.getSubmittedFileName();
        String mimeType = req.getServletContext().getMimeType(imageName);
        if(mimeType == null || !mimeType.startsWith("image/"))
            return null;

        InputStream imageStream = articleImage.getInputStream();
        if(imageStream.available() == 0)
            return null;

        return imageStream;
    }

    public static User requireSession(HttpServletRequest req) {
        var session = getSession(req);
        if(session == null)
            throw new IllegalStateException("User accessed a protected page without logging in");
        return session;
    }

    public static @Nullable User getSession(HttpServletRequest req) {
        var session = req.getSession(false);
        if(session == null)
            return null;

        var sessionObj = session.getAttribute("user");
        if(!(sessionObj instanceof User u))
            return null;

        return u;
    }
}