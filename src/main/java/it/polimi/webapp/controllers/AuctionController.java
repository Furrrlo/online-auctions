package it.polimi.webapp.controllers;

import it.polimi.webapp.Initializer;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.beans.Auction;
import it.polimi.webapp.dao.AuctionDao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AuctionController extends HttpServlet {
    private DataSource dataSource;

    @Override
    @Initializer
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

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        boolean wrongInsertedData = false;

        List<Integer> articleIds = null;
        try {
            var stringArticleIds = req.getParameterValues("selectedArticles");
             articleIds = stringArticleIds == null ? null : Arrays.stream(stringArticleIds)
                     .map(Integer::parseInt)
                     .toList();
        } catch (NumberFormatException ex) {
            wrongInsertedData = true;
        }

        double minimumOfferDifference = -1;
        try {
            minimumOfferDifference = Double.parseDouble(req.getParameter("minimumOfferDifference"));
        } catch (NumberFormatException ex) {
            wrongInsertedData = true;
        }

        LocalDate expiryDate = null;
        try {
            expiryDate = LocalDate.parse(req.getParameter("expiryDate"));
        } catch (DateTimeParseException ex) {
            wrongInsertedData = true;
        }

        wrongInsertedData = wrongInsertedData ||
                articleIds == null || articleIds.isEmpty()
                || minimumOfferDifference <= 0
                || expiryDate == null || expiryDate.isBefore(LocalDate.now());

        if (wrongInsertedData) {
            var disp = Objects.requireNonNull(req.getRequestDispatcher("/auctionInsertion"), "Missing dispatcher");
            req.setAttribute("errorDataInserted", true);
            disp.forward(req, resp);
            return;
        }

        var auction = new Auction(
                expiryDate,
                articleIds.stream().map(Article::new).toList(),
                minimumOfferDifference);

        try (var connection = dataSource.getConnection()) {
            int result = new AuctionDao(connection).insertAuction(auction);
            if (result == 0) {
                //error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/auctionInsertion"), "Missing dispatcher");
                req.setAttribute("errorQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("goodAuctionInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }


}
