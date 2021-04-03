package com.zarbosoft.merman.core.example;

import com.zarbosoft.merman.core.editor.I18nEngine;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.core.syntax.alignments.RelativeAlignmentSpec;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedJSONSpecialPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackJSONSpecialPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackKeySpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.core.syntax.builder.FrontArraySpecBuilder;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbol;
import com.zarbosoft.merman.core.syntax.primitivepattern.Any;
import com.zarbosoft.merman.core.syntax.primitivepattern.Integer;
import com.zarbosoft.merman.core.syntax.primitivepattern.JsonDecimal;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;

public class JsonSyntax {
  private static final String DEFAULT_ID = "default";
  private static final String GROUP_ANY = "any";
  private static final String TYPE_NULL = "null";
  private static final String TYPE_TRUE = "true";
  private static final String TYPE_FALSE = "false";
  private static final String TYPE_STRING = "string";
  private static final String TYPE_INT = "int";
  private static final String TYPE_DECIMAL = "decimal";
  private static final String TYPE_RECORD = "record";
  private static final String TYPE_RECORD_PAIR = "record_pair";
  private static final String TYPE_ARRAY = "array";
  private static final String ALIGNMENT_INDENT = "indent";
  private static final String ALIGNMENT_BASE = "base";

  public static Syntax create(I18nEngine i18n, Padding pad) {
    double fontSize = 5;
    final Style stringStyle =
        new Style.Config().fontSize(fontSize).color(ModelColor.RGB.hex("F79578")).create();
    final Style numberStyle =
        new Style.Config().fontSize(fontSize).color(ModelColor.RGB.hex("95FA94")).create();
    final Style specialStyle =
        new Style.Config().fontSize(fontSize).color(ModelColor.RGB.hex("7DD4FB")).create();
    final Style symbolStyle =
        new Style.Config().fontSize(fontSize).color(ModelColor.RGB.hex("CACACA")).create();
    final Style baseAlignSymbolStyle =
        new Style.Config()
            .fontSize(fontSize)
            .color(ModelColor.RGB.hex("CACACA"))
            .splitAlignment(ALIGNMENT_BASE)
            .create();
    TSMap<String, AlignmentSpec> containerAlignments =
        new TSMap<String, AlignmentSpec>()
            .put(
                ALIGNMENT_BASE,
                new RelativeAlignmentSpec(
                    new RelativeAlignmentSpec.Config(ALIGNMENT_INDENT, 0, false)))
            .put(
                ALIGNMENT_INDENT,
                new RelativeAlignmentSpec(
                    new RelativeAlignmentSpec.Config(ALIGNMENT_INDENT, 20, true)));
    FrontSymbol breakIndent =
        new FrontSymbol(
            new FrontSymbol.Config(
                new SymbolSpaceSpec(
                    new SymbolSpaceSpec.Config()
                        .splitMode(Style.SplitMode.COMPACT)
                        .style(new Style.Config().splitAlignment(ALIGNMENT_INDENT).create()))));
    TSList<AtomType> types =
        TSList.of(
            new FreeAtomType(
                new FreeAtomType.Config(
                    "null",
                    new AtomType.Config(
                        TYPE_NULL,
                        TSList.of(new BackFixedJSONSpecialPrimitiveSpec("null")),
                        TSList.of(textSym("null", specialStyle))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "true",
                    new AtomType.Config(
                        TYPE_TRUE,
                        TSList.of(new BackFixedJSONSpecialPrimitiveSpec("true")),
                        TSList.of(textSym("true", specialStyle))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "false",
                    new AtomType.Config(
                        TYPE_FALSE,
                        TSList.of(new BackFixedJSONSpecialPrimitiveSpec("false")),
                        TSList.of(textSym("false", specialStyle))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "string",
                    new AtomType.Config(
                        TYPE_STRING,
                        TSList.of(
                            new BackPrimitiveSpec(
                                new BaseBackPrimitiveSpec.Config(DEFAULT_ID, Any.repeatedAny))),
                        TSList.of(
                            textSym("\"", stringStyle),
                            new FrontPrimitiveSpec(
                                new FrontPrimitiveSpec.Config(DEFAULT_ID).style(stringStyle)),
                            textSym("\"", stringStyle))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "int",
                    new AtomType.Config(
                        TYPE_INT,
                        TSList.of(
                            new BackJSONSpecialPrimitiveSpec(
                                new BaseBackPrimitiveSpec.Config(DEFAULT_ID, new Integer()))),
                        TSList.of(
                            new FrontPrimitiveSpec(
                                new FrontPrimitiveSpec.Config(DEFAULT_ID).style(numberStyle)))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "decimal",
                    new AtomType.Config(
                        TYPE_DECIMAL,
                        TSList.of(
                            new BackJSONSpecialPrimitiveSpec(
                                new BaseBackPrimitiveSpec.Config(DEFAULT_ID, new JsonDecimal()))),
                        TSList.of(
                            new FrontPrimitiveSpec(
                                new FrontPrimitiveSpec.Config(DEFAULT_ID).style(numberStyle)))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                        "record",
                        new AtomType.Config(
                            TYPE_RECORD,
                            TSList.of(
                                new BackRecordSpec(
                                    new BackRecordSpec.Config(DEFAULT_ID, TYPE_RECORD_PAIR))),
                            TSList.of(
                                textSym("{", symbolStyle),
                                new FrontArraySpecBuilder(DEFAULT_ID)
                                    .prefix(breakIndent)
                                    .separator(textSym(", ", symbolStyle))
                                    .build(),
                                baseAlignTextSym("}", baseAlignSymbolStyle))))
                    .alignments(containerAlignments)),
            new FreeAtomType(
                new FreeAtomType.Config(
                    "record element",
                    new AtomType.Config(
                        TYPE_RECORD_PAIR,
                        TSList.of(
                            new BackKeySpec(
                                new BaseBackPrimitiveSpec.Config("key", Any.repeatedAny)),
                            new BackAtomSpec(new BaseBackAtomSpec.Config("value", GROUP_ANY))),
                        TSList.of(
                            new FrontPrimitiveSpec(
                                new FrontPrimitiveSpec.Config("key").style(symbolStyle)),
                            textSym(": ", symbolStyle),
                            new FrontAtomSpec(new FrontAtomSpec.Config("value")))))),
            new FreeAtomType(
                new FreeAtomType.Config(
                        "array",
                        new AtomType.Config(
                            TYPE_ARRAY,
                            TSList.of(
                                new BackArraySpec(
                                    new BaseBackSimpleArraySpec.Config(
                                        DEFAULT_ID, GROUP_ANY, TSList.of()))),
                            TSList.of(
                                textSym("[", symbolStyle),
                                new FrontArraySpecBuilder(DEFAULT_ID)
                                    .prefix(breakIndent)
                                    .separator(textSym(", ", symbolStyle))
                                    .build(),
                                baseAlignTextSym("]", baseAlignSymbolStyle))))
                    .alignments(containerAlignments)));
    TSMap<String, ROSet<AtomType>> splayedTypes;
    {
      MultiError errors = new MultiError();
      splayedTypes =
          Syntax.splayGroups(
              errors,
              types,
              new TSMap<String, ROList<String>>()
                  .put(
                      GROUP_ANY,
                      TSList.of(
                          TYPE_RECORD,
                          TYPE_ARRAY,
                          TYPE_STRING,
                          TYPE_TRUE,
                          TYPE_FALSE,
                          TYPE_NULL,
                          TYPE_INT,
                          TYPE_DECIMAL)));
      errors.raise();
    }
    return new Syntax(
        i18n,
        new Syntax.Config(
                types,
                splayedTypes,
                new RootAtomType(
                    new RootAtomType.Config(
                        TSList.of(
                            new BackAtomSpec(new BaseBackAtomSpec.Config(DEFAULT_ID, GROUP_ANY))),
                        TSList.of(new FrontAtomSpec(new FrontAtomSpec.Config(DEFAULT_ID))),
                        ROMap.empty)))
            .displayUnit(Syntax.DisplayUnit.MM)
            .background(ModelColor.RGB.hex("333333"))
            .hoverStyle(hoverStyle(false))
            .primitiveHoverStyle(hoverStyle(true))
            .cursorStyle(cursorStyle(false))
            .primitiveCursorStyle(cursorStyle(true))
            .pad(pad));
  }

  private static Style cursorStyle(boolean primitive) {
    return new Style.Config()
        .obbox(
            new ObboxStyle(
                new ObboxStyle.Config()
                    .padding(primitive ? new Padding(0, 0, 1, 1) : Padding.same(1))
                    .roundStart(true)
                    .roundEnd(true)
                    .lineThickness(0.3)
                    .roundRadius(3)
                    .lineColor(ModelColor.RGB.white)))
        .create();
  }

  private static Style hoverStyle(boolean primitive) {
    return new Style.Config()
        .obbox(
            new ObboxStyle(
                new ObboxStyle.Config()
                    .padding(primitive ? new Padding(0, 0, 1, 1) : Padding.same(1))
                    .roundEnd(true)
                    .roundStart(true)
                    .lineThickness(0.3)
                    .roundRadius(3)
                    .lineColor(ModelColor.RGB.hex("888888"))))
        .create();
  }

  public static FrontSymbol textSym(String s, Style style) {
    return new FrontSymbol(
        new FrontSymbol.Config(new SymbolTextSpec(new SymbolTextSpec.Config(s).style(style))));
  }

  public static FrontSymbol baseAlignTextSym(String s, Style style) {
    return new FrontSymbol(
        new FrontSymbol.Config(
            new SymbolTextSpec(
                new SymbolTextSpec.Config(s).splitMode(Style.SplitMode.COMPACT).style(style))));
  }
}
