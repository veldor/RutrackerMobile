package net.veldor.rutrackermobile.utils;

import android.util.Log;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.selections.ViewListItem;
import net.veldor.rutrackermobile.ui.BrowserActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class PageParser {
    public ArrayList<ViewListItem> parsePage(String s) {
        BrowserActivity.sPagesCount = -1;
        ViewListItem item;
        ArrayList<ViewListItem> result = new ArrayList<>();
        Document dom = Jsoup.parse(s, "Windows-1251");


        // найду заголовок
        Elements pageTitle = dom.getElementsByTag("title");
        if (pageTitle.size() == 1) {
            App.getInstance().mPageTitle.postValue(pageTitle.text());
        }

        // обработаю страницы
        Elements paginator = dom.select("div#pagination");
        if (paginator.size() == 1) {
            Elements pages = paginator.select("a.pg");
            if (pages.size() > 2) {
                // возьму предпоследний элемент, он должен быть максимальной страницей
                Element maxPage = pages.get(pages.size() - 2);
                if (maxPage != null) {
                    BrowserActivity.sPagesCount = Integer.parseInt(maxPage.text());
                }

            }
        } else {
            paginator = dom.select("div.bottom_info a.pg");
            if (paginator.size() > 2) {
                // для каждой страницы, если это не начальная и не конечная, добавлю ссылку
                BrowserActivity.sSearchResultsArray = new ArrayList<>();
                for (Element e :
                        paginator) {
                    if (!e.text().equals("Пред.") && !e.text().equals("След.")) {
                        BrowserActivity.sSearchResultsArray.add(e.attr("href"));
                    }
                }
                Log.d("surprise", "PageParser parsePage: founded search result pages: " + BrowserActivity.sSearchResultsArray.size());
            } else {
                Log.d("surprise", "PageParser parsePage: found one page");
                BrowserActivity.sPagesCount = -1;
                BrowserActivity.sSearchResultsArray = null;
            }
        }


        //todo позже объединю разделы, пока что будет дублирование
        String loadedPageUrl = App.getInstance().mHistory.peek();

        // если загружается главная страница- покажу только категории
        if (loadedPageUrl.equals(App.RUTRACKER_MAIN_PAGE)) {
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
                }
            }
            return result;
        }

        if (loadedPageUrl.startsWith("https://rutracker.org/forum/index.php?c=")) {
            String categoryId = loadedPageUrl.substring(40);
            // найду все категории, но содержимое покажу только у той, что выбрана
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
                    // получу id категории
                    String id = e.attr("id");
                    if (id.substring(2).equals(categoryId)) {
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
                }
            }
            return result;
        }

        if (loadedPageUrl.startsWith("https://rutracker.org/forum/viewforum.php?f=")) {
            // тут могут быть вложенные форумы и ссылки на топики
            Elements searchResults = dom.select("h4.forumlink, td.topicSep, tr.hl-tr");
            if (searchResults.size() > 0) {
                for (Element el :
                        searchResults) {
                    // добавлю элемент в зависимости от типа
                    switch (el.tagName()) {
                        case "h4":
                            Elements link = el.select(">a");
                            if (link.size() == 1) {
                                item = new ViewListItem();
                                item.setType("Форум");
                                item.setName(link.text());
                                item.setUrl(link.attr("href"));
                                result.add(item);
                            }
                            break;
                        case "td":
                            item = new ViewListItem();
                            item.setType("Раздел");
                            item.setName(el.text());
                            result.add(item);
                            break;
                        case "tr":
                            // тут ссылка на топик
                            Elements torrentName = el.select(">td.tt>span.topictitle>a.topictitle");
                            if (torrentName.size() == 1) {
                                item = new ViewListItem();
                                item.setType("Раздача");
                                item.setPageLink(true);
                                item.setName(torrentName.text().substring(0, torrentName.text().length() - 2));
                                item.setUrl(torrentName.attr("href"));
                                result.add(item);
                                break;
                            }
                            // тут ссылка на топик
                            torrentName = el.select(">td.tt>div.torTopic>a.torTopic");
                            if (torrentName.size() == 1) {
                                item = new ViewListItem();
                                item.setType("Раздача");
                                item.setPageLink(true);
                                item.setName(torrentName.text().substring(0, torrentName.text().length() - 2));
                                item.setUrl(torrentName.attr("href"));


                                // получу статистику торрента и ссылку на него
                                Elements torrentLink = el.select(">td.vf-col-tor>div>div>a.f-dl");
                                if (torrentLink.size() == 1) {
                                    // найдена ссылка на торрент и размер файла
                                    item.setTorrentUrl(torrentLink.attr("href"));
                                    item.setTorrentSize(torrentLink.text());
                                }
                                // получу количество личей и сидов
                                Elements seeds = el.select(">td.vf-col-tor>div>div>span.seedmed");
                                if (seeds.size() == 1) {
                                    item.setSeeds(seeds.text());
                                }
                                // получу количество личей и сидов
                                Elements leeches = el.select(">td.vf-col-tor>div>div>span.leechmed");
                                if (leeches.size() == 1) {
                                    item.setLeeches(leeches.text());
                                }
                                result.add(item);
                                break;
                            }

                            break;
                    }
                }
            }
            return result;
        }

        // для начала, попробую найти результаты поиска
        Elements searchResults = dom.select("div#search-results table tr.hl-tr");
        if (searchResults.size() > 0) {
            Log.d("surprise", "PageParser parsePage: found search results!");
            for (Element row :
                    searchResults) {
                // заполню название
                Elements torrentName = row.select(">td.t-title>div.t-title>a.tLink");
                if (torrentName.size() == 1) {
                    item = new ViewListItem();
                    item.setType("Раздача");
                    item.setPageLink(true);
                    item.setName(torrentName.text().substring(0, torrentName.text().length() - 2));
                    item.setUrl(torrentName.attr("href"));
                    // найду данные о торренте
                    Elements torrentLink = row.select(">td.tor-size>a.tr-dl");
                    if (torrentLink.size() == 1) {
                        item.setTorrentUrl(torrentLink.attr("href"));
                        item.setTorrentSize(torrentLink.text());
                        // найду данные о сидах и личах
                        Elements leechesInfo = row.select(">td.leechmed");
                        if (leechesInfo.size() == 1) {
                            item.setLeeches(leechesInfo.text());
                        }
                        Elements seedsInfo = row.select(">td>b.seedmed");
                        if (seedsInfo.size() == 1) {
                            item.setSeeds(seedsInfo.text());
                        }
                    }
                    // найду категорию раздачи
                    Elements category = row.select("td.f-name");
                    if (category.size() == 1) {
                        item.setCategory(category.text());
                        // найду ссылку на поиск в данной категории
                        Elements categoryLink = category.select("a.gen");
                        if (categoryLink.size() == 1) {
                            item.setCategoryLink(categoryLink.attr("href") + "&nm=" + BrowserActivity.sLastSearchString);
                        }
                    }
                    result.add(item);
                }
            }
        } else {
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
