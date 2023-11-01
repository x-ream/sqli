package io.xream.sqli.builder.internal;

import io.xream.sqli.builder.JoinType;

/**
 * @author sim
 */
public class Join {

    private String join;
    private String alia;
    private On on;

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }

    public On getOn() {
        return on;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public void setJoin(JoinType joinType) {
        this.join = joinType.sql();
    }
}
