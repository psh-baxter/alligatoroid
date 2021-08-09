The alliga-toroid is an ancient symbol representing recursive complexity and abstraction. Alliga-toroid is a language inspired from this symbol. You can write it with Merman.

# Goals

Without any other context, the language you imagine after reading these goals should be very close to how Alliga-toroid is actually implemented. All features and changes should be evaluated based on these goals.

* Universal
  
   The language should be usable for _any_ sort of software. This is the opposite of domain-specific.
  
   Domains aren't strictly defined, and every problem people try to solve with software will have it's own unique domain. When you use a domain specific language, you will hit these boundaries and then fight with the language authors who won't want to support your use case.
  
   Proliferation of domain-specific languages restricts code reuse.
  
* Low level but powerful

   The language should provide full functionality on any platform it supports.  Ex: in JVM, we'd aim to provide the ability to access all JVM features, same with LLVM.

   Rather than provide closures or borrow checking, provide the tools that library developers need to implement their own closures, or their own memory safety tools (including a borrow checker).

* Permissive

   As a language developer, I can't imagine every possible use case people have for the language. If it doesn't require extraordinary effort and there isn't already another solution, don't reject feature requests as being dangerous, or a bad idea. External linters or advanced abstractions provided by libraries can protect people if they want it.

   (There's some subjectivity here since not every single feature can be implemented.)

* Machine-friendly

   To encourage automation (smart editors, code generating tools, static analysis, etc) the language should be easy to parse and generate.

* Reusable

   Code should be as easy to share as possible. This means reducing boilerplate, reducing setup, making distribution easy, etc.

* Small core

   This follows from `reusable`-

   The language itself should be as small as possible. All additional functionality should be provided by third party libraries.
  
   Standard libraries universally have limited or buggy, poorly thought out, dated functionality.  Good new libraries compete with poor standard libraries. Development is centralized and therefore harder to scale.

   Third party libraries are a natural way to decentralize language development.

# Non-core goals

* Deterministic

  All dependencies are specified exactly (with a hash). There's no concept of version numbers or ordering. Non-core tooling can be used to update dependencies if desired using a scheme that satisfies the user's safety and new functionality requirements. The opposite, providing determinism in a language that's not deterministic, is difficult.

* Imperative

  I think that declarative development is clear and readable, but I haven't seen a declarative specification that doesn't break down for some use cases. Use-case specific declarative tools can be implemented using imperative functionality.

  An example is styling html: Styling is basically taking an element and assigning display properties to it. CSS is a declarative specification for this assignment. By design it's slow, complicated, and inexpressive. Under the hood there's imperative code implementing CSS. In this case I would want to allow users to provide their own imperative code, with CSS being just one openly developed option.

# Implementation

* No syntax
  
   Instead of a text-based syntax, Alliga-toroid is defined as a language tree in Luxem (a format similar to JSON). This makes it easy to generate and parse. Additionally, it prevents syntax-based concerns from compromising core functionality: for example, using verbose, clear names in the language tree avoids new keywords conflicting with old keywords.

   Additionally, each node in the tree has a document-unique numeric id. This is more specific than line numbers for identifying code locations and provides additional context when diffing and merging.

   By leaving representation to a smart editor, sugars (including domain-specific ones) can be chosen per user as appropriate, and new sugars can be added easily.
  
* Micro-modules

   Every source file is a complete, redistributable module.