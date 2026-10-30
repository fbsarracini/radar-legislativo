package br.com.radarlegislativo.log;

import org.slf4j.Logger;

public final class AppLog {

    private final Logger logger;
    private String who;
    private String what;
    private String where;
    private String why;

    private AppLog(Logger logger) {
        this.logger = logger;
    }

    public static AppLog logger(Logger logger) {
        return new AppLog(logger);
    }

    public AppLog who(String who) {
        this.who = who;
        return this;
    }

    public AppLog system() {
        this.who = "sistema";
        return this;
    }

    public AppLog does(String what) {
        this.what = what;
        return this;
    }

    public AppLog on(String where) {
        this.where = where;
        return this;
    }

    public AppLog failed(String why) {
        this.why = why;
        return this;
    }

    public void debug() {
        logger.debug("{}", build());
    }

    public void info() {
        logger.info("{}", build());
    }

    public void warn() {
        logger.warn("{}", build());
    }

    public void error() {
        logger.error("{}", build());
    }

    public void error(Throwable t) {
        logger.error("{}", build(), t);
    }

    private String build() {
        var sb = new StringBuilder();
        if (who != null) sb.append("who=").append(who);
        if (what != null) sb.append(" | what=").append(what);
        if (where != null) sb.append(" | where=").append(where);
        if (why != null) sb.append(" | why=").append(why);
        return sb.toString();
    }
}
