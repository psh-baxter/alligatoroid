package com.zarbosoft.merman.core.syntax;

public enum BackType {
    LUXEM {
        @Override
        public String mime() {
            return "application/luxem";
        }
    },
    JSON {
        @Override
        public String mime() {
            return "application/json";
        }
    };

    public abstract String mime();
}
