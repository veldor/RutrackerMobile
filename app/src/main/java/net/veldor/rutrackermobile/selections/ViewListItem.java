package net.veldor.rutrackermobile.selections;

public class ViewListItem {
    private String name;
    private String url;
    private String type;
    private String torrentUrl;
    private String torrentSize;
    private String seeds;
    private String leeches;
    private String category;
    private String categoryLink;
    private boolean pageLink;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public void setTorrentUrl(String torrentUrl) {
        this.torrentUrl = torrentUrl;
    }

    public String getTorrentSize() {
        return torrentSize;
    }

    public void setTorrentSize(String torrentSize) {
        this.torrentSize = torrentSize;
    }

    public String getSeeds() {
        return seeds;
    }

    public void setSeeds(String seeds) {
        this.seeds = seeds;
    }

    public String getLeeches() {
        return leeches;
    }

    public void setLeeches(String leeches) {
        this.leeches = leeches;
    }

    public boolean isPageLink() {
        return pageLink;
    }

    public void setPageLink(boolean pageLink) {
        this.pageLink = pageLink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryLink() {
        return categoryLink;
    }

    public void setCategoryLink(String categoryLink) {
        this.categoryLink = categoryLink;
    }
}
