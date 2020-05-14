package net.veldor.rutrackermobile.utils;

import android.util.Log;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.selections.ViewListItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class PageParser {
    public ArrayList<ViewListItem> parsePage(String s) {
        ViewListItem item;
        ArrayList<ViewListItem> result = new ArrayList<>();
        Document dom = Jsoup.parse(s, "Windows-1251");
        // для начала, попробую найти результаты поиска
        Elements searchResults = dom.select("div#search-results table tr.hl-tr");
        if(searchResults.size() > 0){
            for (Element row :
                    searchResults) {
                // заполню название
                Elements torrentName = row.select(">td.t-title>div.t-title>a.tLink");
                if(torrentName.size() == 1){
                    item = new ViewListItem();
                    item.setType("Раздача");
                    item.setPageLink(true);
                    item.setName(torrentName.text().substring(0, torrentName.text().length() - 2));
                    item.setUrl(torrentName.attr("href"));
                    // найду данные о торренте
                    Elements torrentLink = row.select(">td.tor-size>a.tr-dl");
                    if(torrentLink.size() == 1){
                        item.setTorrentUrl(torrentLink.attr("href"));
                        item.setTorrentSize(torrentLink.text());
                        // найду данные о сидах и личах
                        Elements leechesInfo = row.select(">td.leechmed");
                        if(leechesInfo.size() == 1){
                            item.setLeeches(leechesInfo.text());
                        }
                        Elements seedsInfo = row.select(">td>b.seedmed");
                        if(seedsInfo.size() == 1){
                            item.setSeeds(seedsInfo.text());
                        }
                    }
                    // найду категорию раздачи
                    Elements category = row.select("td.f-name");
                    if(category.size() == 1){
                        item.setCategory(category.text());
                        // найду ссылку на поиск в данной категории
                        Elements categoryLink = category.select("a.gen");
                        if(categoryLink.size() == 1){
                            item.setCategoryLink(categoryLink.attr("href"));
                        }
                    }
                    result.add(item);
                }
            }
        }
        else{
            // найду заголовок
            Elements pageTitle = dom.getElementsByTag("title");
            if (pageTitle.size() == 1) {
                App.getInstance().mPageTitle.postValue(pageTitle.text());
            }
            Elements categories = dom.select("div.category");
            if (categories.size() > 0) {
                // найдены категории элементов. добавлю категорию в список элементов
                for (Element e :
                        categories) {
                    item = new ViewListItem();
                    // найду название категории
                    Elements title = e.select(">h3.cat_title");
                    item.setName(title.text());
                    // и ссылку на страницу, на которую это дело ведёт
                    Elements a = e.getElementsByTag("a");
                    item.setUrl(a.attr("href"));
                    item.setType(App.getInstance().getString(R.string.category_item));
                    result.add(item);

                    // найду внутри ссылки на форумы
                    Elements forumLinks = e.select("h4.forumLink a");
                    if (forumLinks.size() > 0) {
                        for (Element f : forumLinks) {
                            item = new ViewListItem();
                            item.setType("Форум");
                            item.setName(f.text());
                            item.setUrl(f.attr("href"));
                            result.add(item);

                            Elements subForumLinks = f.parent().parent().select("p.subforums>span.sf_title>a");
                            if (subForumLinks.size() > 0) {
                                for (Element sf : subForumLinks) {
                                    item = new ViewListItem();
                                    item.setType("Раздел");
                                    item.setName(sf.text());
                                    item.setUrl(sf.attr("href"));
                                    result.add(item);
                                }
                            }
                        }
                    }
                }
            } else {
                // проверю таблицу форума
                Elements forumTable = dom.select("table.forum");
                if (forumTable.size() == 1) {
                    // буду перебирать строки
                    Elements rows = forumTable.select(">tbody>tr");
                    if (rows.size() > 0) {
                        for (Element row :
                                rows) {
                            // проверю тип элемента
                            if (row.attr("class").equals("hl-tr")) {
                                // тут ссылка на тему
                                // найду ссылку на тему с торрентом
                                Elements torrentTopicLink = row.select(">td>div.torTopic>a.torTopic");
                                if (torrentTopicLink.size() == 1) {
                                    // а вот и самое главное, найдена ссылка на торрент
                                    item = new ViewListItem();
                                    item.setType("Топик");
                                    item.setName(torrentTopicLink.text());
                                    item.setUrl(torrentTopicLink.attr("href"));
                                    item.setPageLink(true);

                                    // получу статистику торрента и ссылку на него
                                    Elements torrentLink = row.select(">td.vf-col-tor>div>div>a.f-dl");
                                    if (torrentLink.size() == 1) {
                                        // найдена ссылка на торрент и размер файла
                                        item.setTorrentUrl(torrentLink.attr("href"));
                                        item.setTorrentSize(torrentLink.text());
                                    }
                                    // получу количество личей и сидов
                                    Elements seeds = row.select(">td.vf-col-tor>div>div>span.seedmed");
                                    if (seeds.size() == 1) {
                                        item.setSeeds(seeds.text());
                                    }
                                    // получу количество личей и сидов
                                    Elements leeches = row.select(">td.vf-col-tor>div>div>span.leechmed");
                                    if (leeches.size() == 1) {
                                        item.setLeeches(leeches.text());
                                    }

                                    result.add(item);
                                } else {
                                    // найду ссылку на название темы
                                    Elements topicLink = row.select(">td>span>a.topictitle.tt-text");
                                    if (topicLink.size() == 1) {
                                        item = new ViewListItem();
                                        item.setType("Топик");
                                        item.setName(topicLink.text());
                                        item.setUrl(topicLink.attr("href"));
                                        item.setPageLink(true);
                                        result.add(item);
                                    }
                                }
                            } else {
                                // тут, возможно, категория
                                Elements categoryTd = row.select("td.topicSep");
                                if (categoryTd.size() == 1) {
                                    item = new ViewListItem();
                                    item.setType("Раздел");
                                    item.setName(categoryTd.text());
                                    result.add(item);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
