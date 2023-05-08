package it.polimi.webapp.controllers;

import it.polimi.webapp.BaseController;
import it.polimi.webapp.beans.Article;
import it.polimi.webapp.dao.ArticleDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;

@MultipartConfig(
        maxFileSize = 1024 * 1024 * 10,      // 10 MB
        maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class ArticleController extends BaseController {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Integer userId = (Integer) req.getSession().getAttribute("userId");
        String articleName = req.getParameter("articleName");
        String articleDesc = req.getParameter("articleDesc");
        Part articleImage = req.getPart("articleImage");

        InputStream imageStream = null;
        String mimeType = null;

        String imageName = null;

        if(articleImage != null){
            imageStream = articleImage.getInputStream();
            imageName = articleImage.getSubmittedFileName();
            mimeType = getServletContext().getMimeType(imageName);
        }

        boolean dataError = articleName == null || articleName.isEmpty()
                            || articleDesc == null || articleDesc.isEmpty()
                            || Objects.requireNonNull(articleImage).getSize() == 0 || imageStream == null
                            || (imageStream.available()==0) || !Objects.requireNonNull(mimeType).startsWith("image/");
        boolean numError = false;

        double articlePrice = -1;
        try {
            articlePrice = Double.parseDouble(req.getParameter("articlePrice"));
        } catch (NumberFormatException e){
            numError = true;
            dataError = true;
        }

        if(dataError) {
            if(!numError)
                req.setAttribute("articlePrice", articlePrice);
            if(!(articleName == null || articleName.isEmpty()))
                req.setAttribute("articleName", articleName);
            if(!(articleDesc == null || articleDesc.isEmpty()))
                req.setAttribute("articleDescription", articleDesc);

            var disp = Objects.requireNonNull(req.getRequestDispatcher("/sell"), "Missing dispatcher");
            req.setAttribute("errorArticleDataInserted", true);
            disp.forward(req, resp);
            return;
        }

        var article = new Article(articleName, articleDesc, Objects.requireNonNull(imageName), articlePrice, userId);

        try (var connection = dataSource.getConnection()) {
            int inserted = new ArticleDao(connection).insertArticle(article, Objects.requireNonNull(imageStream));
            if (inserted == 0) {
                // error in query execution
                var disp = Objects.requireNonNull(req.getRequestDispatcher("/sell"), "Missing dispatcher");
                req.setAttribute("errorArticleQuery", true);
                disp.forward(req, resp);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        req.setAttribute("articlePrice", "");
        req.setAttribute("articleName", "");
        req.setAttribute("articleDescription", "");
        req.setAttribute("goodInsertion", true);
        resp.sendRedirect(getServletContext().getContextPath() + "/sell");
    }
}
