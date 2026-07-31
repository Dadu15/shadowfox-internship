# Learnings — Task 1: Enhanced Console-Based Calculator

## What was the hardest bug?

Getting the **Shunting Yard parser to handle unary minus** correctly. A naive
tokenizer sees `-3 + 5` and produces the tokens `-`, `3`, `+`, `5`, which the
evaluator then misreads as a binary `-` operator with only one operand
sitting in front of it (nothing to subtract *from*).

The same ambiguity shows up mid-expression too, e.g. `4 * -2` or
`(-3 + 5) * 2` — anywhere a minus sign appears at the very start of the
expression, right after `(`, or right after another operator, it's acting as
a sign on the next number, not a subtraction operator.

## How did I fix it?

In the tokenizer, before treating `+`/`-` as an operator character, I check
whether the *previous* token was empty, an operator, or an opening
parenthesis. If so, the sign is folded directly into the number that follows
it (so `-3` becomes a single token `"-3"`, not two tokens `"-"` and `"3"`).
Everywhere else, `+`/`-` are treated as normal binary operators.

This meant separating "is this token a number" from "is this token an
operator" into an explicit check (`isNumber()`), rather than assuming a
token starting with `-` is always subtraction — a small distinction that
made the whole precedence logic much more predictable to reason about and
test.
