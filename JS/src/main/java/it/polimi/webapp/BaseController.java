package it.polimi.webapp;

import com.google.gson.Gson;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

public abstract class BaseController extends HttpServlet {

    protected DataSource dataSource;

    protected final Gson gson = new Gson();

    @Override
    @Initializer
    @MustBeInvokedByOverriders
    public void init() throws ServletException {
        //connects to the database
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/AsteDB");
        } catch (NamingException e) {
            throw new ServletException("Failed to get Context", e);
        }

        if (this.dataSource == null)
            throw new ServletException("Data source not found!");
    }
}
