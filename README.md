archi  [![Build Status](https://travis-ci.org/Macroz/archi.svg?branch=master)](https://travis-ci.org/Macroz/archi)
=====

> Draw (architecture) diagrams as easily as Archi.

Use a convenient DSL for describing your diagrams and get an interactive HTML visualization (or plain SVG) for free!

Usage
-----

Add to your project.clj:

```clj
[macroz/archi "0.1.0"]
```

Run in your favourite REPL:

```clj
> (use '[archi.core])
> (defnodes Archi World)
> (def features [(feature ["Hello"] [Archi World])])
> (render! features)
```

Open the generated archi.html in your favourite browser.

You should see something like this:

![Example graph](https://rawgithub.com/Macroz/archi/examples/tree/master/archi.svg)
[Example HTML](https://rawgithub.com/Macroz/archi/examples/tree/master/archi.html)

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
