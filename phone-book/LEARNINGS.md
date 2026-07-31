# Learnings — Task 2: Simple Contact Management System

## What was the hardest bug?

Handling **phone-number changes during an edit** without corrupting the
duplicate-check index. `ContactManager` keeps a `HashSet<String>` of known
phone numbers alongside the contact list, purely so "does this phone number
already exist?" is an O(1) lookup instead of scanning every contact on
every add.

The bug: my first version of `updateContact()` mutated the `Contact`
object's phone number *before* checking whether the new number collided
with someone else's. If it did collide, the set and the contact list ended
up disagreeing — the set had already lost the old number (nothing removed
it yet) but the exception aborted before the new number was added, so a
retry with the *original* phone number would then falsely report "not
found," because the old number had silently vanished from the index in a
half-finished update.

## How did I fix it?

I restructured `updateContact()` to do all validation and collision-checking
**before** touching any shared state:

1. Look up the existing contact by its current phone number.
2. If the phone number is changing, check the new number against the
   `HashSet` for a collision — and stop right there if it collides, before
   anything is mutated.
3. Construct a *new*, fully-validated `Contact` object from the proposed
   values (this also reuses all the existing regex validation in `Contact`'s
   constructor, so invalid emails/phones are caught here too).
4. Only after all of that succeeds, update the `HashSet` (remove old number,
   add new number) and copy the validated fields onto the existing object.

The general lesson: when a mutation touches two data structures that need
to stay in sync (the list and the index set here), do every check that
could fail *first*, and only touch state once you're certain the whole
operation can complete — otherwise a failure partway through leaves things
in an inconsistent state that's hard to debug because it only shows up on
a later, unrelated operation.
