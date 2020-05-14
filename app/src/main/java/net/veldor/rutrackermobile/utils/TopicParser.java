package net.veldor.rutrackermobile.utils;

import android.util.Log;

import net.veldor.rutrackermobile.ui.TopicActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class TopicParser {
    public void parsePage(String s) {
        Log.d("surprise", "TopicParser parsePage: start parsing topic");
        Document dom = Jsoup.parse(s, "Windows-1251");
        // найду заголовок
        Elements distributionTitle = dom.select("a#topic-title");
        if (distributionTitle.size() == 1) {
            // оповещу о найденном заголовке
            TopicActivity.liveTitle.postValue(distributionTitle.text());
        }
        Elements distributionSize = dom.select("span#tor-size-humn");
        if (distributionSize.size() == 1) {
            TopicActivity.liveSize.postValue(distributionSize.text());
        }
        Elements seeds = dom.select("span.seed");
        if (seeds.size() == 1) {
            TopicActivity.liveSeeds.postValue(seeds.text());
        }
        Elements leeches = dom.select("span.leech");
        if (leeches.size() == 1) {
            TopicActivity.liveLeeches.postValue(leeches.text());
        }
        Elements poster = dom.select(".postImg.postImgAligned.img-right");
        if (poster.size() == 1) {
            TopicActivity.livePosterUrl.postValue(poster.attr("title"));
        }
        Elements torrentLink = dom.select("a.dl-link");
        if (torrentLink.size() == 1) {
            TopicActivity.liveTorrentLink.postValue(torrentLink.attr("href"));
        }
        Elements magnetLink = dom.select("a.magnet-link");
        if (magnetLink.size() == 1) {
            TopicActivity.liveMagnet.postValue(magnetLink.attr("href"));
        }
        Elements topicBody = dom.select("tbody.row1 td.message.td2>div.post_wrap>div.post_body");
        if (topicBody.size() > 0) {
            TopicActivity.liveBody.postValue(topicBody.get(0).html());
        }
    }
}
