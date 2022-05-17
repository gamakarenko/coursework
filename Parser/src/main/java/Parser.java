import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;


public class Parser {
    public static Connection co;

    // функция создания соединения с базой даннных
    public static void open() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        co = DriverManager.getConnection("jdbc:sqlite:parsering.db");
    }


    public static void close() throws SQLException {
        co.close();
    }
    // необязательная функция вывода данных в консоль( писалась для начальной стадии, чтобы тестировать код программы )
    public static void select() throws SQLException {
        Statement statement = co.createStatement();
        String query = "SELECT id, url, name, header, date, article FROM parsering ";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            int id = rs.getInt("id");
            String url = rs.getString("url");
            String name = rs.getString("name");
            String header = rs.getString("header");
            String date = rs.getString("date");
            String article = rs.getString("article");
            System.out.println(id + "\t|" + url + "\t|" + name + "\t|" + header + "\t|" + date + "\t|" + article);
        }
        rs.close();
        statement.close();
    }

    // функция, которая создаёт таблицу при её отсутствии в базе данных SQLite

    public static void CreateTable() throws SQLException, ClassNotFoundException {
        open();
        Statement statement = co.createStatement();
        String query = "CREATE TABLE if not exists 'parsering' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'url' VARCHAR(50), 'name' VARCCHAR(50), 'header' VARCHAR(50), 'article')";
        statement.executeQuery(query);
    }
    // функция получения и добавления данных в базу данных с помощью SQL запросов
    public static void getInfo() throws IOException, SQLException {
        String urls = "https://rb.ru/sitemap.xml";
        Document page = Jsoup.connect(urls)
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("http://www.google.com")
                .get();

        Elements pages = page.select("loc");
        String str = pages.text();
        String[] words = str.split(" ");
        for (String word : words) {
            Document pagein = Jsoup.connect(word)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .get();

            Elements pagesin = pagein.select("loc");
            String strin = pagesin.text();
            String[] wordsin = strin.split(" ");

            for (String wordin : wordsin) {
                    Document pagein1 = Jsoup.connect(wordin)
                            .userAgent("Chrome/4.0.249.0 Safari/532.5")
                            .referrer("http://www.google.com")
                            .get();

                    Elements pagesName = pagein1.select("#article-feed > div > section.article-header > div > div.article-header__wrap.article-header__wrap--l2 > div.article-header__wrap.article-header__wrap--author > div > span > a");
                    Elements pagesHeader = pagein1.select("#article-feed > div > section.article-header > div > div.article-header__wrap.article-header__wrap--rubric > h1");
                    Elements pagesDate = pagein1.select("#article-feed > div > section.article-header > div > div.article-header__wrap.article-header__wrap--l2 > div.article-header__wrap.article-header__wrap--header.article-header__wrap--header-2 > time");
                    Elements pagesArticle = pagein1.select("#article-feed > div > section.article > div.article__container.article__container--main > div > div:nth-child(2) > div.article__content-block.abv > p");

                    // SQL запрос,с помощью которого в таблицу добавляются только те данные, которых нет

                    String query1 = "INSERT INTO parsering(url, name, header, date, article) SELECT DISTINCT '" + wordin + "', '"+pagesName.text()+"', '"+pagesHeader.text()+"', '"+pagesDate.text()+"', '"+pagesArticle.text()+"' FROM parsering WHERE NOT EXISTS (SELECT url FROM parsering WHERE url  = '" + wordin + "')";

                    Statement statement = co.createStatement();
                    statement.executeUpdate(query1);
                }
            }
        }

    // функция для первичного заполнения базы данных

    public static void filling() throws ClassNotFoundException, SQLException, IOException {
        open();
        Statement statement = co.createStatement();
        String query = "INSERT INTO parsering (url, name, header, date, article) VALUES ('url','name', 'header', 'date', 'article')";
        statement.executeUpdate(query);
        open();
        getInfo();
        select();
        close();
    }

    /* функция режима наблюдения

    1 - запрашиваем у пользователя время ( в минутах, через которое необходимо будет вновь просмотреть страницы )
    2 - открываем соединение и подключаем функцию getInfo
    3 - при наличии новых ссылок они добавятся в таблицу, пропарсятся

    ** Если будет введён 0, программа завершит процесс


    */
    public static void observation() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        boolean b = true;
        Scanner scanner = new Scanner(System.in);
        int id;
        while(b){
            System.out.println("Введите время (в минутах), через которое нужно будет вновь пропарсить страницу: ");
            id = scanner.nextInt();
            if(id>0){
                Thread.sleep(60000 * id);
                open();
                getInfo();
                close();


            }
            else{
                System.out.println("Program terminated");
                b = false;
            }
        }

    }

    // Функция main

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        CreateTable();
        System.out.println("Выберите один из режимов:" + "\n" + "1 - Режим первичного заполнения базы данных" + "\n" + "2 - Режим наблюдения" + "\n" + "0 - Выход их программы");
        Scanner scanner = new Scanner(System.in);
        int n = 1;
        while(n==1 && n!=0) {
            int id = scanner.nextInt();
            switch (id) {
                case (1):
                    filling();
                    break;
                case (2):
                    observation();
                    break;
                case (0):
                    break;
            }
            System.out.println("Для продолжения работы программы введите 1, если необходимо выйти - введите 0: ");
            n = scanner.nextInt();
            System.out.println("Выберите один из режимов:" + "\n" + "1 - Режим первичного заполнения базы данных" + "\n" + "2 - Режим наблюдения" + "\n" + "0 - Выход их программы");
        }


    }
}



















