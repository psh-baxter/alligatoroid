package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.alignments.AlignmentSpec;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentSpec;
import com.zarbosoft.merman.syntax.back.BackArraySpec;
import com.zarbosoft.merman.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.syntax.back.BackFixedJSONSpecialPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackJSONSpecialPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BackSpec;
import com.zarbosoft.merman.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.syntax.back.BaseBackSimpleArraySpec;
import com.zarbosoft.merman.syntax.builder.BackFixedRecordSpecBuilder;
import com.zarbosoft.merman.syntax.builder.FrontArraySpecBuilder;
import com.zarbosoft.merman.syntax.builder.RootTypeBuilder;
import com.zarbosoft.merman.syntax.builder.StyleBuilder;
import com.zarbosoft.merman.syntax.builder.TypeBuilder;
import com.zarbosoft.merman.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.primitivepattern.Any;
import com.zarbosoft.merman.syntax.primitivepattern.JsonDecimal;
import com.zarbosoft.merman.syntax.primitivepattern.PatternCharacterClass;
import com.zarbosoft.merman.syntax.primitivepattern.PatternSequence;
import com.zarbosoft.merman.syntax.primitivepattern.PatternString;
import com.zarbosoft.merman.syntax.primitivepattern.PatternUnion;
import com.zarbosoft.merman.syntax.primitivepattern.Repeat1;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSSet;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;

public class Main {
  public static final String rawDoc =
      "{\n"
          + "  \"type\": \"Program\",\n"
          + "  \"start\": 0,\n"
          + "  \"end\": 192,\n"
          + "  \"body\": [\n"
          + "    {\n"
          + "      \"type\": \"ForStatement\",\n"
          + "      \"start\": 0,\n"
          + "      \"end\": 192,\n"
          + "      \"init\": {\n"
          + "        \"type\": \"VariableDeclaration\",\n"
          + "        \"start\": 5,\n"
          + "        \"end\": 14,\n"
          + "        \"declarations\": [\n"
          + "          {\n"
          + "            \"type\": \"VariableDeclarator\",\n"
          + "            \"start\": 9,\n"
          + "            \"end\": 14,\n"
          + "            \"id\": {\n"
          + "              \"type\": \"Identifier\",\n"
          + "              \"start\": 9,\n"
          + "              \"end\": 10,\n"
          + "              \"name\": \"i\"\n"
          + "            },\n"
          + "            \"init\": {\n"
          + "              \"type\": \"Literal\",\n"
          + "              \"start\": 13,\n"
          + "              \"end\": 14,\n"
          + "              \"value\": 1,\n"
          + "              \"raw\": \"1\"\n"
          + "            }\n"
          + "          }\n"
          + "        ],\n"
          + "        \"kind\": \"let\"\n"
          + "      },\n"
          + "      \"test\": {\n"
          + "        \"type\": \"BinaryExpression\",\n"
          + "        \"start\": 16,\n"
          + "        \"end\": 24,\n"
          + "        \"left\": {\n"
          + "          \"type\": \"Identifier\",\n"
          + "          \"start\": 16,\n"
          + "          \"end\": 17,\n"
          + "          \"name\": \"i\"\n"
          + "        },\n"
          + "        \"operator\": \"<=\",\n"
          + "        \"right\": {\n"
          + "          \"type\": \"Literal\",\n"
          + "          \"start\": 21,\n"
          + "          \"end\": 24,\n"
          + "          \"value\": 100,\n"
          + "          \"raw\": \"100\"\n"
          + "        }\n"
          + "      },\n"
          + "      \"update\": {\n"
          + "        \"type\": \"UpdateExpression\",\n"
          + "        \"start\": 26,\n"
          + "        \"end\": 29,\n"
          + "        \"operator\": \"++\",\n"
          + "        \"prefix\": true,\n"
          + "        \"argument\": {\n"
          + "          \"type\": \"Identifier\",\n"
          + "          \"start\": 28,\n"
          + "          \"end\": 29,\n"
          + "          \"name\": \"i\"\n"
          + "        }\n"
          + "      },\n"
          + "      \"body\": {\n"
          + "        \"type\": \"BlockStatement\",\n"
          + "        \"start\": 31,\n"
          + "        \"end\": 192,\n"
          + "        \"body\": [\n"
          + "          {\n"
          + "            \"type\": \"VariableDeclaration\",\n"
          + "            \"start\": 34,\n"
          + "            \"end\": 46,\n"
          + "            \"declarations\": [\n"
          + "              {\n"
          + "                \"type\": \"VariableDeclarator\",\n"
          + "                \"start\": 38,\n"
          + "                \"end\": 46,\n"
          + "                \"id\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 38,\n"
          + "                  \"end\": 41,\n"
          + "                  \"name\": \"out\"\n"
          + "                },\n"
          + "                \"init\": {\n"
          + "                  \"type\": \"Literal\",\n"
          + "                  \"start\": 44,\n"
          + "                  \"end\": 46,\n"
          + "                  \"value\": \"\",\n"
          + "                  \"raw\": \"\\\"\\\"\"\n"
          + "                }\n"
          + "              }\n"
          + "            ],\n"
          + "            \"kind\": \"let\"\n"
          + "          },\n"
          + "          {\n"
          + "            \"type\": \"IfStatement\",\n"
          + "            \"start\": 48,\n"
          + "            \"end\": 86,\n"
          + "            \"test\": {\n"
          + "              \"type\": \"BinaryExpression\",\n"
          + "              \"start\": 52,\n"
          + "              \"end\": 63,\n"
          + "              \"left\": {\n"
          + "                \"type\": \"BinaryExpression\",\n"
          + "                \"start\": 52,\n"
          + "                \"end\": 57,\n"
          + "                \"left\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 52,\n"
          + "                  \"end\": 53,\n"
          + "                  \"name\": \"i\"\n"
          + "                },\n"
          + "                \"operator\": \"%\",\n"
          + "                \"right\": {\n"
          + "                  \"type\": \"Literal\",\n"
          + "                  \"start\": 56,\n"
          + "                  \"end\": 57,\n"
          + "                  \"value\": 3,\n"
          + "                  \"raw\": \"3\"\n"
          + "                }\n"
          + "              },\n"
          + "              \"operator\": \"===\",\n"
          + "              \"right\": {\n"
          + "                \"type\": \"Literal\",\n"
          + "                \"start\": 62,\n"
          + "                \"end\": 63,\n"
          + "                \"value\": 0,\n"
          + "                \"raw\": \"0\"\n"
          + "              }\n"
          + "            },\n"
          + "            \"consequent\": {\n"
          + "              \"type\": \"BlockStatement\",\n"
          + "              \"start\": 65,\n"
          + "              \"end\": 86,\n"
          + "              \"body\": [\n"
          + "                {\n"
          + "                  \"type\": \"ExpressionStatement\",\n"
          + "                  \"start\": 69,\n"
          + "                  \"end\": 83,\n"
          + "                  \"expression\": {\n"
          + "                    \"type\": \"AssignmentExpression\",\n"
          + "                    \"start\": 69,\n"
          + "                    \"end\": 82,\n"
          + "                    \"operator\": \"+=\",\n"
          + "                    \"left\": {\n"
          + "                      \"type\": \"Identifier\",\n"
          + "                      \"start\": 69,\n"
          + "                      \"end\": 72,\n"
          + "                      \"name\": \"out\"\n"
          + "                    },\n"
          + "                    \"right\": {\n"
          + "                      \"type\": \"Literal\",\n"
          + "                      \"start\": 76,\n"
          + "                      \"end\": 82,\n"
          + "                      \"value\": \"Fizz\",\n"
          + "                      \"raw\": \"\\\"Fizz\\\"\"\n"
          + "                    }\n"
          + "                  }\n"
          + "                }\n"
          + "              ]\n"
          + "            },\n"
          + "            \"alternate\": null\n"
          + "          },\n"
          + "          {\n"
          + "            \"type\": \"IfStatement\",\n"
          + "            \"start\": 88,\n"
          + "            \"end\": 126,\n"
          + "            \"test\": {\n"
          + "              \"type\": \"BinaryExpression\",\n"
          + "              \"start\": 92,\n"
          + "              \"end\": 103,\n"
          + "              \"left\": {\n"
          + "                \"type\": \"BinaryExpression\",\n"
          + "                \"start\": 92,\n"
          + "                \"end\": 97,\n"
          + "                \"left\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 92,\n"
          + "                  \"end\": 93,\n"
          + "                  \"name\": \"i\"\n"
          + "                },\n"
          + "                \"operator\": \"%\",\n"
          + "                \"right\": {\n"
          + "                  \"type\": \"Literal\",\n"
          + "                  \"start\": 96,\n"
          + "                  \"end\": 97,\n"
          + "                  \"value\": 5,\n"
          + "                  \"raw\": \"5\"\n"
          + "                }\n"
          + "              },\n"
          + "              \"operator\": \"===\",\n"
          + "              \"right\": {\n"
          + "                \"type\": \"Literal\",\n"
          + "                \"start\": 102,\n"
          + "                \"end\": 103,\n"
          + "                \"value\": 0,\n"
          + "                \"raw\": \"0\"\n"
          + "              }\n"
          + "            },\n"
          + "            \"consequent\": {\n"
          + "              \"type\": \"BlockStatement\",\n"
          + "              \"start\": 105,\n"
          + "              \"end\": 126,\n"
          + "              \"body\": [\n"
          + "                {\n"
          + "                  \"type\": \"ExpressionStatement\",\n"
          + "                  \"start\": 109,\n"
          + "                  \"end\": 123,\n"
          + "                  \"expression\": {\n"
          + "                    \"type\": \"AssignmentExpression\",\n"
          + "                    \"start\": 109,\n"
          + "                    \"end\": 122,\n"
          + "                    \"operator\": \"+=\",\n"
          + "                    \"left\": {\n"
          + "                      \"type\": \"Identifier\",\n"
          + "                      \"start\": 109,\n"
          + "                      \"end\": 112,\n"
          + "                      \"name\": \"out\"\n"
          + "                    },\n"
          + "                    \"right\": {\n"
          + "                      \"type\": \"Literal\",\n"
          + "                      \"start\": 116,\n"
          + "                      \"end\": 122,\n"
          + "                      \"value\": \"Buzz\",\n"
          + "                      \"raw\": \"\\\"Buzz\\\"\"\n"
          + "                    }\n"
          + "                  }\n"
          + "                }\n"
          + "              ]\n"
          + "            },\n"
          + "            \"alternate\": null\n"
          + "          },\n"
          + "          {\n"
          + "            \"type\": \"IfStatement\",\n"
          + "            \"start\": 128,\n"
          + "            \"end\": 166,\n"
          + "            \"test\": {\n"
          + "              \"type\": \"BinaryExpression\",\n"
          + "              \"start\": 132,\n"
          + "              \"end\": 148,\n"
          + "              \"left\": {\n"
          + "                \"type\": \"MemberExpression\",\n"
          + "                \"start\": 132,\n"
          + "                \"end\": 142,\n"
          + "                \"object\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 132,\n"
          + "                  \"end\": 135,\n"
          + "                  \"name\": \"out\"\n"
          + "                },\n"
          + "                \"property\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 136,\n"
          + "                  \"end\": 142,\n"
          + "                  \"name\": \"length\"\n"
          + "                },\n"
          + "                \"computed\": false,\n"
          + "                \"optional\": false\n"
          + "              },\n"
          + "              \"operator\": \"===\",\n"
          + "              \"right\": {\n"
          + "                \"type\": \"Literal\",\n"
          + "                \"start\": 147,\n"
          + "                \"end\": 148,\n"
          + "                \"value\": 0,\n"
          + "                \"raw\": \"0\"\n"
          + "              }\n"
          + "            },\n"
          + "            \"consequent\": {\n"
          + "              \"type\": \"BlockStatement\",\n"
          + "              \"start\": 150,\n"
          + "              \"end\": 166,\n"
          + "              \"body\": [\n"
          + "                {\n"
          + "                  \"type\": \"ExpressionStatement\",\n"
          + "                  \"start\": 154,\n"
          + "                  \"end\": 163,\n"
          + "                  \"expression\": {\n"
          + "                    \"type\": \"AssignmentExpression\",\n"
          + "                    \"start\": 154,\n"
          + "                    \"end\": 162,\n"
          + "                    \"operator\": \"+=\",\n"
          + "                    \"left\": {\n"
          + "                      \"type\": \"Identifier\",\n"
          + "                      \"start\": 154,\n"
          + "                      \"end\": 157,\n"
          + "                      \"name\": \"out\"\n"
          + "                    },\n"
          + "                    \"right\": {\n"
          + "                      \"type\": \"Identifier\",\n"
          + "                      \"start\": 161,\n"
          + "                      \"end\": 162,\n"
          + "                      \"name\": \"i\"\n"
          + "                    }\n"
          + "                  }\n"
          + "                }\n"
          + "              ]\n"
          + "            },\n"
          + "            \"alternate\": null\n"
          + "          },\n"
          + "          {\n"
          + "            \"type\": \"ExpressionStatement\",\n"
          + "            \"start\": 168,\n"
          + "            \"end\": 190,\n"
          + "            \"expression\": {\n"
          + "              \"type\": \"CallExpression\",\n"
          + "              \"start\": 168,\n"
          + "              \"end\": 190,\n"
          + "              \"callee\": {\n"
          + "                \"type\": \"MemberExpression\",\n"
          + "                \"start\": 168,\n"
          + "                \"end\": 179,\n"
          + "                \"object\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 168,\n"
          + "                  \"end\": 175,\n"
          + "                  \"name\": \"console\"\n"
          + "                },\n"
          + "                \"property\": {\n"
          + "                  \"type\": \"Identifier\",\n"
          + "                  \"start\": 176,\n"
          + "                  \"end\": 179,\n"
          + "                  \"name\": \"log\"\n"
          + "                },\n"
          + "                \"computed\": false,\n"
          + "                \"optional\": false\n"
          + "              },\n"
          + "              \"arguments\": [\n"
          + "                {\n"
          + "                  \"type\": \"BinaryExpression\",\n"
          + "                  \"start\": 180,\n"
          + "                  \"end\": 189,\n"
          + "                  \"left\": {\n"
          + "                    \"type\": \"Identifier\",\n"
          + "                    \"start\": 180,\n"
          + "                    \"end\": 183,\n"
          + "                    \"name\": \"out\"\n"
          + "                  },\n"
          + "                  \"operator\": \"+\",\n"
          + "                  \"right\": {\n"
          + "                    \"type\": \"Literal\",\n"
          + "                    \"start\": 186,\n"
          + "                    \"end\": 189,\n"
          + "                    \"value\": \" \",\n"
          + "                    \"raw\": \"\\\" \\\"\"\n"
          + "                  }\n"
          + "                }\n"
          + "              ],\n"
          + "              \"optional\": false\n"
          + "            }\n"
          + "          }\n"
          + "        ]\n"
          + "      }\n"
          + "    }\n"
          + "  ],\n"
          + "  \"sourceType\": \"script\"\n"
          + "}\n";

  public static final int indentPx = 16;
  public static final int spacePx = 4;

  public static final String baseAlign = "base";
  public static final String indentAlign = "indent";

  public static final String statementGroupType = "statement";
  public static final String accessGroupType = "access";
  public static final String expresisonGroupType = "expression";
  public static final String literalGroupType = "literal";

  public static final String declareType = "declare";
  public static final String letType = "let";
  public static final String stringLiteralType = "string_literal";
  public static final String symbolLiteralType = "symbol_literal";
  public static final String identifierType = "identifier";
  public static final String memberType = "member";
  public static final String forType = "for";
  public static final String ifType = "if";
  public static final String blockType = "block";
  public static final String preincrementOperatorType = "preincrement";
  public static final String lessThanEqualOperatorType = "less_than_equal";
  public static final String moduloOperatorType = "modulo";
  public static final String addEqualOperatorType = "add_equal";
  public static final String tripleEqualOperatorType = "triple_equal";

  public static final String breakPrefixTag = "es_break";
  public static final FrontSymbol breakPrefix =
      new FrontSymbol(
          new FrontSymbol.Config(new SymbolSpaceSpec(), null, "", TSSet.of(breakPrefixTag).ro()));
  public static final String closeBreakPrefixTag = "es_close_break";
  public static final FrontSymbol closeBreakPrefix =
      new FrontSymbol(
          new FrontSymbol.Config(
              new SymbolSpaceSpec(), null, "", TSSet.of(closeBreakPrefixTag).ro()));
  private static final String spaceBreakPrefixTag = "es_space_break";
  public static final FrontSymbol spaceBreakPrefix =
      new FrontSymbol(
          new FrontSymbol.Config(
              new SymbolSpaceSpec(), null, "", TSSet.of(spaceBreakPrefixTag).ro()));
  private static final String spaceTag = "es_space";
  public static final FrontSymbol space =
      new FrontSymbol(
          new FrontSymbol.Config(new SymbolSpaceSpec(), null, "", TSSet.of(spaceTag).ro()));

  @JsMethod
  public static void main() {
    JSI18nEngine i18n = new JSI18nEngine("en");

    FrontArraySpec statementsFront =
        new FrontArraySpecBuilder("statements")
            .prefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(), null, "", TSSet.of("statement_start").ro())))
            .separator(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(), null, "", TSSet.of("break").ro())))
            .build();
    AlignmentSpec blockAlignment =
        new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(baseAlign, indentPx, true));
    BackArraySpec statementBackArray =
        new BackArraySpec(
            new BaseBackSimpleArraySpec.Config(
                "statements",
                statementGroupType,
                TSList.of(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("ExpressionStatement"))
                        .field(
                            "expression",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config(null, expresisonGroupType)))
                        .build())));

    TSList<AtomType> types =
        TSList.of(
            new TypeBuilder(blockType, "Block")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("BlockStatement"))
                        .field("body", statementBackArray)
                        .build())
                .alignment(baseAlign, blockAlignment)
                .front(text("{"))
                .front(new FrontArraySpecBuilder("body").prefix(spaceBreakPrefix).build())
                .front(closeBreakPrefix)
                .front(text("}"))
                .build(),
            //
            /// Declaration
            new TypeBuilder(letType, "Declare - let (outer)")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("VariableDeclaration"))
                        .field("kind", new BackFixedPrimitiveSpec("let"))
                        .field(
                            "declarations",
                            new BackArraySpec(
                                new BaseBackSimpleArraySpec.Config(
                                    "declarations", letType, new TSList<>())))
                        .build())
                .front(
                    new FrontArraySpecBuilder("declarations")
                        .separator(text(","))
                        .separator(spaceBreakPrefix)
                        .build())
                .build(),
            new TypeBuilder(letType, "Declare (inner)")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("VariableDeclarator"))
                        .field("id", identifierBack(i18n, "id"))
                        .field(
                            "init",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("init", expresisonGroupType)))
                        .build())
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("id")))
                .front(space)
                .front(text("="))
                .front(spaceBreakPrefix)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("init")))
                .build(),
            //
            /// Access
            new TypeBuilder(identifierType, "Identifier")
                .back(identifierBack(i18n, "name"))
                .front(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config("name", ROSet.empty)))
                .build(),
            new TypeBuilder(memberType, "Member")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("MemberExpression"))
                        .field(
                            "object",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("object", expresisonGroupType)))
                        .field("property", identifierBack(i18n, "property"))
                        .build())
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("object")))
                .front(text("."))
                .front(breakPrefix)
                .front(
                    new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config("property", ROSet.empty)))
                .build(),
            //
            /// Literal
            new TypeBuilder(symbolLiteralType, "Symbol literal")
                .back(
                    estreeBackBuilder()
                        .discardField("value")
                        .field("type", new BackFixedPrimitiveSpec("Literal"))
                        .field(
                            "value",
                            new BackJSONSpecialPrimitiveSpec(
                                i18n,
                                new BaseBackPrimitiveSpec.Config(
                                    "value",
                                    new PatternUnion(
                                        TSList.of(
                                            new PatternString("true"),
                                            new PatternString("false"),
                                            new PatternString("null"),
                                            new JsonDecimal())))))
                        .build())
                .front(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config("value", ROSet.empty)))
                .build(),
            new TypeBuilder(stringLiteralType, "String literal")
                .back(
                    estreeBackBuilder()
                        .discardField("value")
                        .field("type", new BackFixedPrimitiveSpec("Literal"))
                        .field(
                            "value",
                            new BackPrimitiveSpec(
                                i18n, new BaseBackPrimitiveSpec.Config("value", new Any())))
                        .build())
                .front(text("\""))
                .front(new FrontPrimitiveSpec(new FrontPrimitiveSpec.Config("value", ROSet.empty)))
                .front(text("\""))
                .build(),
            //
            /// Control
            new TypeBuilder(forType, "For")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("ForStatement"))
                        .field(
                            "init",
                            new BackAtomSpec(new BaseBackAtomSpec.Config("init", declareType)))
                        .field(
                            "test",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("test", expresisonGroupType)))
                        .field(
                            "update",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("update", expresisonGroupType)))
                        .field(
                            "body",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("body", statementGroupType)))
                        .build())
                .front(text("for ("))
                .front(breakPrefix)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("init")))
                .front(text(";"))
                .front(spaceBreakPrefix)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("test")))
                .front(text(";"))
                .front(spaceBreakPrefix)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("update")))
                .front(text(")"))
                .front(space)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("body")))
                .build(),
            new TypeBuilder(ifType, "If")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("IfStatement"))
                        .field(
                            "test",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("test", expresisonGroupType)))
                        .field(
                            "consequent",
                            new BackAtomSpec(
                                new BaseBackAtomSpec.Config("consequent", statementGroupType)))
                        .field("alternate", new BackFixedJSONSpecialPrimitiveSpec("null"))
                        .build())
                .front(text("if ("))
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("test")))
                .front(text(")"))
                .front(spaceBreakPrefix)
                .front(new FrontAtomSpec(new FrontAtomSpec.Config("consequent")))
                .build(),
            //
            /// Operators
            prefixOperator(preincrementOperatorType, "++", "UpdateExpression", accessGroupType),
            binaryOperator(
                lessThanEqualOperatorType,
                "<=",
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            binaryOperator(
                moduloOperatorType,
                "%",
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            binaryOperator(
                addEqualOperatorType,
                "+=",
                "AssignmentExpression",
                accessGroupType,
                expresisonGroupType),
            binaryOperator(
                tripleEqualOperatorType,
                "===",
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType));
    TSMap<String, ROList<String>> groups =
        new TSMap<>(
            m ->
                m.put(
                        statementGroupType,
                        TSList.of(declareType, forType, ifType, blockType, expresisonGroupType))
                    .put(accessGroupType, TSList.of(identifierType, memberType))
                    .put(
                        expresisonGroupType,
                        TSList.of(
                            literalGroupType,
                            accessGroupType,
                            preincrementOperatorType,
                            lessThanEqualOperatorType,
                            moduloOperatorType,
                            addEqualOperatorType,
                            tripleEqualOperatorType))
                    .put(literalGroupType, TSList.of(symbolLiteralType, stringLiteralType)));
    MultiError errors = new MultiError();
    TSMap<String, ROSet<AtomType>> splayedTypes = Syntax.splayGroups(errors, types, groups);
    Syntax.Config syntaxConfig =
        new Syntax.Config(
            i18n,
            types,
            splayedTypes,
            new RootTypeBuilder()
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("Program"))
                        .field("sourceType", new BackFixedPrimitiveSpec("script"))
                        .field("body", statementBackArray)
                        .build())
                .alignment(baseAlign, blockAlignment)
                .front(statementsFront)
                .build());
    syntaxConfig.styles =
        TSList.of(
            new StyleBuilder().with(breakPrefixTag).with(Tags.TAG_COMPACT).split().build(),
            new StyleBuilder()
                .with(closeBreakPrefixTag)
                .with(Tags.TAG_COMPACT)
                .split()
                .align(baseAlign)
                .build(),
            new StyleBuilder()
                .with(spaceBreakPrefixTag)
                .without(Tags.TAG_COMPACT)
                .space(spacePx)
                .build(),
            new StyleBuilder()
                .with(spaceBreakPrefixTag)
                .with(Tags.TAG_COMPACT)
                .split()
                .align(indentAlign)
                .split()
                .build(),
            new StyleBuilder().with(spaceTag).space(spacePx).build());
    DomGlobal.document.body.appendChild(
        new JSSourceView(new Syntax(syntaxConfig), i18n, rawDoc).element);
  }

  private static FreeAtomType binaryOperator(
      String id, String symbol, String esType, String leftChildType, String rightChildType) {
    return new TypeBuilder(id, symbol + " operator")
        .back(
            estreeBackBuilder()
                .field("type", new BackFixedPrimitiveSpec(esType))
                .field("left", new BackAtomSpec(new BaseBackAtomSpec.Config("left", leftChildType)))
                .field(
                    "right", new BackAtomSpec(new BaseBackAtomSpec.Config("right", rightChildType)))
                .build())
        .front(new FrontAtomSpec(new FrontAtomSpec.Config("left")))
        .front(space)
        .front(text(symbol))
        .front(spaceBreakPrefix)
        .front(new FrontAtomSpec(new FrontAtomSpec.Config("right")))
        .build();
  }

  private static FreeAtomType prefixOperator(
      String id, String symbol, String esType, String childType) {
    return new TypeBuilder(id, symbol + " operator")
        .back(
            estreeBackBuilder()
                .field("type", new BackFixedPrimitiveSpec(esType))
                .field("prefix", new BackFixedJSONSpecialPrimitiveSpec("true"))
                .field(
                    "argument",
                    new BackAtomSpec(new BaseBackAtomSpec.Config("argument", childType)))
                .build())
        .front(text(symbol))
        .front(new FrontAtomSpec(new FrontAtomSpec.Config("argument")))
        .build();
  }

  private static BackSpec identifierBack(JSI18nEngine i18n, String id) {
    return estreeBackBuilder()
        .field("type", new BackFixedPrimitiveSpec("Identifier"))
        .field(
            "name",
            new BackPrimitiveSpec(
                i18n,
                new BaseBackPrimitiveSpec.Config(
                    id,
                    new PatternSequence(
                        TSList.of(
                            new PatternCharacterClass(
                                TSList.of(
                                    new ROPair<>("$", "$"),
                                    new ROPair<>("_", "_"),
                                    new ROPair<>("a", "z"),
                                    new ROPair<>("A", "Z"))),
                            new Repeat1(
                                new PatternCharacterClass(
                                    TSList.of(
                                        new ROPair<>("$", "$"),
                                        new ROPair<>("_", "_"),
                                        new ROPair<>("a", "z"),
                                        new ROPair<>("A", "Z"),
                                        new ROPair<>("0", "9")))))))))
        .build();
  }

  private static FrontSymbol text(String text) {
    return new FrontSymbol(new FrontSymbol.Config(new SymbolTextSpec(text), null, "", ROSet.empty));
  }

  private static BackFixedRecordSpecBuilder estreeBackBuilder() {
    return new BackFixedRecordSpecBuilder()
        .discardField("start")
        .discardField("end")
        .discardField("raw");
  }
}
