package it.polimi.webapp.pages;

import it.polimi.webapp.IWebExchanges;
import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class BuyPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        var session = IWebExchanges.requireSession(webExchange);

        String search = webExchange.getRequest().getParameterValue("search");
        ctx.setVariable("searchTerm", search != null ? search : "");

        try (var connection = dataSource.getConnection()) {
            if (search != null && !search.equals("")) {
                var result = new AuctionDao(connection).findAuctionByWord(search);
                ctx.setVariable("auctions", result);
            } else {
                ctx.setVariable("auctions", Collections.emptyList());
            }

            var boughtAuctions = new AuctionDao(connection).findUserBoughtAuctions(session.id());
            ctx.setVariable("boughtAuctions", boughtAuctions);

            ctx.setVariable("errorQuery", false);
        } catch (SQLException e) {
            ctx.setVariable("errorQuery", true);
            e.printStackTrace();
        }

        templateEngine.process("buy", ctx, writer);
    }
}