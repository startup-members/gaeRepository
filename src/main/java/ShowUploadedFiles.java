package main.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class ShowUploadedFiles extends HttpServlet {

    private DatastoreService datastoreService = DatastoreServiceFactory
            .getDatastoreService();

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String token = req.getParameter("parameter1");
        String responseJson = "";

        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();

        // ユーザ認証
        String dir = Util.getUserid(token);

        if (!dir.isEmpty()) {
            // ファイル情報一覧取得処理
            FileInfoBean fileInfoBean = new FileInfoBean();
            List<String> filenameList = new ArrayList<String>();

            Query query = new Query("FairuJouhou");
            query.setFilter(FilterOperator.EQUAL.of("directory", dir));
            PreparedQuery pQuery = datastoreService.prepare(query);

            for (Entity entity : pQuery.asIterable()) {
                // ファイル名一覧を取得
                filenameList.add(String.valueOf(entity.getProperty("fairuMei")));
            }
            fileInfoBean.setFairuMei(filenameList);
            fileInfoBean.setDirectory(dir);

            StringBuilder buil = new StringBuilder();
            for (String fairuMei : fileInfoBean.getFairuMei()) {
                // ▽ liタグ▽
                buil.append("<li style='list-style:none; display:inline-block; margin:1em'>");
                // ▽ aタグ ▽
                // divタグだとチェックボックスが右上にちゃんと付く。aタグだと右下になる。
                // buil.append("<div class='image_box'>");
                buil.append("<a class='image_box' href='");
                buil.append("https://storage.googleapis.com/smple_bucket/");
                buil.append(fileInfoBean.getDirectory());
                buil.append("/");
                buil.append(fairuMei);
                buil.append("'");
                buil.append(" ");
                buil.append("rel=\'prettyPhoto[group1]\'");
                buil.append(" ");
                buil.append("title=\'");
                buil.append(fairuMei);
                buil.append("'");
                buil.append(">");
                // ▽ imgタグ ▽
                buil.append("<img class='thumbnail' src='");
                buil.append("https://storage.googleapis.com/smple_bucket/");
                buil.append(fileInfoBean.getDirectory());
                buil.append("/");
                buil.append(fairuMei);
                buil.append("'");
                buil.append(" ");
                buil.append("alt='");
                buil.append(fairuMei);
                buil.append("'");
                buil.append(" ");
                buil.append("width='120' height='120'/>");
                // △ imgタグ △
                // ▽ checkbox ▽
                buil.append("<input class='checkbox' type='checkbox' value='");
                buil.append(fairuMei);
                buil.append("'/>");
                // △ checkbox △
                // buil.append("</div>");
                buil.append("</a>");
                // △ aタグ △
                buil.append("</li>");
                // △ liタグ △
            }
            // トークン生成
            token = UUID.randomUUID().toString();
            if (Util.updateToken(dir, token)) {
                // 戻り値設定
                responseJson = "{\"message\":\"success\", \"token\":\"" + token
                        + "\", \"image\":\"" + buil.toString() + "\"}";
            } else {
                responseJson = "{\"message\":\"token update fail\"}";
            }
        } else {
            // ユーザ認証失敗
            responseJson = "{\"message\":\"auth fail\"}";
        }
        out.write(responseJson);
    }
}
