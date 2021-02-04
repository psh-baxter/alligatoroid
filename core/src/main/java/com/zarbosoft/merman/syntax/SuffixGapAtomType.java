package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.editor.I18nEngine;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class SuffixGapAtomType extends BaseGapAtomType {
  public static final String GAP_ARRAY_KEY = "value";
  public final String backType;

  public SuffixGapAtomType(I18nEngine i18n, Config config) {
    super(
        new AtomType.Config(
            config.id,
            config.atomTags,
            TSList.of(
                new BackFixedTypeSpec(
                    new BackFixedTypeSpec.Config(
                        config.backType,
                        new BackFixedRecordSpec(
                            new BackFixedRecordSpec.Config(
                                new TSMap<>(
                                    s ->
                                        s.put(
                                                "value",
                                                new BackArraySpec(
                                                    new BaseBackSimpleArraySpec.Config(
                                                        GAP_ARRAY_KEY, null, new TSList<>())))
                                            .put(
                                                "gap",
                                                new BackPrimitiveSpec(
                                                    i18n,
                                                    new BaseBackPrimitiveSpec.Config(
                                                        GAP_PRIMITIVE_KEY, null)))),
                                ROSet.empty))))),
            TSList.of(
                    new FrontArraySpec(
                        new FrontArraySpec.Config(GAP_ARRAY_KEY, config.frontArrayConfig)),
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config(
                            GAP_PRIMITIVE_KEY, config.frontPrimitiveTags)))
                .addAll(config.frontSuffix)));
    backType = config.backType;
  }

  @Override
  public String name() {
    return "Suffix gap";
  }

  public static class Config {
    public String id = "__suffix_gap";
    public ROSet<String> atomTags = ROSet.empty;
    public String backType = "__suffix_gap";
    public ROSet<String> frontPrimitiveTags = ROSet.empty;
    public FrontArraySpecBase.Config frontArrayConfig = new FrontArraySpecBase.Config();
    public ROList<FrontSpec> frontSuffix = ROList.empty;

    public Config() {}

    public Config(
        String id,
        ROSet<String> atomTags,
        String backType,
        ROSet<String> frontPrimitiveTags,
        FrontArraySpecBase.Config frontArrayConfig,
        ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.atomTags = atomTags;
      this.backType = backType;
      this.frontPrimitiveTags = frontPrimitiveTags;
      this.frontArrayConfig = frontArrayConfig;
      this.frontSuffix = frontSuffix;
    }
  }
}
