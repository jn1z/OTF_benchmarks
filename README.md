# OTF (On-The-Fly) Algorithm Benchmarks

See the paper [Deconstructing Subset Construction: Reducing While Determinizing]() for more details.

# Algorithm Configurations



# Language Projection

*Language projection* is a fundamental operation in formal language theory that, effectively, merges some letters of a DFA. When applied to a DFA, this operation usually introduces non-determinism (forms an NFA). However, this also introduces a certain amount of structure into the resultant NFA. See the paper for more details.

Java code: [InputTVLanguageProjection.java](src/main/java/OTF_benchmark/InputTVLanguageProjection.java)

# Language Projection with Product Automata

Here we introduce even more structure into the resultant NFA. We language-project two DFAs, determinize and minimize, combine them via a product automaton operation, and then language-project again.

This is a very finicky benchmark where slight changes in original NFA size,
acceptance, or language-projection cause the generated NFA to be either trivial
or experience blowup.

In particular, we look at alphabets of size [16, 24, 32],
language compression factors [1.3, 1.4, 1.5, 1.6, 1.7],
accepted factor (fraction of DFA states that are accepting) of 0.1 -- essentially meaning 1 or 2 statea are accepting,
NFA sizes of exactly 4 states,
and an XOR product operation.

Java code: [InputTVLanguageProjectionIterated.java](src/main/java/OTF_benchmark/InputTVLanguageProjectionIterated.java)

# Büchi Arithmetic (Walnut)

Various examples of Büchi Arithmetic encountered via the [Walnut](https://github.com/Walnut-Theorem-Prover/Walnut) tool.

See [walnut.md](walnut.md) for details.

