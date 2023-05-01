package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;

public class BuyPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {
        webExchange.getSession().setAttributeValue("user", "John");

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        templateEngine.process("buy", ctx, writer);
    }
}