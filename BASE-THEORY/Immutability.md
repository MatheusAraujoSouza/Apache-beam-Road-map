# Immutability

A PCollection is immutable. Once created, you cannot add, remove, or change individual elements. A Beam Transform might process each element of a PCollection and generate new pipeline data (as a new PCollection), but it does not consume or modify the original input collection.



Note: Beam SDKs avoid unnecessary copying of elements, so PCollection contents are logically immutable, not physically immutable. Changes to input elements may be visible to other DoFns executing within the same bundle, and may cause correctness issues. As a rule, it’s not safe to modify values provided to a DoFn.

