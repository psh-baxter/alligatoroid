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

import java.util.Set;

public class Main {
  public static final int indentPx = 16;
  public static final int spacePx = 4;

  public static final String baseAlign = "base";
  public static final String indentAlign = "indent";

  public static final String statementGroupType = "statement";
  public static final String declareType = "declare";
  public static final String letType = "let";
  public static final String accessGroupType = "access";
  public static final String expresisonGroupType = "expression";
  public static final String literalGroupType = "literal";
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
        new RelativeAlignmentSpec(new RelativeAlignmentSpec.Config(baseAlign, indentPx, false));
    BackArraySpec statementBackArray =
        new BackArraySpec(
            new BaseBackSimpleArraySpec.Config(
                "statements",
                new BackAtomSpec(new BaseBackAtomSpec.Config(null, statementGroupType))));

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
                                    "declarations",
                                    new BackAtomSpec(new BaseBackAtomSpec.Config(null, letType)))))
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
    TSMap<String, Set<AtomType>> splayedTypes = Syntax.splayGroups(errors, types, groups);
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
    Syntax esTree1 = new Syntax(syntaxConfig);
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
