function highlight(data) {
  var originalStyles = {};
  
  function highlightNode(id) {
    var o = originalStyles[id] || {c1: {}, c2: {}};
    var c1 = document.getElementById(id).children[1];
    var c2 = document.getElementById(id).children[2];
    o.c1.fill = c1.getAttribute('fill');
    o.c1.stroke = c1.getAttribute('stroke');
    o.c2.fill = c2.getAttribute('fill');
    o.c2.stroke = c2.getAttribute('stroke');
    originalStyles[id] = o;
    c1.setAttribute('fill', '#000000');
    c1.setAttribute('stroke', '#000000');
    c2.setAttribute('fill', '#ffffff');
    c2.setAttribute('stroke', '#ffffff');
  }

  function highlightEdge(id) {
    var o = originalStyles[id] || {};
    var c1 = document.getElementById(id).children[1];
    var c2 = document.getElementById(id).children[2];
    var l = document.getElementById(id).children[2].children[0].children[0];
    o.strokeWidth = c1.getAttribute('stroke-width');
    o.fontSize = l.getAttribute('font-size');
    originalStyles[id] = o;
    c1.setAttribute('stroke-width', '8');
    c2.setAttribute('stroke-width', '8');
    l.setAttribute('font-size', '24');
  }

  function restoreNode(id) {
    var o = originalStyles[id];
    var c1 = document.getElementById(id).children[1];
    var c2 = document.getElementById(id).children[2];
    c1.setAttribute('fill', o.c1.fill);
    c1.setAttribute('stroke', o.c1.stroke);
    c2.setAttribute('fill', o.c2.fill);
    c2.setAttribute('stroke', o.c2.stroke);
    delete originalStyles[id]
  }

  function restoreEdge(id) {
    var o = originalStyles[id];
    var c1 = document.getElementById(id).children[1];
    var c2 = document.getElementById(id).children[2];
    var l = document.getElementById(id).children[2].children[0].children[0];
    c1.setAttribute('stroke-width', o.strokeWidth);
    c2.setAttribute('stroke-width', o.strokeWidth);
    l.setAttribute('font-size', o.fontSize);
    delete originalStyles[id]
  }

  function forData(d, nodeFn, edgeFn) {
    for (var i = 0; i < d.nodes.length; ++i) {
      nodeFn(d.nodes[i]);
    }
    for (var i = 0; i < d.edges.length; ++i) {
      edgeFn(d.edges[i]);
    }
  }

  function enterNode(event) {
    var id = event.target.id;
    forData(data[id], highlightNode, highlightEdge);
  }
  
  function leaveNode(event) {
    var id = event.target.id;
    forData(data[id], restoreNode, restoreEdge);
  }
  
  function enterEdge(event) {
    var id = event.target.id;
    forData(data[id], highlightNode, highlightEdge);
  }
  
  function leaveEdge(event) {
    var id = event.target.id;
    forData(data[id], restoreNode, restoreEdge);
  }
  
  function setupListeners(data) {
    for (var id in data) {
      forData(data[id], function (id) {
        var node = document.getElementById(id);
        node.addEventListener('mouseenter', enterNode);
        node.addEventListener('mouseleave', leaveNode);
      }, function (id) {
        var edge = document.getElementById(id);
        edge.addEventListener('mouseenter', enterEdge);
        edge.addEventListener('mouseleave', leaveEdge);
      });
    }
  }

  setupListeners(data);
}
