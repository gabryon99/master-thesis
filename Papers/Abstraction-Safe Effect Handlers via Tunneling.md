### Abstraction-Safe Effect Handlers via Tunneling

Authors: *YIZHOU ZHANG*, *ANDREW C. MYERS*
Date: 02 January 2019
DOI: 10.1145/3290318

The paper shows how the effect handlers semantics are not *abstraction-safe*. In fact, the example shown explains how the abstraction of a given function could be broken due to the **effect handlers** scopes. Right now, the effect handler scopesis similar to the **dynamic scoping** technique, which can be confusing for the user.

As said by the abstract:
> *We demonstrate that abstraction-safe algebraic effect handlers are possible by giving them a new semantics*. ==The key insight is that code should only handle effects it is aware of.== 

