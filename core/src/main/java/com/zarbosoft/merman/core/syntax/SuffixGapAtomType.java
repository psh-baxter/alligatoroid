package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.error.GapHasExtraField;
import com.zarbosoft.merman.core.syntax.error.GapPrimitiveCantHavePattern;
import com.zarbosoft.merman.core.syntax.error.GapPrimitiveHasBadId;
import com.zarbosoft.merman.core.syntax.error.SuffixGapPrecedingHasBadId;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
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

public class SuffixGapAtomType extends BaseGapAtomType {
  public static final String PRECEDING_KEY = "preceding";
  public static ROList<BackSpec> jsonBack =
      TSList.of(
          new BackFixedRecordSpec(
              new BackFixedRecordSpec.Config(
                  new TSOrderedMap<>(
                      m ->
                          m.put("type", new BackFixedPrimitiveSpec("suffix_gap"))
                              .put(
                                  "primitive",
                                  new BackPrimitiveSpec(
                                      new BaseBackPrimitiveSpec.Config(GapAtomType.PRIMITIVE_KEY)))
                              .put(
                                  "preceding",
                                  new BackArraySpec(
                                      new BaseBackArraySpec.Config(
                                          SuffixGapAtomType.PRECEDING_KEY, null, ROList.empty)))),
                  ROSet.empty)));
  public final String backType;

  public SuffixGapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
            config.back == null
                ? TSList.of(
                    new BackFixedTypeSpec(
                        new BackFixedTypeSpec.Config(
                            config.backType,
                            new BackFixedRecordSpec(
                                new BackFixedRecordSpec.Config(
                                    new TSOrderedMap<>(
                                        m ->
                                            m.put(
                                                    "preceding",
                                                    new BackArraySpec(
                                                        new BaseBackArraySpec.Config(
                                                            PRECEDING_KEY, null, ROList.empty)))
                                                .put(
                                                    "text",
                                                    new BackPrimitiveSpec(
                                                        new BaseBackPrimitiveSpec.Config(
                                                            PRIMITIVE_KEY)))),
                                    ROSet.empty)))))
                : config.back,
            new TSList<FrontSpec>()
                .add(
                    new FrontArraySpec(
                        new FrontArraySpec.Config(PRECEDING_KEY, config.frontArrayConfig)))
                .add(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config(PRIMITIVE_KEY).style(config.primitiveStyle)))
                .addAll(config.frontSuffix)));
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
                  if (GapAtomType.PRIMITIVE_KEY.equals(id)) {
                    if (((BaseBackPrimitiveSpec) backSpec).pattern != null) {
                      checkErrors.add(new GapPrimitiveCantHavePattern(SuffixGapAtomType.this.id));
                    }
                  } else {
                    checkErrors.add(new GapPrimitiveHasBadId(SuffixGapAtomType.this.id, id));
                  }
                } else if (backSpec instanceof BaseBackArraySpec) {
                  if (SuffixGapAtomType.PRECEDING_KEY.equals(id)) {
                    // nop
                  } else {
                    checkErrors.add(new SuffixGapPrecedingHasBadId(SuffixGapAtomType.this.id, id));
                  }
                } else {
                  checkErrors.add(new GapHasExtraField(SuffixGapAtomType.this.id, id));
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
    return "Suffix gap";
  }

  public static class Config {
    public String id = "__suffix_gap";
    public String backType = "__suffix_gap";
    public ROList<BackSpec> back;
    public FrontArraySpecBase.Config frontArrayConfig = new FrontArraySpecBase.Config();
    public ROList<FrontSpec> frontSuffix = ROList.empty;
    public Symbol gapPlaceholderSymbol;
    public Style primitiveStyle;

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

    public Config back(ROList<BackSpec> back) {
      this.back = back;
      return this;
    }

    public Config primitiveStyle(Style style) {
      this.primitiveStyle = style;
      return this;
    }

    public Config frontSuffix(ROList<FrontSpec> specs) {
      this.frontSuffix = specs;
      return this;
    }

    public Config frontArrayConfig(FrontArraySpecBase.Config config) {
      this.frontArrayConfig = config;
      return this;
    }
  }
}
