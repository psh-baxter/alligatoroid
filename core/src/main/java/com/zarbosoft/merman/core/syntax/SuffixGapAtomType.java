package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class SuffixGapAtomType extends BaseGapAtomType {
  public static final String PRECEDING_KEY = "preceding";
  public final String backType;

  public SuffixGapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
            TSList.of(
                new BackFixedTypeSpec(
                    new BackFixedTypeSpec.Config(
                        config.backType,
                        new BackFixedRecordSpec(
                            new BackFixedRecordSpec.Config(
                                new TSMap<>(
                                    s ->
                                        s.put(
                                                "preceding",
                                                new BackArraySpec(
                                                    new BaseBackSimpleArraySpec.Config(
                                                        PRECEDING_KEY, null, new TSList<>())))
                                            .put(
                                                "text",
                                                new BackPrimitiveSpec(
                                                    new BaseBackPrimitiveSpec.Config(
                                                        GAP_PRIMITIVE_KEY)))),
                                ROSet.empty))))),
            new TSList<FrontSpec>()
                .add(
                    new FrontArraySpec(
                        new FrontArraySpec.Config(PRECEDING_KEY, config.frontArrayConfig)))
                .add(
                    new FrontSymbol(
                        new FrontSymbol.Config(
                                config.gapPlaceholderSymbol == null
                                    ? new SymbolTextSpec(new SymbolTextSpec.Config("•"))
                                    : config.gapPlaceholderSymbol)
                            .condition(
                                new ConditionValue(
                                    new ConditionValue.Config(
                                        GAP_PRIMITIVE_KEY, ConditionValue.Is.EMPTY, false)))))
                .add(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config(GAP_PRIMITIVE_KEY)))
                .addAll(config.frontSuffix)));
    backType = config.backType;
  }

  @Override
  public String name() {
    return "Suffix gap";
  }

  public static class Config {
    public String id = "__suffix_gap";
    public String backType = "__suffix_gap";
    public FrontArraySpecBase.Config frontArrayConfig = new FrontArraySpecBase.Config();
    public ROList<FrontSpec> frontSuffix = ROList.empty;
    public Symbol gapPlaceholderSymbol;

    public Config() {}

    public Config(
        String id,
        String backType,
        FrontArraySpecBase.Config frontArrayConfig,
        ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.backType = backType;
      this.frontArrayConfig = frontArrayConfig;
      this.frontSuffix = frontSuffix;
    }
  }
}
