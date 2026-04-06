# Walnut Automata

The below is a list of benchmarked automata and references.
If the automata generation is not described in the paper, it can be found in addenda posted at [Walnut papers and books](https://cs.uwaterloo.ca/~shallit/walnut-papers.html).

Note that DFA and mDFA size may not agree with the result in the associated paper. The benchmarks are intermediate automata generated during quantifier elimination, and are often not the final automata outputted by Walnut.

If DFA size is not given, this is because Subset Construction ran beyond our time cutoff. Sometimes size estimates are given when they are known from the associated paper.
If mDFA size is not given, then there is no known determinization result. This is currently the case for **tribpseudo3**.

## Papers and Results

- **Bell, Jason, Thomas F. Lidbetter, and Jeffrey Shallit. "Additive number theory via approximation by regular languages." International Journal of Foundations of Computer Science 31.06 (2020): 667-687.**
  - **[thm5](systems/walnut/thm5.ba):** NFA: 1,790; DFA: 150,420; mDFA: 12

- **Bosma, Wieb, et al. “Using Walnut to Solve Problems from the OEIS.” _arXiv_, 6 Mar. 2025, [https://arxiv.org/abs/2503.04122](https://arxiv.org/abs/2503.04122).**  
  - **[z5](systems/walnut/z5.ba):** NFA: 199; DFA: unknown; mDFA: 169
  - **[coef_rev](systems/walnut/coef_rev.ba):** NFA: 313; DFA: unknown; mDFA: 264

- **Currie, James, et al. "Properties of a ternary infinite word." RAIRO-Theoretical Informatics and Applications 57 (2023): 1.**
  - **[pisotfaceeq](systems/walnut/pisotfaceeq.ba):** NFA: 37,095; DFA: unknown (used over 5 terabytes of memory); mDFA: 1,684
  - **[pisotlargepower](systems/walnut/pisotlargepower.ba):** NFA: 10,859; DFA: 13,114,120; mDFA: 288

- **Fici, Gabriele, and Jeffrey Shallit. "Properties of a class of Toeplitz words." Theoretical Computer Science 922 (2022): 1-12.** 
  - **[toeplitz](systems/walnut/toeplitz.ba):** NFA: 100; DFA: 142,311; mDFA: 432

- **Gabric, Daniel, and Jeffrey Shallit. "The simplest binary word with only three squares." RAIRO-Theoretical Informatics and Applications 55 (2021): 3.**
  - **[thm2test.ba](systems/walnut/thm2test.ba):** NFA: 16,791; DFA:; mDFA: 84
  - **[thm4test.ba](systems/walnut/thm4test.ba):** NFA: 13,065; DFA: 1,660,923; mDFA: 79

- **Meleshko, Joseph, et al. "Pseudoperiodic words and a question of Shevelev." Discrete Mathematics & Theoretical Computer Science 25.Automata, Logic and Semantics (2023).**  
  - **[paper_pseudo2](systems/walnut/paper_pseudo2.ba):** NFA: 293; DFA: 200,649; mDFA: 778  
  - **[rudinpseudo](systems/walnut/rudinpseudo.ba):** NFA: 1,024; DFA: 23,674,378; mDFA: 241  
  - **[threepseudomw](systems/walnut/threepseudomw.ba):** NFA: 64; DFA: 1,705,918; mDFA: 144  
  - **[threepseudovtm](systems/walnut/threepseudovtm.ba):** NFA: 128; DFA: 35,400,982; mDFA: 179  
  - **[tribpseudo3](systems/walnut/tribpseudo3.ba):** NFA: 60,317. Open problem.
  - **[triple](systems/walnut/triple.ba):** NFA: 64; DFA: 2,952,594; mDFA: 521
  - **[triplemw](systems/walnut/triplemw.ba):** NFA: 64; DFA: 1,705,918; mDFA: 144
  - **[triplevtm](systems/walnut/triplevtm.ba):** NFA: 128; DFA: 35,400,982; mDFA: 179

- **Mignoty, Bastien, Antoine Renard, Michel Rigo, and Markus Whiteland. "Automatic proofs in combinatorial game theory." (2024)
  - **[ost2_test](systems/walnut/ost2_test.ba):** NFA:12,478; DFA:128,267; mDFA:65
  - **[ost3_non_redundant](systems/walnut/ost3_non_redundant.ba):** NFA:2,525; DFA:15,532; mDFA:548

- **Mousavi, Hamoon, and Jeffrey Shallit. "Mechanical proofs of properties of the Tribonacci word." International Conference on Combinatorics on Words. Cham: Springer International Publishing, 2015.**
  - **[trib4](systems/walnut/trib4.ba):** NFA: 24,903; DFA: 30,521,806; mDFA: 13
  - **[tribmirror](systems/walnut/tribmirror.ba):** NFA: 14,001; DFA: unknown; mDFA: 174
  - **[triboddpal](systems/walnut/triboddpal.ba):** NFA: 2,633; DFA: 1,098,563; mDFA: 47

- **Schaeffer, Luke, and Jeffrey Shallit. "The first-order theory of binary overlap-free words is decidable." Canadian Journal of Mathematics 76.4 (2024): 1144-1162.**
  - **[agrees](systems/walnut/agrees.ba):** NFA: 152; DFA: 3,534,633; mDFA: 122

- **Shallit, Jeffrey. “Developing Walnut Commands for Sequence Properties.” _Slides_, University of Waterloo, [https://cs.uwaterloo.ca/~shallit/Talks/walnut-properties.pdf](https://cs.uwaterloo.ca/~shallit/Talks/walnut-properties.pdf).**  
  - **[tribsquarelen](systems/walnut/tribsquarelen.ba):** NFA: 4,611; DFA: 13,159,141; mDFA: 23

- **Shallit, Jeffrey. The Logical Approach to Automatic Sequences. Vol. 482. Cambridge University Press, 2022.**
  - **[abelcube](systems/walnut/abelcube.ba):** NFA: 4,651; DFA: 2,230,715; mDFA: 21
  - **[noabelcube](systems/walnut/noabelcube.ba):** NFA: 4,651; DFA: 2,230,715; mDFA: 19
  - **[gamard2factoreq](systems/walnut/gamard2factoreq.ba):** NFA: 9,195; DFA: 17,699,273; mDFA: 8,072

- **Shallit, Jeffrey. "The Narayana morphism and related words." arXiv preprint arXiv:2503.01026 (2025).**
  - **[jaef](systems/walnut/jaef.ba):** NFA: 55455; DFA: > 100,000,000 (array overflow in Walnut); mDFA: 685

- **Shallit, Jeffrey. “Walnut: A Tool for Doing Combinatorics on Words.” _Slides_, University of Waterloo, [https://cs.uwaterloo.ca/~shallit/Talks/walnut-unpaused.pdf](https://cs.uwaterloo.ca/~shallit/Talks/walnut-unpaused.pdf).**  
- **Shallit, Jeffrey. “Adventures with an Automatic Prover.” _Slides_, University of Waterloo, [https://cs.uwaterloo.ca/~shallit/Talks/simons3.pdf](https://cs.uwaterloo.ca/~shallit/Talks/simons3.pdf).**
  - **[tribfaceeq](systems/walnut/tribfaceeq.ba):** NFA: 10,452; DFA 323,831,403; mDFA: 64

- **Shallit, Jeffrey. Personal communication.**  
  - **[och3](systems/walnut/och3.ba):** NFA: 2,025; DFA: unknown; mDFA: 616
  - **[res](systems/walnut/res.ba):** NFA: 5,312; DFA: unknown; mDFA: 2,656
  - **[r3ef](systems/walnut/r3ef.ba):** NFA: 128; DFA: 46,036,053; mDFA: 3,044,069

- **Shallit, Jeffrey, Arseny Shur, and Stefan Zorcic. "Power-free complementary binary morphisms." Journal of Combinatorial Theory, Series A 207 (2024): 105910.**
  - **[test0001](systems/walnut/test0001.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,395
  - **[test0010](systems/walnut/test0010.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,390
  - **[test0011](systems/walnut/test0011.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,397
  - **[test0101](systems/walnut/test0101.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,392
  - **[test0110](systems/walnut/test0110.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,399
  - **[test0010b](systems/walnut/test0010b.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,420
  - **[test0011b](systems/walnut/test0011b.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,420
  - **[test0101b](systems/walnut/test0101b.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,420
  - **[test0110b](systems/walnut/test0110b.ba):** NFA: 11,631; DFA: approximately 163 million; mDFA: 1,420
  - **[testf0010](systems/walnut/testf0010.ba):** NFA: 12,140; DFA: approximately 187 million; mDFA: 1,938
  - **[testf0101](systems/walnut/testf0101.ba):** NFA: 12,140; DFA: approximately 187 million; mDFA: 1,938
  - **[testf0110](systems/walnut/testf0110.ba):** NFA: 12,140; DFA: approximately 187 million; mDFA: 1,938

- **Shallit, Jeffrey, and Ramin Zarifi. "Circular critical exponents for Thue–Morse factors." RAIRO-Theoretical Informatics and Applications 53.1-2 (2019): 37-49.**
  - **[crep_1](systems/walnut/crep_1.ba):** NFA: 280; DFA: 80,206; mDFA: 714  
  - **[crep_2](systems/walnut/crep_2.ba):** NFA: 186; DFA: 87,506; mDFA: 325  
  - **[prcrep2](systems/walnut/prcrep2.ba):** NFA: 1,160; DFA: 822,161; mDFA: 1,863  
  - **[prcrep3](systems/walnut/prcrep3.ba):** NFA: 900; DFA: 601,172; mDFA: 1,307

- **Shallit, Jeffrey, Sonja Linghui Shan, and Kai Hsiang Yang. "Automatic sequences in negative bases and proofs of some conjectures of Shevelev." RAIRO-Theoretical Informatics and Applications 57 (2023): 4.**
  - **[testshur](systems/walnut/testshur.ba):** NFA: 48,257; DFA: 25,109,972; mDFA: 11,198
