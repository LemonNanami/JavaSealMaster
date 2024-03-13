package org.javasealmaster;

public class Box {
        private int bx;
        private int by;
        private int bt;
        private int bw;
        private int bh;

    public Box(int bx, int by, int bw, int bh, int bt) {
        this.bx = bx;
        this.by = by;
        this.bw = bw;
        this.bh = bh;
        this.bt = bt;
    }

    public int getBx() {
            return bx;
        }

        public void setBx(int bx) {
            this.bx = bx;
        }

        public int getBy() {
            return by;
        }

        public void setBy(int by) {
            this.by = by;
        }

        public int getBt() {
            return bt;
        }

        public void setBt(int bt) {
            this.bt = bt;
        }

        public int getBw() {
            return bw;
        }

        public void setBw(int bw) {
            this.bw = bw;
        }

        public int getBh() {
            return bh;
        }

        public void setBh(int bh) {
            this.bh = bh;
        }
    }
