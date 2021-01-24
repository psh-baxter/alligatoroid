# Pidgoon

A Java GLR-like parser generator with tools to parse both byte streams and streams of arbitrary events (input events, tokens, tree nodes, etc).  It has no lexer, parses all branches simultaneously, has no ambiguity, and can handle left recursion if you have enough memory.  Actually, that it _can_ have a lexer if you want.  You can even have two lexers, and nest them.

Probably not particularly fast.

## Include with:

Maven

```xml
<dependency>
    <groupId>com.zarbosoft</groupId>
    <artifactId>pidgoon</artifactId>
    <version>0.2.1</version>
</dependency>
```

## Example

```java
Grammar grammar = new Grammar();
grammar.add(
    "root",
    new Operator(
        new Union().add(Grammar.stringSequence("hello"), Grammar.stringSequence("yes")),
        store -> store.pushStack("parsed")
    )
);
assertThat(new Parse().grammar(grammar).root("root").parse("hello"), equals("parsed"));
```

## Library Structure

There are two subpackages: `events` and `bytes`.  `events` can parse streams of arbitrary data, but `bytes` has helper methods for dealing with byte streams including constructing nodes for character sequences and error reporting including stream byte offset.

`Grammar` describes a grammar.  If you don't go out of your way to modify the grammar while parsing, it is effectively immutable and thread safe.

`Node` is an element of the grammar.

`Parse` is a specific parse.  For each file/stream you parse you need a new `Parse` object.  Call `parse()` to begin parsing. Parsing raises an error if the stream doesn't match the grammar. Multiple branches of your grammar can match simultaneously. `parse()` returns whatever's on the top of the stack of the first branch that matches with exactly one back on the stack, otherwise `null`.