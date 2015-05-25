archi
=====

> Draw (architecture) diagrams as easily as Archi.

Use a convenient DSL for describing your diagrams and get an interactive HTML visualization (or plain SVG) for free!

Usage
-----

    (use '[archi.core])

    (defnodes Archi World)
    (def features [(feature ["Hello"] [Archi World])])

    (render! features)

Open archi.html in your favourite browser.

See also [archi-example](http://www.github.com/Macroz/archi-example).

Future Plans
------------

- Replace Graphviz with something more dynamic
- Hierarchical graphs with ability to expand nodes

Suggestions welcome!

License
-------

Copyright © 2015 Markku Rontu

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
