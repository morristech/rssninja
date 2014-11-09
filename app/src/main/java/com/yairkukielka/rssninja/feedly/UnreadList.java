package com.yairkukielka.rssninja.feedly;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superyair on 8/31/14.
 */
public class UnreadList {

    List<Unread> unreadcounts = new ArrayList<Unread>();

    public List<Unread> getUnreadcounts() {
        return unreadcounts;
    }

    public void setUnreadcounts(List<Unread> unreadcounts) {
        this.unreadcounts = unreadcounts;
    }


    public static class Unread {

        private String id;
        private int count;
        //private Date updated;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
