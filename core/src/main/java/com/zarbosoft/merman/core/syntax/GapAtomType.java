package com.zarbosoft.merman.core.syntax;

import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.TSList;

public class GapAtomType extends BaseGapAtomType {
  public final String backType;

  public static class Config {
    public String id = "__gap";
    public String backType = "__gap";
    public ROList<FrontSpec> frontPrefix = ROList.empty;
    public ROList<FrontSpec> frontSuffix = ROList.empty;

    public Config() {}

    public Config(
        String id,
        String backType,
        ROList<FrontSpec> frontPrefix,
        ROList<FrontSpec> frontSuffix) {
      this.id = id;
      this.backType = backType;
      this.frontPrefix = frontPrefix;
      this.frontSuffix = frontSuffix;
    }
  }

  public GapAtomType(Config config) {
    super(
        new AtomType.Config(
            config.id,
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
                            GAP_PRIMITIVE_KEY)))
                .addAll(config.frontSuffix)));
    backType = config.backType;
  }

  @Override
  public String name() {
    return "Gap";
  }
}
