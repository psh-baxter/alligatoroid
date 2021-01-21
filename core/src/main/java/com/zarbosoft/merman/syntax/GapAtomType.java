package com.zarbosoft.merman.syntax;

import com.zarbosoft.merman.misc.ROList;
import com.zarbosoft.merman.misc.ROSet;
import com.zarbosoft.merman.misc.TSList;
import com.zarbosoft.merman.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSpec;

public class GapAtomType extends BaseGapAtomType {
  public final String backType;

  public static class Config {
    public String id = "__gap";
    public ROSet<String> atomTags = ROSet.empty;
    public String backType = "__gap";
    public ROSet<String> frontPrimitiveTags = ROSet.empty;
    public ROList<FrontSpec> frontPrefix = ROList.empty;
    public ROList<FrontSpec> frontSuffix = ROList.empty;

    public Config() {}

    public Config(
        String id,
        ROSet<String> atomTags,
        String backType,
        ROSet<String> frontPrimitiveTags,
        ROList<FrontSpec> frontPrefix,
        ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.atomTags = atomTags;
      this.backType = backType;
      this.frontPrimitiveTags = frontPrimitiveTags;
      this.frontPrefix = frontPrefix;
      this.frontSuffix = frontSuffix;
    }
  }

  public GapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
            config.atomTags,
            TSList.of(
                new BackFixedTypeSpec(
                    new BackFixedTypeSpec.Config(
                        config.backType,
                        new BackPrimitiveSpec(
                            new BaseBackPrimitiveSpec.Config(GAP_PRIMITIVE_KEY, null))))),
            new TSList<FrontSpec>()
                .addAll(config.frontPrefix)
                .add(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config(
                            GAP_PRIMITIVE_KEY, config.frontPrimitiveTags)))
                .addAll(config.frontSuffix)));
    backType = config.backType;
  }

  @Override
  public String name() {
    return "Gap";
  }
}
