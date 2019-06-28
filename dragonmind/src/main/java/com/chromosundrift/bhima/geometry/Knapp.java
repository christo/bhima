package com.chromosundrift.bhima.geometry;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public enum Knapp {

    ZIG_ZAG(0), ZAG_ZIG(1);

    final int start;

    Knapp(int start) {
        this.start = start;
    }

    public <T> Nexter<T> use(T zig, T zag) {
        return new Nexter<T>(zig, zag);
    }

    public class Nexter<T> implements Supplier<T> {
        private T zig;
        private T zag;

        public Nexter(T zig, T zag) {
            this.zig = zig;
            this.zag = zag;
        }

        @Override
        public T get() {
            return null;
        }

    }
}
