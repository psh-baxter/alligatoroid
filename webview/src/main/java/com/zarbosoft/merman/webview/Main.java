package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.misc.MultiError;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.BackType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
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
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.events.Position;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.annotations.JsMethod;

import java.util.function.Function;

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

  public static final int indentPx = 32;
  public static final int fontSize = 15;
  public static final int lineSpace = 4;

  public static final String baseAlign = "base";
  public static final String indentAlign = "indent";

  /*
  public static final ModelColor.RGB stringColor = ModelColor.RGB.hex("C25E8C");
  public static final ModelColor.RGB stringQuoteColor = ModelColor.RGB.hex("E76F82");
  public static final ModelColor.RGB nonstringColor = ModelColor.RGB.hex("FFAB66");
  public static final ModelColor.RGB identifierColor = ModelColor.RGB.hex("9F66B3");
  public static final ModelColor.RGB keywordColor = ModelColor.RGB.hex("4C0027");
  public static final ModelColor.RGB declareColor = ModelColor.RGB.hex("5175CE");
  public static final ModelColor.RGB blockColor = ModelColor.RGB.hex("00857A");
  public static final ModelColor.RGB operatorColor = ModelColor.RGB.hex("0087B0");
  public static final ModelColor.RGB callColor = ModelColor.RGB.hex("554149");
   */
  public static final double bright = 0.7;
  public static final double dark = 0.25;
  public static final double semidark = 0.35;
  public static final double subbright = 0.6;
  public static final double sat = 0.4;
  public static final double unsat = 0.2;
  public static final ModelColor.RGB stringColor =
      ModelColor.RGB.polarOKLab(bright, unsat, 100 + 60);
  public static final ModelColor.RGB stringQuoteColor =
      ModelColor.RGB.polarOKLab(subbright, unsat, 100 + 70);
  public static final ModelColor.RGB nonstringColor =
      ModelColor.RGB.polarOKLab(bright, unsat, 100 + 240);
  public static final ModelColor.RGB identifierColor =
      ModelColor.RGB.polarOKLab(bright, unsat, 100 + 120);
  public static final ModelColor.RGB declareColor =
      ModelColor.RGB.polarOKLab(semidark, unsat, 100 + 150);
  public static final ModelColor.RGB keywordColor = ModelColor.RGB.polarOKLab(dark, sat, 0);
  public static final ModelColor.RGB blockColor = ModelColor.RGB.polarOKLab(dark, sat, 30);
  public static final ModelColor.RGB operatorColor = ModelColor.RGB.polarOKLab(dark, sat, 60);
  public static final ModelColor.RGB callColor = ModelColor.RGB.polarOKLab(dark, sat, 90);
  public static final String statementGroupType = "statement";
  public static final String accessGroupType = "access";
  public static final String expresisonGroupType = "expression";
  public static final String literalGroupType = "literal";
  public static final String declareGroupType = "declare";
  public static final String letType = "let";
  public static final String declareInner = "declare_inner";
  public static final String stringLiteralType = "string_literal";
  public static final String symbolLiteralType = "symbol_literal";
  public static final String identifierType = "identifier";
  public static final String memberType = "member";
  public static final String callType = "call";
  public static final String forType = "for";
  public static final String ifType = "if";
  public static final String blockType = "block";
  public static final String preincrementOperatorType = "preincrement";
  public static final String addOperatorType = "add";
  public static final String lessThanEqualOperatorType = "less_than_equal";
  public static final String moduloOperatorType = "modulo";
  public static final String addEqualOperatorType = "add_equal";
  public static final String tripleEqualOperatorType = "triple_equal";
  public static final FrontSymbol prefixCompactIndent =
      new FrontSymbol(
          new FrontSymbol.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.COMPACT)
                      .style(new Style.Config().splitAlignment(indentAlign).create()))));
  public static final FrontSymbol prefixIndent =
      new FrontSymbol(
          new FrontSymbol.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.ALWAYS)
                      .style(new Style.Config().splitAlignment(indentAlign).create()))));
  public static final FrontSymbol space = text(" ", Function.identity());

  private static Style.Config styleBase(Style.Config c) {
    return c.spaceTransverseBefore(lineSpace).spaceTransverseAfter(lineSpace).fontSize(fontSize);
  }

  private static Style.Config styleString(Style.Config c) {
    return styleBase(c).color(stringColor);
  }

  private static Style.Config styleNonstring(Style.Config c) {
    return styleBase(c).color(nonstringColor);
  }

  private static Style.Config styleStringQuote(Style.Config c) {
    return styleBase(c).color(stringQuoteColor);
  }

  private static Style.Config styleIdentifier(Style.Config c) {
    return styleBase(c).color(identifierColor);
  }

  private static Style.Config styleOperator(Style.Config c) {
    return styleBase(c).color(operatorColor);
  }

  private static Style.Config styleCall(Style.Config c) {
    return styleBase(c).color(callColor);
  }

  private static Style.Config styleKeyword(Style.Config c) {
    return styleBase(c).color(keywordColor);
  }

  private static Style.Config styleDeclare(Style.Config c) {
    return styleBase(c).color(declareColor);
  }

  private static Style.Config styleBlock(Style.Config c) {
    return styleBase(c).color(blockColor);
  }

  @JsMethod
  public static void main() {
    try {
      {
        Element ast = DomGlobal.document.createElement("code");
        ast.classList.add("block");
        ast.textContent = rawDoc;
        DomGlobal.document.getElementById("replace-javascript-ast").replaceWith(ast);
      }
      WebView webView = new WebView();
      JSI18nEngine i18n = new JSI18nEngine("en");
      for (ROPair<String, SyntaxFrontFactory> p :
          new ROPair[] {
            new ROPair<>("javascript", new JavascriptSyntaxFrontFactory()),
            new ROPair<>("python", new PythonSyntaxFrontFactory()),
            new ROPair<>("lisp", new LispSyntaxFrontFactory())
          }) {
        DomGlobal.document
            .getElementById("replace-" + p.first)
            .replaceWith(
                webView.block(
                    buildSyntax(i18n, p.second),
                    i18n,
                    rawDoc,
                    TSList.of("type", "operator", "kind")));
      }
    } catch (GrammarTooUncertain e) {
      StringBuilder message = new StringBuilder();
      for (Parse.State leaf : e.context.leaves) {
        message.append(Format.format(" * %s (%s)\n", leaf, leaf.color()));
      }
      throw new RuntimeException(
          Format.format(
              "Too much uncertainty while parsing!\nat %s %s\n%s branches:\n%s",
              ((Position) e.position).at, ((Position) e.position).event, message.toString()));
    } catch (InvalidStream e) {
      StringBuilder message = new StringBuilder();
      for (MismatchCause error : e.state.errors) {
        message.append(Format.format(" * %s\n", error));
      }
      throw new RuntimeException(
          Format.format(
              "Document doesn't conform to syntax tree\nat %s %s\nexpected:\n%s",
              ((Position) e.position).at, ((Position) e.position).event, message.toString()));
    } catch (RuntimeException e) {
      throw new RuntimeException("\n" + e.toString());
    }
  }

  private static Syntax buildSyntax(JSI18nEngine i18n, SyntaxFrontFactory frontFactory) {
    FrontArraySpec statementsFront =
        new FrontArraySpecBuilder("statements")
            .prefix(
                new FrontSymbol(
                    new FrontSymbol.Config(
                        new SymbolSpaceSpec(
                            new SymbolSpaceSpec.Config().splitMode(Style.SplitMode.ALWAYS)))))
            .build();
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
            frontFactory
                .block(
                    estreeTypeBuilder(blockType, "Block")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("BlockStatement"))
                                .field("body", statementBackArray)
                                .build()),
                    "statements")
                .precedence(0)
                .build(),
            //
            /// Declaration
            frontFactory
                .declareOuter(
                    estreeTypeBuilder(letType, "Declare - let (outer)")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("VariableDeclaration"))
                                .field("kind", new BackFixedPrimitiveSpec("let"))
                                .field(
                                    "declarations",
                                    new BackArraySpec(
                                        new BaseBackSimpleArraySpec.Config(
                                            "declarations", declareInner, new TSList<>())))
                                .build()),
                    "declarations")
                .precedence(10)
                .build(),
            frontFactory
                .declareInner(
                    estreeTypeBuilder(declareInner, "Declare (inner)")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("VariableDeclarator"))
                                .field("id", identifierBack(i18n, "id"))
                                .field(
                                    "init",
                                    new BackAtomSpec(
                                        new BaseBackAtomSpec.Config("init", expresisonGroupType)))
                                .build()),
                    "id",
                    "init")
                .build(),
            //
            /// Access
            estreeTypeBuilder(identifierType, "Identifier")
                .back(identifierBack(i18n, "name"))
                .front(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config("name").style(c -> styleIdentifier(c))))
                .build(),
            frontFactory
                .memberExpr(
                    estreeTypeBuilder(memberType, "Member")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("MemberExpression"))
                                .field(
                                    "object",
                                    new BackAtomSpec(
                                        new BaseBackAtomSpec.Config("object", expresisonGroupType)))
                                .field("property", identifierBack(i18n, "property"))
                                .field("computed", new BackFixedJSONSpecialPrimitiveSpec("false"))
                                .field("optional", new BackFixedJSONSpecialPrimitiveSpec("false"))
                                .build()),
                    "object",
                    "property")
                .precedence(200)
                .build(),
            //
            /// Literal
            estreeTypeBuilder(symbolLiteralType, "Symbol literal")
                .back(
                    estreeBackBuilder()
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
                .front(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config("value").style(c -> styleNonstring(c))))
                .build(),
            estreeTypeBuilder(stringLiteralType, "String literal")
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec("Literal"))
                        .field(
                            "value",
                            new BackPrimitiveSpec(
                                i18n, new BaseBackPrimitiveSpec.Config("value", new Any())))
                        .build())
                .front(text("\"", Main::styleStringQuote))
                .front(
                    new FrontPrimitiveSpec(
                        new FrontPrimitiveSpec.Config("value").style(c -> styleString(c))))
                .front(text("\"", Main::styleStringQuote))
                .build(),
            //
            /// Control
            frontFactory
                .forStatement(
                    estreeTypeBuilder(forType, "For")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("ForStatement"))
                                .field(
                                    "init",
                                    new BackAtomSpec(
                                        new BaseBackAtomSpec.Config("init", declareGroupType)))
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
                                .build()),
                    "init",
                    "test",
                    "update",
                    "body")
                .build(),
            frontFactory
                .ifStatement(
                    estreeTypeBuilder(ifType, "If")
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
                                        new BaseBackAtomSpec.Config(
                                            "consequent", statementGroupType)))
                                .field("alternate", new BackFixedJSONSpecialPrimitiveSpec("null"))
                                .build()),
                    "test",
                    "consequent")
                .build(),
            //
            /// Operators
            prefixOperator(
                frontFactory,
                preincrementOperatorType,
                "++",
                170,
                "UpdateExpression",
                accessGroupType),
            binaryOperator(
                frontFactory,
                addOperatorType,
                "+",
                140,
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            binaryOperator(
                frontFactory,
                lessThanEqualOperatorType,
                "<=",
                120,
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            binaryOperator(
                frontFactory,
                moduloOperatorType,
                "%",
                150,
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            binaryOperator(
                frontFactory,
                addEqualOperatorType,
                "+=",
                30,
                "AssignmentExpression",
                accessGroupType,
                expresisonGroupType),
            binaryOperator(
                frontFactory,
                tripleEqualOperatorType,
                "===",
                110,
                "BinaryExpression",
                expresisonGroupType,
                expresisonGroupType),
            //
            /// Other expressions
            frontFactory
                .callExpr(
                    estreeTypeBuilder(callType, "Call")
                        .back(
                            estreeBackBuilder()
                                .field("type", new BackFixedPrimitiveSpec("CallExpression"))
                                .field(
                                    "callee",
                                    new BackAtomSpec(
                                        new BaseBackAtomSpec.Config("callee", expresisonGroupType)))
                                .field(
                                    "arguments",
                                    new BackArraySpec(
                                        new BaseBackSimpleArraySpec.Config(
                                            "arguments", expresisonGroupType, new TSList<>())))
                                .field("optional", new BackFixedJSONSpecialPrimitiveSpec("false"))
                                .build()),
                    "callee",
                    "arguments")
                .precedence(0)
                .build());
    TSMap<String, ROList<String>> groups =
        new TSMap<>(
            m ->
                m.put(
                        statementGroupType,
                        TSList.of(
                            declareGroupType, forType, ifType, blockType, expresisonGroupType))
                    .put(accessGroupType, TSList.of(identifierType, memberType))
                    .put(declareGroupType, TSList.of(letType))
                    .put(
                        expresisonGroupType,
                        TSList.of(
                            literalGroupType,
                            accessGroupType,
                            preincrementOperatorType,
                            addOperatorType,
                            lessThanEqualOperatorType,
                            moduloOperatorType,
                            addEqualOperatorType,
                            tripleEqualOperatorType,
                            callType))
                    .put(literalGroupType, TSList.of(symbolLiteralType, stringLiteralType)));
    MultiError errors = new MultiError();
    TSMap<String, ROSet<AtomType>> splayedTypes = Syntax.splayGroups(errors, types, groups);
    errors.raise();
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
                .front(statementsFront)
                .build());
    syntaxConfig.backType = BackType.JSON;
    return new Syntax(syntaxConfig);
  }

  public static TypeBuilder estreeTypeBuilder(String callType, String call) {
    return new TypeBuilder(callType, call)
        .alignment(
            baseAlign,
            new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(indentAlign, 0, false)))
        .alignment(
            indentAlign,
            new RelativeAlignmentSpec(
                new RelativeAlignmentSpec.Config(indentAlign, indentPx, true)));
  }

  private static FreeAtomType binaryOperator(
      SyntaxFrontFactory frontFactory,
      String id,
      String symbol,
      int precedence,
      String esType,
      String leftChildType,
      String rightChildType) {
    return frontFactory
        .binaryOp(
            estreeTypeBuilder(id, symbol + " operator")
                .precedence(precedence)
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec(esType))
                        .field(
                            "left",
                            new BackAtomSpec(new BaseBackAtomSpec.Config("left", leftChildType)))
                        .field("operator", new BackFixedPrimitiveSpec(symbol))
                        .field(
                            "right",
                            new BackAtomSpec(new BaseBackAtomSpec.Config("right", rightChildType)))
                        .build()),
            symbol,
            "left",
            "right")
        .build();
  }

  private static FreeAtomType prefixOperator(
      SyntaxFrontFactory frontFactory,
      String id,
      String symbol,
      int precedence,
      String esType,
      String childType) {
    return frontFactory
        .prefixOp(
            estreeTypeBuilder(id, symbol + " operator")
                .precedence(precedence)
                .associateForward()
                .back(
                    estreeBackBuilder()
                        .field("type", new BackFixedPrimitiveSpec(esType))
                        .field("operator", new BackFixedPrimitiveSpec(symbol))
                        .field("prefix", new BackFixedJSONSpecialPrimitiveSpec("true"))
                        .field(
                            "argument",
                            new BackAtomSpec(new BaseBackAtomSpec.Config("argument", childType)))
                        .build()),
            symbol,
            "argument")
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

  private static FrontSymbol text(String text, Function<Style.Config, Style.Config> styler) {
    return new FrontSymbol(
        new FrontSymbol.Config(
            new SymbolTextSpec(
                new SymbolTextSpec.Config(text).style(styler.apply(new Style.Config()).create()))));
  }

  private static FrontSymbol textCompactBase(
      String text, Function<Style.Config, Style.Config> styler) {
    return new FrontSymbol(
        new FrontSymbol.Config(
            new SymbolTextSpec(
                new SymbolTextSpec.Config(text)
                    .splitMode(Style.SplitMode.COMPACT)
                    .style(styler.apply(new Style.Config()).splitAlignment(baseAlign).create()))));
  }

  private static FrontSymbol textCompactIndent(
      String text, Function<Style.Config, Style.Config> styler) {
    return new FrontSymbol(
        new FrontSymbol.Config(
            new SymbolTextSpec(
                new SymbolTextSpec.Config(text)
                    .splitMode(Style.SplitMode.COMPACT)
                    .style(
                        styler.apply(new Style.Config()).splitAlignment(indentAlign).create()))));
  }

  private static BackFixedRecordSpecBuilder estreeBackBuilder() {
    return new BackFixedRecordSpecBuilder()
        .discardField("start")
        .discardField("end")
        .discardField("raw");
  }

  public interface SyntaxFrontFactory {
    TypeBuilder block(TypeBuilder type, String statementsKey);

    TypeBuilder forStatement(
        TypeBuilder type, String initKey, String testKey, String updateKey, String bodyKey);

    TypeBuilder ifStatement(TypeBuilder type, String testKey, String consequentKey);

    TypeBuilder callExpr(TypeBuilder type, String calleeKey, String argumentsKey);

    TypeBuilder memberExpr(TypeBuilder type, String objectKey, String propertyKey);

    TypeBuilder binaryOp(TypeBuilder type, String symbol, String leftKey, String rightKey);

    TypeBuilder prefixOp(TypeBuilder type, String symbol, String argumentKey);

    TypeBuilder declareInner(TypeBuilder type, String idKey, String initKey);

    TypeBuilder declareOuter(TypeBuilder type, String declarationsKey);
  }

  public abstract static class NonLispSyntaxFrontFactory implements SyntaxFrontFactory {
    @Override
    public TypeBuilder callExpr(TypeBuilder type, String calleeKey, String argumentsKey) {
      return type.front(new FrontAtomSpec(new FrontAtomSpec.Config(calleeKey)))
          .front(text("(", Main::styleCall))
          .front(
              new FrontArraySpecBuilder(argumentsKey)
                  .prefix(prefixCompactIndent)
                  .separator(text(", ", Main::styleCall))
                  .build())
          .front(textCompactBase(")", Main::styleCall));
    }

    @Override
    public TypeBuilder memberExpr(TypeBuilder type, String objectKey, String propertyKey) {
      return type.front(new FrontAtomSpec(new FrontAtomSpec.Config(objectKey)))
          .front(textCompactIndent(".", Main::styleOperator))
          .front(
              new FrontPrimitiveSpec(
                  new FrontPrimitiveSpec.Config(propertyKey).style(c -> styleIdentifier(c))));
    }

    @Override
    public TypeBuilder binaryOp(TypeBuilder type, String symbol, String leftKey, String rightKey) {
      return type.front(new FrontAtomSpec(new FrontAtomSpec.Config(leftKey)))
          .front(space)
          .front(
              new FrontSymbol(
                  new FrontSymbol.Config(
                      new SymbolTextSpec(
                          new SymbolTextSpec.Config(symbol + " ")
                              .splitMode(Style.SplitMode.COMPACT)
                              .style(
                                  styleOperator(new Style.Config())
                                      .splitAlignment(indentAlign)
                                      .create())))))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(rightKey)));
    }

    @Override
    public TypeBuilder prefixOp(TypeBuilder type, String symbol, String argumentKey) {
      return type.front(text(symbol, Main::styleOperator))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config("argument")));
    }

    @Override
    public TypeBuilder declareInner(TypeBuilder type, String idKey, String initKey) {
      return type.front(
              new FrontPrimitiveSpec(
                  new FrontPrimitiveSpec.Config("id").style(c -> c.color(identifierColor))))
          .front(text(" = ", Main::styleKeyword))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config("init")));
    }

    @Override
    public TypeBuilder declareOuter(TypeBuilder type, String declarationsKey) {
      return type.front(text("let ", Main::styleDeclare))
          .front(
              new FrontArraySpecBuilder("declarations")
                  .prefix(prefixCompactIndent)
                  .separator(text(", ", Main::styleDeclare))
                  .build());
    }
  }

  public static class LispSyntaxFrontFactory implements SyntaxFrontFactory {
    @Override
    public TypeBuilder binaryOp(TypeBuilder type, String symbol, String leftKey, String rightKey) {
      String lispSymbol;
      switch (symbol) {
        case "+=":
          {
            lispSymbol = "incf";
            break;
          }
        case "%":
          {
            lispSymbol = "mod";
            break;
          }
        default:
          {
            lispSymbol = symbol;
            break;
          }
      }
      return type.front(text("(" + lispSymbol, Main::styleOperator))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(leftKey)))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(rightKey)))
          .front(text(")", Main::styleOperator));
    }

    @Override
    public TypeBuilder prefixOp(TypeBuilder type, String symbol, String argumentKey) {
      String lispSymbol;
      switch (symbol) {
        case "++":
          {
            lispSymbol = "incf";
            break;
          }
        default:
          {
            lispSymbol = symbol;
            break;
          }
      }
      return type.front(text("(" + lispSymbol, Main::styleOperator))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config("argument")))
          .front(text(")", Main::styleOperator));
    }

    @Override
    public TypeBuilder callExpr(TypeBuilder type, String calleeKey, String argumentsKey) {
      return type.front(text("(", Main::styleCall))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(calleeKey)))
          .front(space)
          .front(
              new FrontArraySpecBuilder(argumentsKey)
                  .prefix(prefixCompactIndent)
                  .separator(text(" ", Function.identity()))
                  .build())
          .front(text(")", Main::styleCall));
    }

    @Override
    public TypeBuilder memberExpr(TypeBuilder type, String objectKey, String propertyKey) {
      return type.front(new FrontAtomSpec(new FrontAtomSpec.Config(objectKey)))
          .front(textCompactIndent(".", Main::styleOperator))
          .front(
              new FrontPrimitiveSpec(
                  new FrontPrimitiveSpec.Config(propertyKey).style(c -> styleIdentifier(c))));
    }

    @Override
    public TypeBuilder block(TypeBuilder type, String statementsKey) {
      return type.front(text("(block ", Main::styleKeyword))
          .front(
              new FrontArraySpecBuilder("statements")
                  .prefix(prefixCompactIndent)
                  .separator(space)
                  .build())
          .front(text(")", Main::styleKeyword));
    }

    @Override
    public TypeBuilder forStatement(
        TypeBuilder type, String initKey, String testKey, String updateKey, String bodyKey) {
      return type.front(text("(for ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(initKey)))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(updateKey)))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(bodyKey)))
          .front(text(")", Main::styleKeyword));
    }

    @Override
    public TypeBuilder ifStatement(TypeBuilder type, String testKey, String consequentKey) {
      return type.front(text("(if ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(space)
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(consequentKey)))
          .front(text(")", Main::styleKeyword));
    }

    @Override
    public TypeBuilder declareInner(TypeBuilder type, String idKey, String initKey) {
      return type.front(prefixCompactIndent)
          .front(
              new FrontPrimitiveSpec(
                  new FrontPrimitiveSpec.Config("id").style(c -> c.color(identifierColor))))
          .front(text(" ", Function.identity()))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config("init")));
    }

    @Override
    public TypeBuilder declareOuter(TypeBuilder type, String declarationsKey) {
      return type.front(text("(let ", Main::styleDeclare))
          .front(
              new FrontArraySpecBuilder("declarations")
                  .prefix(prefixCompactIndent)
                  .separator(space)
                  .build())
          .front(text(")", Main::styleDeclare));
    }
  }

  public static class JavascriptSyntaxFrontFactory extends NonLispSyntaxFrontFactory {
    @Override
    public TypeBuilder block(TypeBuilder type, String statementsKey) {
      return type.front(text("{ ", Main::styleBlock))
          .front(
              new FrontArraySpecBuilder("statements")
                  .prefix(prefixCompactIndent)
                  .suffix(text("; ", Main::styleBlock))
                  .build())
          .front(textCompactBase("}", Main::styleBlock));
    }

    @Override
    public TypeBuilder forStatement(
        TypeBuilder type, String initKey, String testKey, String updateKey, String bodyKey) {
      return type.front(text("for (", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(initKey)))
          .front(text("; ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(text("; ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(updateKey)))
          .front(textCompactBase(") ", Main::styleKeyword))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(bodyKey)));
    }

    @Override
    public TypeBuilder ifStatement(TypeBuilder type, String testKey, String consequentKey) {
      return type.front(text("if (", Main::styleKeyword))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(text(") ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(consequentKey)));
    }
  }

  public static class PythonSyntaxFrontFactory extends NonLispSyntaxFrontFactory {
    @Override
    public TypeBuilder block(TypeBuilder type, String statementsKey) {
      return type.front(text(":", Main::styleBlock))
          .front(new FrontArraySpecBuilder("statements").prefix(prefixIndent).build());
    }

    @Override
    public TypeBuilder forStatement(
        TypeBuilder type, String initKey, String testKey, String updateKey, String bodyKey) {
      return type.front(text("for ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(initKey)))
          .front(text("; ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(text("; ", Main::styleKeyword))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(updateKey)))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(bodyKey)));
    }

    @Override
    public TypeBuilder ifStatement(TypeBuilder type, String testKey, String consequentKey) {
      return type.front(text("if ", Main::styleKeyword))
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(testKey)))
          .front(prefixCompactIndent)
          .front(new FrontAtomSpec(new FrontAtomSpec.Config(consequentKey)));
    }
  }
}
