package com.zarbosoft.merman.core.syntax.primitivepattern;

import com.zarbosoft.pidgoon.events.EscapableResult;
import com.zarbosoft.pidgoon.model.Node;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

public class SymbolCharacter extends Pattern{
    public static java.util.regex.Pattern IsSymbol = java.util.regex.Pattern.compile("\\p{Alnum}+");
    public Node<EscapableResult<ROList<String>>> build(boolean capture) {
        return new BaseTerminal(capture) {
            @Override
            protected ROPair<Boolean, ROList<String>> matches1(CharacterEvent event) {
                return new ROPair<>(IsSymbol.matcher(event.value).matches(), new TSList<>(event.value));
            }
        };
    }
}
