archi  [![Build Status](https://travis-ci.org/Macroz/archi.svg?branch=master)](https://travis-ci.org/Macroz/archi)
=====

> Draw (architecture) diagrams as easily as Archi.

Use a convenient DSL for describing your diagrams and get an interactive HTML visualization (or plain SVG) for free!

Usage
-----

Add to your project.clj:

[![Clojars Project](http://clojars.org/macroz/archi/latest-version.svg)](http://clojars.org/macroz/archi)

Run in your favourite REPL:

```clj
(use '[archi.core])
(defnodes Archi World)
(def features [(feature ["Hello"] [Archi World])])
(render! features)
```

Open the generated archi.html in your favourite browser.

You should see something like this:

![Example graph](https://rawgit.com/Macroz/archi/master/examples/hello.svg)

[Example as interactive HTML](https://rawgit.com/Macroz/archi/master/examples/hello.html)

It's possible to style the nodes and edges by passing :node->descriptor and :edge->descriptor parameters to the render-method. This functionality uses standard [tangle](https://www.github.com/Macroz/tangle) features.

See also [archi-example](http://www.github.com/Macroz/archi-example) for a more elaborate use case.

Future Plans
------------

- Docstrings
- Document options
- More complex examples
- Easy customization of styles for common use cases
- Extract interaction support to tangle or other library
- Tool version that works with java -jar and evals your definitions
- Replace Graphviz with something more dynamic
- Hierarchical graphs with ability to expand nodes
- Dynamic, when tangle is too

Suggestions, and pull requests, welcome!

License
-------

Copyright © 2015 Markku Rontu

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
