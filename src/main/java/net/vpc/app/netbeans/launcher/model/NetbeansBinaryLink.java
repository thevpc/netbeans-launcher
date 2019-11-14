package net.vpc.app.netbeans.launcher.model;

public class NetbeansBinaryLink extends NetbeansLocation{
    private String version;
    private String packaging;
    private String url;

    public String getVersion() {
        return version;
    }

    public NetbeansBinaryLink setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getPackaging() {
        return packaging;
    }

    public NetbeansBinaryLink setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NetbeansBinaryLink setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        return "Netbeans "+version;
    }
}
