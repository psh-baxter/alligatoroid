package com.zarbosoft.pidgoon.model;

import com.zarbosoft.rendaw.common.Format;

public class MismatchCause {
    public final Node node;
    public final Object color;

    public MismatchCause(Node node, Object color) {
        this.node = node;
        this.color = color;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append(node.toString());
        if (color != null) message.append(Format.format(" (%s)", color));
        return message.toString();
    }
}
