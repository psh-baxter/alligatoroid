package com.zarbosoft.merman.jfxeditor1;

import com.zarbosoft.merman.core.Environment;
import com.zarbosoft.merman.core.MultiError;
import com.zarbosoft.merman.core.example.SyntaxOut;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.core.syntax.BaseGapAtomType;
import com.zarbosoft.merman.core.syntax.FreeAtomType;
import com.zarbosoft.merman.core.syntax.GapAtomType;
import com.zarbosoft.merman.core.syntax.RootAtomType;
import com.zarbosoft.merman.core.syntax.SuffixGapAtomType;
import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.back.BackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedRecordSpec;
import com.zarbosoft.merman.core.syntax.back.BackFixedTypeSpec;
import com.zarbosoft.merman.core.syntax.back.BackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackArraySpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackAtomSpec;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.ConditionValue;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpec;
import com.zarbosoft.merman.core.syntax.front.FrontArraySpecBase;
import com.zarbosoft.merman.core.syntax.front.FrontAtomSpec;
import com.zarbosoft.merman.core.syntax.front.FrontPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSpec;
import com.zarbosoft.merman.core.syntax.front.FrontSymbolSpec;
import com.zarbosoft.merman.core.syntax.primitivepattern.Maybe;
import com.zarbosoft.merman.core.syntax.primitivepattern.Pattern;
import com.zarbosoft.merman.core.syntax.primitivepattern.PatternCharacterClass;
import com.zarbosoft.merman.core.syntax.primitivepattern.PatternSequence;
import com.zarbosoft.merman.core.syntax.primitivepattern.Repeat0;
import com.zarbosoft.merman.core.syntax.primitivepattern.Repeat1;
import com.zarbosoft.merman.core.syntax.primitivepattern.SymbolCharacter;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.merman.core.syntax.style.ObboxStyle;
import com.zarbosoft.merman.core.syntax.style.Padding;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.syntax.symbol.SymbolSpaceSpec;
import com.zarbosoft.merman.core.syntax.symbol.SymbolTextSpec;
import com.zarbosoft.rendaw.common.ROList;
import com.zarbosoft.rendaw.common.ROMap;
import com.zarbosoft.rendaw.common.ROOrderedSetRef;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.ROSet;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSMap;
import com.zarbosoft.rendaw.common.TSOrderedMap;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.function.Consumer;
import java.util.function.Function;

public class AlligatoroidSyntax {
  public static final FrontSymbolSpec compactZeroSplit =
      new FrontSymbolSpec(
          new FrontSymbolSpec.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.COMPACT)
                      .style(new Style(new Style.Config())))));
  public static final FrontSymbolSpec zeroSplit =
      new FrontSymbolSpec(
          new FrontSymbolSpec.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.ALWAYS)
                      .style(new Style(new Style.Config())))));
  public static final Pattern PATTERN_IDENTIFIER = new Repeat1(new SymbolCharacter());
  public static final Pattern PATTERN_INT;
  public static final Pattern PATTERN_HEXINT;
  public static final Pattern PATTERN_FLOAT;
  public static final Pattern PATTERN_HEXFLOAT;
  private static final ModelColor COLOR_LITERAL_SYMBOL; // number suffix
  private static final ModelColor COLOR_LITERAL_PRIMITIVE; // numbers
  private static final ModelColor COLOR_LITERAL_KEYWORD; // true/false
  private static final ModelColor COLOR_IDENTIFIER;
  private static final ModelColor COLOR_OPERATOR;
  private static final ModelColor COLOR_ARRAY_SEP;
  private static final ModelColor COLOR_BRACKET;
  private static final ModelColor COLOR_LABEL;
  private static final ModelColor COLOR_KEYWORD;
  private static final ModelColor COLOR_BACKGROUND;
  private static final ModelColor COLOR_CHOICE_BACKGROUND;
  private static final ModelColor COLOR_INCOMPLETE;
  private static final ModelColor COLOR_HOVER;
  private static final ModelColor COLOR_CURSOR;
  private static final ModelColor COLOR_CHOICE_TEXT;
  private static final ModelColor COLOR_CHOICE_CURSOR;
  private static final ModelColor COLOR_COMMENT;
  private static final double fontSize = 6;
  private static final String GROUP_EXPR = "expr";
  private static final String GROUP_STATEMENT = "statement";
  private static final String GROUP_BRANCH_ELEMENT = "branch_element";
  private static final String GROUP_COMMENT_BODY = "comment";
  private static final String BACK_TYPE_SCOPE = "local";
  private static final String BACK_TYPE_BUILTIN = "builtin";
  private static final String BACK_TYPE_ACCESS = "access";
  private static final String TYPE_MODULE_LOCAL = "mod_local";
  private static final String TYPE_MODULE_REMOTE_BUNDLE = "mod_git";
  private static final String TYPE_BIND = "bind";
  private static final String TYPE_ASSIGN = "assign";
  private static final String TYPE_LOWER = "lower";
  private static final String TYPE_LOWER_LABEL = "lower_label";
  private static final String TYPE_STAGE = "stage";
  private static final String TYPE_BLOCK = "block";
  private static final String TYPE_BUILTIN = "builtin";
  private static final String TYPE_ADD = "add";
  private static final String TYPE_SUBTRACT = "subtract";
  private static final String TYPE_MULTIPLY = "multiply";
  private static final String TYPE_DIVIDE = "divide";
  private static final String TYPE_EQUAL = "equal";
  private static final String TYPE_NOT_EQUAL = "not_equal";
  private static final String TYPE_LESS = "less";
  private static final String TYPE_LESS_EQUAL = "less_equal";
  private static final String TYPE_GREATER = "greater";
  private static final String TYPE_GREATER_EQUAL = "greater_equal";
  private static final String TYPE_LOGICAL_AND = "logical_and";
  private static final String TYPE_LOGICAL_OR = "logical_or";
  private static final String TYPE_LOGICAL_NOT = "logical_not";
  private static final String TYPE_BINARY_AND = "binary_and";
  private static final String TYPE_BINARY_OR = "binary_or";
  private static final String TYPE_BINARY_XOR = "binary_xor";
  private static final String TYPE_BINARY_INVERT = "binary_invert";
  private static final String TYPE_BINARY_LSHIFT = "binary_lshift";
  private static final String TYPE_BINARY_RSHIFT = "binary_rshift";
  private static final String TYPE_LOOP = "loop";
  private static final String TYPE_BRANCH = "branch";
  private static final String TYPE_BRANCH_COND = "branch_cond";
  private static final String TYPE_BRANCH_DEFAULT = "branch_default";
  private static final String BACK_TYPE_LITERAL_RECORD = "record";
  private static final String TYPE_LITERAL_RECORD = "record";
  private static final String TYPE_LITERAL_TUPLE = "tuple";
  private static final String TYPE_EXIT = "exit";
  private static final String TYPE_RETURN = "return";
  private static final String ALIGN_INDENT = "indent";
  public static final FrontSymbolSpec compactSplit =
      new FrontSymbolSpec(
          new FrontSymbolSpec.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.COMPACT)
                      .style(new Style(new Style.Config().splitAlignment(ALIGN_INDENT))))));
  private static final String ALIGN_BASE = "base";
  public static final FrontSymbolSpec baseCompactSplit =
      new FrontSymbolSpec(
          new FrontSymbolSpec.Config(
              new SymbolSpaceSpec(
                  new SymbolSpaceSpec.Config()
                      .splitMode(Style.SplitMode.COMPACT)
                      .style(new Style(new Style.Config().splitAlignment(ALIGN_BASE))))));
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_ACCESS_DYNAMIC = "access_dynamic";
  private static final String TYPE_LOCAL = "local";
  private static final String TYPE_LITERAL_SCOPE = "literal_local";
  private static final String TYPE_LABEL = "label";
  private static final String BACK_TYPE_CALL = "call";
  private static final String TYPE_CALL = "call";
  private static final String TYPE_LITERAL_RECORD_ELEMENT = "record_element";
  private static final String BACK_TYPE_LITERAL_STRING = "string";
  private static final String TYPE_LITERAL_STRING = "string";
  private static final String TYPE_LITERAL_INT = "int";
  private static final String TYPE_LITERAL_HEX_INT = "hex_int";
  private static final String TYPE_LITERAL_FLOAT = "float";
  private static final String TYPE_LITERAL_HEX_FLOAT = "hex_float";
  private static final String TYPE_LITERAL_TRUE = "true";
  private static final String TYPE_LITERAL_FALSE = "false";
  private static final String TYPE_LITERAL_UNIQUE = "unique";
  private static final String TYPE_LITERAL_VOID = "void";
  private static final String TYPE_STATEMENT_COMMENT = "comment_statement";
  private static final String TYPE_EXPR_COMMENT = "comment_expr";
  private static final String TYPE_COMMENT_H1 = "comment_h1";
  private static final String TYPE_COMMENT_P = "comment_p";
  private static final String FIELD_LITERAL_VALUE = "value";

  static {
    Pattern maybeNegative =
        new Maybe(new PatternCharacterClass(new TSList<>(new ROPair<>("-", "-"))));
    Pattern dot = new PatternCharacterClass(new TSList<>(new ROPair<>(".", ".")));
    Pattern integerDigit = new PatternCharacterClass(new TSList<>(new ROPair<>("0", "9")));
    Pattern hexDigit =
        new PatternCharacterClass(new TSList<>(new ROPair<>("0", "9"), new ROPair<>("a", "f")));
    PATTERN_INT = new PatternSequence(new TSList<>(maybeNegative, new Repeat1(integerDigit)));
    PATTERN_HEXINT = new PatternSequence(new TSList<>(maybeNegative, new Repeat1(hexDigit)));
    PATTERN_FLOAT =
        new PatternSequence(
            new TSList<>(
                maybeNegative,
                new Repeat1(integerDigit),
                new Maybe(new PatternSequence(new TSList<>(dot, new Repeat0(integerDigit))))));
    PATTERN_HEXFLOAT =
        new PatternSequence(
            new TSList<>(
                maybeNegative,
                new Repeat1(hexDigit),
                new Maybe(new PatternSequence(new TSList<>(dot, new Repeat0(hexDigit))))));
  }

  static {
    ModelColor identifier = ModelColor.RGB.hex("#dbec80");
    ModelColor.RGB literal = ModelColor.RGB.hex("#85c1da");
    ModelColor literalFade = new ModelColor.RGBA(literal.r, literal.g, literal.b, 0.7);
    ModelColor comment = ModelColor.RGB.hex("#827c67");
    ModelColor choice = ModelColor.RGB.hex("#909090");
    ModelColor keyword = ModelColor.RGB.hex("#be74d0");
    ModelColor label = ModelColor.RGB.hex("#3a7fff");
    ModelColor other = ModelColor.RGB.hex("#b2c5d4");
    ModelColor error = ModelColor.RGB.hex("#ea4c3b");

    ModelColor bg = ModelColor.RGB.hex("#2b323a");
    // ModelColor popupBg = ModelColor.RGB.hex("#222529");
    ModelColor popupBg = new ModelColor.RGBA(14. / 255, 8. / 255, 0, 0.3);
    ModelColor hover = ModelColor.RGB.hex("#737373");
    ModelColor cursor = ModelColor.RGB.hex("#b9b9b9");

    COLOR_LITERAL_PRIMITIVE = literal; // numbers
    COLOR_LITERAL_SYMBOL = literalFade; // number suffix
    COLOR_LITERAL_KEYWORD = literal; // true/false
    COLOR_IDENTIFIER = identifier;
    COLOR_OPERATOR = other;
    COLOR_ARRAY_SEP = other;
    COLOR_BRACKET = other;
    COLOR_LABEL = label;
    COLOR_KEYWORD = keyword;
    COLOR_BACKGROUND = bg;
    COLOR_CHOICE_BACKGROUND = popupBg;
    COLOR_INCOMPLETE = error;
    COLOR_HOVER = hover;
    COLOR_CURSOR = cursor;
    COLOR_CHOICE_TEXT = choice;
    COLOR_CHOICE_CURSOR = choice;
    COLOR_COMMENT = comment;
  }

  private static BackSpec literalStringBack(BackSpec value) {
    return new ABackBuilder(BACK_TYPE_LITERAL_STRING).raw("value", value).build().get(0);
  }

  private static BackSpec literalStringBackDynamic(String field) {
    return literalStringBack(
        new BackPrimitiveSpec(
            new BaseBackPrimitiveSpec.Config(field).pattern(PATTERN_IDENTIFIER, "identifier")));
  }

  private static Style cursorStyle(boolean primitive) {
    return new Style(
        new Style.Config()
            .obbox(
                new ObboxStyle(
                    new ObboxStyle.Config()
                        .padding(primitive ? new Padding(0, 0, 1, 1) : Padding.same(1))
                        .roundStart(true)
                        .roundEnd(true)
                        .lineThickness(0.3)
                        .roundRadius(3)
                        .lineColor(COLOR_CURSOR))));
  }

  private static Style hoverStyle(boolean primitive) {
    return new Style(
        new Style.Config()
            .obbox(
                new ObboxStyle(
                    new ObboxStyle.Config()
                        .padding(primitive ? new Padding(0, 0, 1, 1) : Padding.same(1))
                        .roundEnd(true)
                        .roundStart(true)
                        .lineThickness(0.3)
                        .roundRadius(3)
                        .lineColor(COLOR_HOVER))));
  }

  private static Style.Config baseCodeStyle() {
    return sizedBaseCodeStyle(fontSize);
  }

  private static Style.Config sizedBaseCodeStyle(double size) {
    return new Style.Config()
        .font("monospace")
        .fontSize(size)
        .ascent(size * 0.8)
        .descent(size * 0.2);
  }

  public static AtomType binaryInfix(
      String id, String description, int precedence, String symbol, String args) {
    return new ATypeBuilder(id, description)
        .precedence(precedence)
        .atom("first", args)
        .s()
        .t(symbol, COLOR_OPERATOR)
        .s()
        .atom("second", args)
        .build();
  }

  public static AtomType binaryInfixNoSpace(
      String id, String description, int precedence, String symbol, String args) {
    return new ATypeBuilder(id, description)
        .precedence(precedence)
        .atom("first", args)
        .t(symbol, COLOR_OPERATOR)
        .atom("second", args)
        .build();
  }

  public static AtomType unaryPrefix(String id, String description, String prefix, String arg) {
    return new ATypeBuilder(id, description)
        .precedence(Integer.MAX_VALUE)
        .t(prefix, COLOR_OPERATOR)
        .atom("first", arg)
        .build();
  }

  public static SyntaxOut create(Environment env, Padding pad) {
    TypeGrouper types = new TypeGrouper();

    types.add(GROUP_EXPR, GROUP_STATEMENT);

    // Variables, fields
    types.add(
        new ATypeBuilder(TYPE_ACCESS, "Access")
            .type(BACK_TYPE_ACCESS)
            .atom("parent", GROUP_EXPR)
            .t(".", COLOR_OPERATOR)
            .nestedIdentifier("child", COLOR_IDENTIFIER)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_ACCESS_DYNAMIC, "Dynamic Access")
            .type(BACK_TYPE_ACCESS)
            .atom("parent", GROUP_EXPR)
            .sb("[", COLOR_BRACKET)
            .compactSplit()
            .atom("child", GROUP_EXPR)
            .eb("]", COLOR_BRACKET)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LOCAL, "Variable")
            .type(BACK_TYPE_SCOPE)
            .nestedIdentifier("value", COLOR_IDENTIFIER)
            .build(),
        GROUP_EXPR);
    types.add(binaryInfix(TYPE_BIND, "Bind", 0, ":=", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_ASSIGN, "Set", 0, "=", GROUP_EXPR), GROUP_EXPR);

    // Calls
    types.add(
        binaryInfixNoSpace(TYPE_CALL, "Call", Integer.MAX_VALUE, "@", GROUP_EXPR), GROUP_EXPR);

    // Control flow
    types.add(
        new ATypeBuilder(TYPE_LABEL, "Label")
            .t("#", COLOR_LABEL)
            .primitive("label", COLOR_LABEL)
            .s()
            .atom("child", GROUP_EXPR)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_BLOCK, "Block")
            .sb("{", COLOR_BRACKET)
            .s()
            .array("statements", GROUP_STATEMENT, "; ")
            .s()
            .eb("}", COLOR_BRACKET)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LOOP, "Loop")
            .t("loop", COLOR_KEYWORD)
            .s()
            .atom("body", GROUP_EXPR)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_BRANCH, "Branch")
            .t("branch", COLOR_KEYWORD)
            .s()
            .array("clauses", GROUP_BRANCH_ELEMENT, ", ")
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_BRANCH_COND, "Conditional")
            .atom("condition", GROUP_EXPR)
            .s()
            .t("->", COLOR_OPERATOR)
            .s()
            .atom("body", GROUP_EXPR)
            .build(),
        GROUP_BRANCH_ELEMENT);
    types.add(
        new ATypeBuilder(TYPE_BRANCH_DEFAULT, "Default")
            .defaultSelection("body")
            .t("default", COLOR_KEYWORD)
            .s()
            .t("->", COLOR_OPERATOR)
            .s()
            .atom("body", GROUP_EXPR)
            .build(),
        GROUP_BRANCH_ELEMENT);
    types.add(
        new ATypeBuilder(TYPE_EXIT, "Exit")
            .t("exit", COLOR_KEYWORD)
            .s()
            .compactSplit()
            .primitive("label", COLOR_LABEL)
            .build(),
        GROUP_STATEMENT);
    types.add(
        new ATypeBuilder(TYPE_RETURN, "Return")
            .defaultSelection("value")
            .t("return", COLOR_KEYWORD)
            .s()
            .primitive("label", COLOR_LABEL)
            .s()
            .compactSplit()
            .atom("value", GROUP_EXPR)
            .build(),
        GROUP_STATEMENT);

    // Imports
    types.add(
        new ATypeBuilder(TYPE_MODULE_LOCAL, "Local Module")
            .t("mod", COLOR_KEYWORD)
            .s()
            .compactSplit()
            .atom("path", GROUP_EXPR)
            .build(b -> backBuiltinFunc("moduleLocal", b.get(0))),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_MODULE_REMOTE_BUNDLE, "Bundle Module")
            .t("mod", COLOR_KEYWORD)
            .s()
            .compactSplit()
            .atom("uri", GROUP_EXPR)
            .s()
            .compactSplit()
            .atom("hash", GROUP_EXPR)
            .build(b -> backBuiltinFunc("moduleBundle", b.get(0))),
        GROUP_EXPR);

    // Staging
    types.add(unaryPrefix(TYPE_STAGE, "Stage", "`", GROUP_EXPR), GROUP_EXPR);
    types.add(unaryPrefix(TYPE_LOWER, "Lower", "$", GROUP_EXPR), GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LOWER_LABEL, "Lower (from label)")
            .defaultSelection("child")
            .t("~", COLOR_OPERATOR)
            .primitive("label", COLOR_LABEL)
            .s()
            .atom("child", GROUP_EXPR)
            .build(),
        GROUP_EXPR);

    // Primitive literals
    types.add(
        new ATypeBuilder(TYPE_LITERAL_STRING, "String")
            .type(BACK_TYPE_LITERAL_STRING)
            .t("\"", COLOR_LITERAL_SYMBOL)
            .primitive(FIELD_LITERAL_VALUE, COLOR_LITERAL_PRIMITIVE)
            .t("\"", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_HEX_INT, "Hex int")
            .pattern(
                FIELD_LITERAL_VALUE,
                PATTERN_HEXINT,
                "hex int",
                COLOR_LITERAL_PRIMITIVE,
                COLOR_INCOMPLETE)
            .t("x", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_HEX_FLOAT, "Hex float")
            .pattern(
                FIELD_LITERAL_VALUE,
                PATTERN_HEXFLOAT,
                "hex float",
                COLOR_LITERAL_PRIMITIVE,
                COLOR_INCOMPLETE)
            .t("xf", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_INT, "Int")
            .pattern(
                FIELD_LITERAL_VALUE, PATTERN_INT, "int", COLOR_LITERAL_PRIMITIVE, COLOR_INCOMPLETE)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_FLOAT, "Float")
            .pattern(
                FIELD_LITERAL_VALUE,
                PATTERN_FLOAT,
                "float",
                COLOR_LITERAL_PRIMITIVE,
                COLOR_INCOMPLETE)
            .t("f", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_TRUE, "True").t("true", COLOR_LITERAL_KEYWORD).build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_FALSE, "False").t("false", COLOR_LITERAL_KEYWORD).build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_UNIQUE, "Unique").t("unique", COLOR_LITERAL_KEYWORD).build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_VOID, "Void").t("void", COLOR_LITERAL_KEYWORD).build(),
        GROUP_EXPR);

    // Aggregate literals
    types.add(
        new ATypeBuilder(TYPE_LITERAL_RECORD, "Record")
            .type(BACK_TYPE_LITERAL_RECORD)
            .t("rec", COLOR_LITERAL_KEYWORD)
            .s()
            .sb("(", COLOR_LITERAL_SYMBOL)
            .array("elements", TYPE_LITERAL_RECORD_ELEMENT, ",")
            .eb(")", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_RECORD_ELEMENT, "Element")
            .defaultSelection("value")
            .atom("key", GROUP_EXPR)
            .t(":", COLOR_OPERATOR)
            .s()
            .atom("value", GROUP_EXPR)
            .build());
    types.add(
        new ATypeBuilder(TYPE_LITERAL_TUPLE, "Tuple")
            .sb("(", COLOR_LITERAL_SYMBOL)
            .array("elements", GROUP_EXPR, ",")
            .eb(")", COLOR_LITERAL_SYMBOL)
            .build(),
        GROUP_EXPR); // TODO key/value tuple
    types.add(new ATypeBuilder(TYPE_BUILTIN, "Builtin").t("%", COLOR_KEYWORD).build(), GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_LITERAL_SCOPE, "Scope").t("scope", COLOR_KEYWORD).build(),
        GROUP_EXPR);

    // Numeric operators
    types.add(binaryInfix(TYPE_ADD, "Add", 500, "+", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_SUBTRACT, "Subtract", 500, "-", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_MULTIPLY, "Multiply", 600, "*", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_DIVIDE, "Divide", 600, "/", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_EQUAL, "Equal", 100, "==", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_NOT_EQUAL, "Not equal", 100, "!=", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_LESS, "Less than", 100, "<", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_LESS_EQUAL, "Less than/equal", 100, "<=", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_GREATER, "Greater than", 100, ">", GROUP_EXPR), GROUP_EXPR);
    types.add(
        binaryInfix(TYPE_GREATER_EQUAL, "Greater than/equal", 100, ">=", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_LOGICAL_AND, "And", 300, "&&", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_LOGICAL_OR, "Or", 200, "||", GROUP_EXPR), GROUP_EXPR);
    types.add(unaryPrefix(TYPE_LOGICAL_NOT, "Not", "!", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_BINARY_AND, "Binary and", 700, "&", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_BINARY_OR, "Binary or", 700, "|", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_BINARY_XOR, "Binary xor", 800, "^", GROUP_EXPR), GROUP_EXPR);
    types.add(unaryPrefix(TYPE_BINARY_INVERT, "Binary invert", "~", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_BINARY_LSHIFT, "Left shift", 750, "<<", GROUP_EXPR), GROUP_EXPR);
    types.add(binaryInfix(TYPE_BINARY_RSHIFT, "Right shift", 750, ">>", GROUP_EXPR), GROUP_EXPR);

    // Comments
    types.add(
        new ATypeBuilder(TYPE_STATEMENT_COMMENT, "Comment")
            .gapKey(",")
            .placeholder("￮", "children", COLOR_COMMENT)
            .custom(AlligatoroidSyntax::commentArray)
            .build(),
        GROUP_STATEMENT);
    types.add(
        new ATypeBuilder(TYPE_EXPR_COMMENT, "Comment")
            .atom("nested", GROUP_EXPR)
            .gapKey(",")
            .placeholder("￮", "children", COLOR_COMMENT)
            .custom(AlligatoroidSyntax::commentArray)
            .build(),
        GROUP_EXPR);
    types.add(
        new ATypeBuilder(TYPE_COMMENT_P, "Paragraph")
            .gapKey("p")
            .styledPrimitive("text", COLOR_COMMENT, null, fontSize)
            .vspacer(fontSize, fontSize * 0.8)
            .build(),
        GROUP_COMMENT_BODY);
    types.add(
        new ATypeBuilder(TYPE_COMMENT_H1, "Header (1)")
            .gapKey("h1")
            .vspacer(fontSize * 2 * 1.5, 0)
            .styledPrimitive("text", COLOR_COMMENT, null, fontSize * 2)
            .vspacer(0, fontSize * 0.8)
            .build(),
        GROUP_COMMENT_BODY);

    // Gap
    final Style.Config gapStyleConfig =
        new Style.Config().fontSize(fontSize).color(COLOR_INCOMPLETE);
    final Style gapStyle = new Style(gapStyleConfig);
    final Style gapEmptySymbolStyle = new Style(gapStyleConfig.dupe().padding(Padding.ct(1, 0)));
    GapAtomType gap =
        new GapAtomType(
            new GapAtomType.Config()
                .primitiveStyle(gapStyle)
                .frontSuffix(
                    new TSList<FrontSpec>(
                        new FrontSymbolSpec(
                            new FrontSymbolSpec.Config(
                                    new SymbolTextSpec(
                                        new SymbolTextSpec.Config("￮").style(gapEmptySymbolStyle)))
                                .condition(
                                    new ConditionValue(
                                        new ConditionValue.Config(
                                            BaseGapAtomType.PRIMITIVE_KEY,
                                            ConditionValue.Is.EMPTY,
                                            false)))))));
    SuffixGapAtomType suffixGap =
        new SuffixGapAtomType(
            new SuffixGapAtomType.Config()
                .primitiveStyle(gapStyle)
                .frontPrefix(
                    new TSList<>(
                        new FrontSymbolSpec(
                            new FrontSymbolSpec.Config(
                                new SymbolSpaceSpec(
                                    new SymbolSpaceSpec.Config()
                                        .style(new Style(new Style.Config().space(1))))))))
                .frontSuffix(
                    new TSList<FrontSpec>(
                        new FrontSymbolSpec(
                            new FrontSymbolSpec.Config(
                                    new SymbolTextSpec(
                                        new SymbolTextSpec.Config("▹").style(gapEmptySymbolStyle)))
                                .condition(
                                    new ConditionValue(
                                        new ConditionValue.Config(
                                            BaseGapAtomType.PRIMITIVE_KEY,
                                            ConditionValue.Is.EMPTY,
                                            false))))))
                .frontArrayConfig(new FrontArraySpecBase.Config().prefix(TSList.of(compactSplit))));

    TSMap<String, ROOrderedSetRef<AtomType>> splayedTypes;
    {
      MultiError errors = new MultiError();
      splayedTypes = Syntax.splayGroups(errors, types.types, gap, suffixGap, types.groups);
      errors.raise();
    }
    return new SyntaxOut(
        COLOR_CHOICE_TEXT,
        COLOR_CHOICE_CURSOR,
        COLOR_CHOICE_BACKGROUND,
        new Syntax(
            env,
            new Syntax.Config(
                    splayedTypes,
                    new RootAtomType(
                        new RootAtomType.Config(
                            TSList.of(
                                new BackFixedTypeSpec(
                                    new BackFixedTypeSpec.Config(
                                        "alligatorus:0.0.1",
                                        new BackArraySpec(
                                            new BaseBackArraySpec.Config(
                                                "root_elements", GROUP_STATEMENT, ROList.empty))))),
                            TSList.of(
                                new FrontArraySpec(
                                    new FrontArraySpec.Config(
                                        "root_elements",
                                        new FrontArraySpecBase.Config()
                                            .prefix(
                                                new TSList<>(
                                                    new FrontSymbolSpec(
                                                        new FrontSymbolSpec.Config(
                                                            new SymbolSpaceSpec(
                                                                new SymbolSpaceSpec.Config()
                                                                    .splitMode(
                                                                        Style.SplitMode.ALWAYS))))))
                                            .suffix(
                                                new TSList<>(
                                                    new FrontSymbolSpec(
                                                        new FrontSymbolSpec.Config(
                                                            new SymbolTextSpec(
                                                                new SymbolTextSpec.Config(";")
                                                                    .style(
                                                                        new Style(
                                                                            baseCodeStyle()
                                                                                .color(
                                                                                    COLOR_ARRAY_SEP))))))))))),
                            ROMap.empty)),
                    gap,
                    suffixGap)
                .backType(BackType.LUXEM)
                .displayUnit(Syntax.DisplayUnit.MM)
                .background(COLOR_BACKGROUND)
                .hoverStyle(hoverStyle(false))
                .primitiveHoverStyle(hoverStyle(true))
                .cursorStyle(cursorStyle(false))
                .primitiveCursorStyle(cursorStyle(true))
                .pad(pad)),
        TSSet.of(TYPE_LOCAL, TYPE_ACCESS));
  }

  private static void commentArray(ATypeBuilder b) {
    b.front.front.add(
        new FrontArraySpec(
            new FrontArraySpec.Config(
                "children", new FrontArraySpecBase.Config().prefix(new TSList<>(zeroSplit)))));
    b.front.vspacer(0, fontSize * 1.5);
    b.back.array("children", GROUP_COMMENT_BODY);
  }

  private static ROList<BackSpec> backBuiltinField(BackSpec child) {
    return new ABackBuilder(BACK_TYPE_ACCESS)
        .raw("parent", new ABackBuilder(BACK_TYPE_BUILTIN).build().get(0))
        .raw("child", child)
        .build();
  }

  public static ROList<BackSpec> backBuiltinFunc(String field, BackSpec args) {
    return new ABackBuilder(BACK_TYPE_CALL)
        .raw("first", backBuiltinField(literalStringBack(new BackFixedPrimitiveSpec(field))).get(0))
        .raw("second", args)
        .build();
  }

  public static class TypeGrouper {
    public TSList<AtomType> types = new TSList<>();
    public TSOrderedMap<String, ROList<String>> groups = new TSOrderedMap<>();

    public TypeGrouper add(AtomType t, String... groups) {
      types.add(t);
      for (String group : groups) {
        ((TSList<String>) this.groups.getCreate(group, () -> new TSList<>())).add(t.id);
      }
      return this;
    }

    public TypeGrouper add(String group, String... groups) {
      for (String dest : groups) {
        ((TSList<String>) this.groups.getCreate(dest, () -> new TSList<>())).add(group);
      }
      return this;
    }
  }

  public static class AFrontBuilder {
    private final TSList<FrontSpec> front = new TSList<>();

    public AFrontBuilder fixed(String text, ModelColor color) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                  new SymbolTextSpec(
                      new SymbolTextSpec.Config(text)
                          .style(new Style(baseCodeStyle().color(color)))))));
      return this;
    }

    public AFrontBuilder space() {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                  new SymbolTextSpec(
                      new SymbolTextSpec.Config(" ").nonGapKey().style(new Style(baseCodeStyle()))))));
      return this;
    }

    public AFrontBuilder startBracket(String text, ModelColor color) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                  new SymbolTextSpec(
                      new SymbolTextSpec.Config(text)
                          .style(new Style(baseCodeStyle().color(color)))))));
      front.add(compactSplit);
      return this;
    }

    public AFrontBuilder endBracket(String text, ModelColor color) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                  new SymbolTextSpec(
                      new SymbolTextSpec.Config(text)
                          .splitMode(Style.SplitMode.COMPACT)
                          .style(
                              new Style(
                                  baseCodeStyle().splitAlignment(ALIGN_BASE).color(color)))))));
      return this;
    }

    public AFrontBuilder atom(String id) {
      front.add(new FrontAtomSpec(new FrontAtomSpec.Config(id)));
      return this;
    }

    public AFrontBuilder compactSplit() {
      front.add(compactSplit);
      return this;
    }

    public AFrontBuilder compactBaseSplit() {
      front.add(baseCompactSplit);
      return this;
    }

    public void zeroSplit() {
      front.add(zeroSplit);
    }

    public void compactZeroSplit() {
      front.add(compactZeroSplit);
    }

    public AFrontBuilder array(String id, String separator) {
      front.add(
          new FrontArraySpec(
              new FrontArraySpec.Config(
                  id,
                  new FrontArraySpecBase.Config()
                      .prefix(new TSList<>(compactSplit))
                      .suffix(
                          new TSList<>(
                              new FrontSymbolSpec(
                                  new FrontSymbolSpec.Config(
                                      new SymbolTextSpec(
                                          new SymbolTextSpec.Config(separator)
                                              .style(
                                                  new Style(
                                                      baseCodeStyle()
                                                          .color(COLOR_ARRAY_SEP)))))))))));
      return this;
    }

    public AFrontBuilder primitive(String id, ModelColor color) {
      front.add(
          new FrontPrimitiveSpec(
              new FrontPrimitiveSpec.Config(id).style(new Style(baseCodeStyle().color(color)))));
      return this;
    }

    public AFrontBuilder styledPrimitive(String id, ModelColor color, String font, double size) {
      front.add(
          new FrontPrimitiveSpec(
              new FrontPrimitiveSpec.Config(id)
                  .style(new Style(sizedBaseCodeStyle(size).font(font).color(color)))));
      return this;
    }

    public void placeholder(String text, String field, ModelColor color) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                      new SymbolTextSpec(
                          new SymbolTextSpec.Config(text)
                              .style(new Style(baseCodeStyle().color(color)))))
                  .condition(
                      new ConditionValue(
                          new ConditionValue.Config(field, ConditionValue.Is.EMPTY, false)))));
    }

    public void gapKey(String text) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(new SymbolSpaceSpec(new SymbolSpaceSpec.Config()))
                  .gapKey(text)));
    }

    public void pattern(String field, ModelColor color, ModelColor invalidColor) {
      front.add(
          new FrontPrimitiveSpec(
              new FrontPrimitiveSpec.Config(field)
                  .style(new Style(baseCodeStyle().color(color).invalidColor(invalidColor)))));
    }

    public void vspacer(double ascent, double descent) {
      front.add(
          new FrontSymbolSpec(
              new FrontSymbolSpec.Config(
                  new SymbolSpaceSpec(
                      new SymbolSpaceSpec.Config()
                          .style(new Style(new Style.Config().ascent(ascent).descent(descent)))))));
    }
  }

  public static class ABackBuilder {
    private final TSOrderedMap<String, BackSpec> back = new TSOrderedMap<>();
    private String backType;

    public ABackBuilder(String backType) {
      this.backType = backType;
    }

    public ROList<BackSpec> build() {
      return new TSList<>(
          new BackFixedTypeSpec(
              new BackFixedTypeSpec.Config(
                  backType,
                  new BackFixedRecordSpec(new BackFixedRecordSpec.Config(back, ROSet.empty)))));
    }

    public ABackBuilder atom(String id, String elementType) {
      back.put(id, new BackAtomSpec(new BaseBackAtomSpec.Config(id, elementType)));
      return this;
    }

    public ABackBuilder array(String id, String elementType) {
      back.put(id, new BackArraySpec(new BaseBackArraySpec.Config(id, elementType, ROList.empty)));
      return this;
    }

    public ABackBuilder primitive(String id) {
      back.put(id, new BackPrimitiveSpec(new BaseBackPrimitiveSpec.Config(id)));
      return this;
    }

    public ABackBuilder nestedIdentifier(String child) {
      back.put(child, literalStringBackDynamic(child));
      return this;
    }

    public ABackBuilder type(String t) {
      this.backType = t;
      return this;
    }

    public ABackBuilder raw(String field, BackSpec back) {
      this.back.put(field, back);
      return this;
    }

    public ABackBuilder pattern(String field, Pattern pattern, String patternDescription) {
      this.back.put(
          field,
          new BackPrimitiveSpec(
              new BaseBackPrimitiveSpec.Config(field).pattern(pattern, patternDescription)));
      return this;
    }
  }

  /**
   * Constructs a type from a front-end perspective, generating a suitable unambiguous luxem back
   * spec to match.
   */
  public static class ATypeBuilder {
    private final String id;
    private final AFrontBuilder front = new AFrontBuilder();
    private final ABackBuilder back;
    private final String description;
    private int precedence;
    private String defaultSelection;

    public ATypeBuilder(String id, String description) {
      this.id = id;
      this.description = description;
      this.back = new ABackBuilder(id);
    }

    public ATypeBuilder gapKey(String text) {
      front.gapKey(text);
      return this;
    }

    public AtomType build() {
      return new FreeAtomType(
          new FreeAtomType.Config(
                  description,
                  new AtomType.Config(id, back.build(), front.front)
                      .defaultSelection(defaultSelection))
              .precedence(this.precedence));
    }

    public AtomType build(Function<ROList<BackSpec>, ROList<BackSpec>> wrap) {
      return new FreeAtomType(
          new FreeAtomType.Config(
              description, new AtomType.Config(id, wrap.apply(back.build()), front.front)));
    }

    public ATypeBuilder t(String text, ModelColor color) {
      front.fixed(text, color);
      return this;
    }

    public ATypeBuilder s() {
      front.space();
      return this;
    }

    public ATypeBuilder sb(String text, ModelColor color) {
      front.startBracket(text, color);
      return this;
    }

    public ATypeBuilder eb(String text, ModelColor color) {
      front.endBracket(text, color);
      return this;
    }

    public ATypeBuilder atom(String id, String elementType) {
      back.atom(id, elementType);
      front.atom(id);
      return this;
    }

    public ATypeBuilder zeroSplit() {
      front.zeroSplit();
      return this;
    }

    public ATypeBuilder compactZeroSplit() {
      front.compactZeroSplit();
      return this;
    }

    public ATypeBuilder compactSplit() {
      front.compactSplit();
      return this;
    }

    public ATypeBuilder compactBaseSplit() {
      front.compactBaseSplit();
      return this;
    }

    public ATypeBuilder array(String id, String elementType, String separator) {
      back.array(id, elementType);
      front.array(id, separator);
      return this;
    }

    public ATypeBuilder nestedIdentifier(String child, ModelColor color) {
      back.nestedIdentifier(child);
      front.pattern(child, color, COLOR_INCOMPLETE);
      return this;
    }

    public ATypeBuilder type(String t) {
      back.type(t);
      return this;
    }

    public ATypeBuilder primitive(String id, ModelColor color) {
      front.primitive(id, color);
      back.primitive(id);
      return this;
    }

    public ATypeBuilder styledPrimitive(String id, ModelColor color, String font, double size) {
      front.styledPrimitive(id, color, font, size);
      back.primitive(id);
      return this;
    }

    public ATypeBuilder placeholder(String text, String field, ModelColor color) {
      front.placeholder(text, field, color);
      return this;
    }

    public ATypeBuilder precedence(int precedence) {
      this.precedence = precedence;
      return this;
    }

    public ATypeBuilder pattern(
        String field,
        Pattern pattern,
        String patternDescription,
        ModelColor color,
        ModelColor invalidColor) {
      back.pattern(field, pattern, patternDescription);
      front.pattern(field, color, invalidColor);
      return this;
    }

    public ATypeBuilder defaultSelection(String id) {
      defaultSelection = id;
      return this;
    }

    public ATypeBuilder vspacer(double ascent, double descent) {
      front.vspacer(ascent, descent);
      return this;
    }

    public ATypeBuilder custom(Consumer<ATypeBuilder> c) {
      c.accept(this);
      return this;
    }
  }
}
