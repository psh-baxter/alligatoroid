package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.error.GapHasExtraField;
import com.zarbosoft.merman.core.syntax.error.GapPrimitiveCantHavePattern;
import com.zarbosoft.merman.core.syntax.error.GapPrimitiveHasBadId;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.Symbol;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSOrderedMap;

import java.util.function.Function;

public class GapAtomType extends BaseGapAtomType {
  public static ROList<BackSpec> jsonBack =
      TSList.of(
          new BackFixedRecordSpec(
              new BackFixedRecordSpec.Config(
                  new TSOrderedMap<>(
                      m ->
                          m.put("type", new BackFixedPrimitiveSpec("gap"))
                              .put(
                                  "primitive",
                                  new BackPrimitiveSpec(
                                      new BaseBackPrimitiveSpec.Config(
                                          GapAtomType.PRIMITIVE_KEY)))),
                  ROSet.empty)));
  public final String backType;

  public GapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
            config.back == null
                ? TSList.of(
                    new BackFixedTypeSpec(
                        new BackFixedTypeSpec.Config(
                            config.backType,
                            new BackPrimitiveSpec(
                                new BaseBackPrimitiveSpec.Config(PRIMITIVE_KEY)))))
                : config.back,
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
                .add(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config(PRIMITIVE_KEY).style(config.primitiveStyle)))
                .addAll(config.frontSuffix == null ? ROList.empty : config.frontSuffix)));
    backType = config.backType;
    MultiError checkErrors = new MultiError();
    for (BackSpec backSpec : back()) {
      BackSpec.walk(
          backSpec,
          new Function<BackSpec, Boolean>() {
            @Override
            public Boolean apply(BackSpec backSpec) {
              if (backSpec instanceof BackSpecData) {
                String id = ((BackSpecData) backSpec).id;
                if (backSpec instanceof BaseBackPrimitiveSpec) {
                  if (GapAtomType.PRIMITIVE_KEY.equals(((BaseBackPrimitiveSpec) backSpec).id)) {
                    if (((BaseBackPrimitiveSpec) backSpec).pattern != null) {
                      checkErrors.add(new GapPrimitiveCantHavePattern(GapAtomType.this.id));
                    }
                  } else {
                    checkErrors.add(
                        new GapPrimitiveHasBadId(
                            GapAtomType.this.id, ((BaseBackPrimitiveSpec) backSpec).id));
                  }
                } else {
                  checkErrors.add(new GapHasExtraField(id, GapAtomType.this.id));
                }
              }
              return true;
            }
          });
    }
    checkErrors.raise();
  }

  @Override
  public String name() {
    return "Gap";
  }

  public static class Config {
    public String id = "__gap";
    public String backType = "__gap";
    public ROList<BackSpec> back;
    public ROList<FrontSpec> frontPrefix = null;
    public ROList<FrontSpec> frontSuffix = null;
    public Symbol gapPlaceholderSymbol;
    public Style primitiveStyle;

    public Config() {}

    public Config(
        String id, String backType, ROList<FrontSpec> frontPrefix, ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.backType = backType;
      this.frontPrefix = frontPrefix;
      this.frontSuffix = frontSuffix;
    }

    public Config back(ROList<BackSpec> back) {
      this.back = back;
      return this;
    }

    public Config primitiveStyle(Style style) {
      this.primitiveStyle = style;
      return this;
    }
  }
}
