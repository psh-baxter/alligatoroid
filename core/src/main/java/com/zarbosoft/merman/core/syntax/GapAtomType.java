package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class GapAtomType extends BaseGapAtomType {
  public final String backType;

  public GapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
            TSList.of(
                new BackFixedTypeSpec(
                    new BackFixedTypeSpec.Config(
                        config.backType,
                        new BackPrimitiveSpec(
                            new BaseBackPrimitiveSpec.Config(PRIMITIVE_KEY))))),
            new TSList<FrontSpec>()
                .addAll(config.frontPrefix == null ? ROList.empty : config.frontPrefix)
                .add(
                    new FrontSymbol(
                        new FrontSymbol.Config(
                                config.gapPlaceholderSymbol == null
                                    ? new SymbolTextSpec(new SymbolTextSpec.Config("•"))
                                    : config.gapPlaceholderSymbol)
                            .condition(
                                new ConditionValue(
                                    new ConditionValue.Config(
                                            PRIMITIVE_KEY, ConditionValue.Is.EMPTY, false)))))
                .add(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config(PRIMITIVE_KEY)))
                .addAll(config.frontSuffix == null ? ROList.empty : config.frontSuffix)));
    backType = config.backType;
  }

  @Override
  public String name() {
    return "Gap";
  }

  public static class Config {
    public String id = "__gap";
    public String backType = "__gap";
    public ROList<FrontSpec> frontPrefix = null;
    public ROList<FrontSpec> frontSuffix = null;
    public Symbol gapPlaceholderSymbol;

    public Config() {}

    public Config(
        String id, String backType, ROList<FrontSpec> frontPrefix, ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.backType = backType;
      this.frontPrefix = frontPrefix;
      this.frontSuffix = frontSuffix;
    }
  }
}
