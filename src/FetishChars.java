import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class FetishChars {
    private static ArrayList<Character> characters = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:/****/******/animeChars.db");
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS characters (id PRIMARY KEY,name TEXT,imgUrl TEXT)");
        ArrayList<String> namesList = new ArrayList<>();
        ArrayList<String> imgUrlsList = new ArrayList<>();
        try {
            for (int i = 0; i <= 108000; i += 50) {
                Document doc = Jsoup.connect("https://myanimelist.net/character.php?limit=" + i).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").timeout(600000).get();
                Elements names = doc.select("table.characters-favorites-ranking-table tbody tr.ranking-list td.people div.information.di-ib.mt24 a.fs14.fw-b");
                for (Element element : names) {
                    namesList.add(element.text());
                }
                Elements imgUrls = doc.select("table.characters-favorites-ranking-table tbody tr.ranking-list td.people a");
                for (Element element : imgUrls) {
                    String html = element.tagName("img").toString();
                    String imgUrl = cleanImgUrl((getDirtyImgUrl(html)));
                    if (!imgUrl.isEmpty()) {
                        imgUrlsList.add(imgUrl);
                    }
                }
                for (int j = 0; j < imgUrlsList.size(); j++) {
                    characters.add(new Character(namesList.get(j), imgUrlsList.get(j)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!characters.isEmpty()) {
            int i = 0;
            for (Character character : characters) {
                String url = "'" + character.getImgUrl() + "'";
                String name = "'" + character.getName() + "'";
                statement.execute("INSERT INTO characters VALUES(" +
                                    i++ + "," + name + "," + url + ")");
                System.out.println(i + " Characters in the data-base.");
            }
        }
        statement.close();
        connection.close();
    }

    public static String cleanImgUrl(String imgUrl) {
        imgUrl = imgUrl.replaceAll("/r/[0-9]{2,3}x[0-9]{2,3}", "");
        Pattern p = compile("^.*(?=(\\?))");
        Matcher m = p.matcher(imgUrl);
        while (m.find()) {
            imgUrl = m.group();
        }
        return imgUrl;
    }

    public static String getDirtyImgUrl(String html) {
        String dirtyImgUrl = "";
        Pattern p = compile("(?<=data-src=\")(.*?)(?=\" data-srcset)");
        Matcher m = p.matcher(html);
        while (m.find()) {
            dirtyImgUrl = m.group();
        }
        return dirtyImgUrl;
    }
}
