package com.zarbosoft.merman.core.syntax.front;

public class FrontArraySpec extends FrontArraySpecBase {
  private final String fieldId;

  public static class Config {
    public final String fieldId;
    public final FrontArraySpecBase.Config base;

    public Config(String fieldId, FrontArraySpecBase.Config base) {
      this.fieldId = fieldId;
      this.base = base;
    }
  }

  public FrontArraySpec(Config config) {
    super(config.base);
    this.fieldId = config.fieldId;
  }

  @Override
  public String fieldId() {
    return fieldId;
  }
}
